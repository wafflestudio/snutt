package com.wafflestudio.snutt.notification.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.BigQueryOptions
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import com.wafflestudio.snutt.notification.data.PushHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

/**
 * 푸시 발송 기록을 남긴다. 기록 실패가 푸시 발송을 막거나 실패시키면 안 되므로 예외를 던지지 않는다.
 */
interface PushHistoryRecorder {
    fun record(pushHistory: PushHistory)
}

@Service
@Profile("!test")
internal class BigQueryPushHistoryRecorder(
    @param:Value("\${google.bigquery.push-history.enabled:false}") private val enabled: Boolean,
    @param:Value("\${google.bigquery.push-history.project-id:}") projectId: String,
    @param:Value("\${google.bigquery.push-history.dataset:server_events}") dataset: String,
    @param:Value("\${google.bigquery.push-history.table:push_history}") table: String,
    @param:Value("\${google.firebase.project-id}") firebaseProjectId: String,
    @param:Value("\${google.firebase.service-account}") private val serviceAccountString: String,
) : PushHistoryRecorder,
    DisposableBean {
    private val log = LoggerFactory.getLogger(javaClass)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val SHUTDOWN_TIMEOUT_MILLIS = 10_000L
    }

    private val tableId = TableId.of(projectId.ifBlank { firebaseProjectId }, dataset, table)

    private val bigQuery: BigQuery by lazy {
        BigQueryOptions
            .newBuilder()
            .setProjectId(tableId.project)
            .setCredentials(GoogleCredentials.fromStream(serviceAccountString.byteInputStream()))
            .build()
            .service
    }

    override fun record(pushHistory: PushHistory) {
        if (!enabled) return
        scope.launch {
            runCatching { insert(pushHistory) }
                .onFailure { log.warn("push_history BigQuery 적재 실패 (push_id={})", pushHistory.pushId, it) }
        }
    }

    // batch는 발송 직후 프로세스가 끝나므로 종료 전에 남은 적재를 기다린다
    override fun destroy() {
        val pending =
            scope.coroutineContext.job.children
                .toList()
        runBlocking {
            if (withTimeoutOrNull(SHUTDOWN_TIMEOUT_MILLIS) { pending.joinAll() } == null) {
                log.warn("종료 전에 push_history 적재 {}건을 마치지 못했습니다", pending.count { it.isActive })
            }
        }
        scope.cancel()
    }

    private fun insert(pushHistory: PushHistory) {
        val request =
            InsertAllRequest
                .newBuilder(tableId)
                // 같은 push_id 재전송 시 BigQuery가 best-effort로 중복 제거
                .addRow(pushHistory.pushId, pushHistory.toRow())
                .build()
        val response = bigQuery.insertAll(request)
        if (response.hasErrors()) {
            log.warn("push_history BigQuery 적재 실패 (push_id={}, insertErrors={})", pushHistory.pushId, response.insertErrors)
        }
    }

    private fun PushHistory.toRow(): Map<String, Any?> =
        mapOf(
            "push_id" to pushId,
            "push_type" to pushType,
            "sent_at" to sentAt.toString(),
            "target_user_count" to targetUserCount,
            "sent_user_count" to sentUserCount,
            "sent_token_count" to sentTokenCount,
            "is_global" to isGlobal,
            "topic" to topic,
            "title" to title,
        )
}
