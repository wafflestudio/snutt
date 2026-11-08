package com.wafflestudio.snutt.notification.service

import com.google.auth.oauth2.GoogleCredentials
import com.wafflestudio.snutt.common.util.CoroutineUtils
import com.wafflestudio.snutt.notification.data.PushHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import java.time.Duration

/**
 * 푸시 발송 기록을 남긴다. 기록 실패가 푸시 발송을 막거나 실패시키면 안 되므로 예외를 던지지 않는다.
 */
interface PushHistoryRecorder {
    fun record(pushHistory: PushHistory)
}

/**
 * BigQuery `tabledata.insertAll` REST API로 push_history 테이블에 한 줄씩 적재한다.
 * 네이티브 이미지 호환을 위해 google-cloud-bigquery 라이브러리 대신 WebClient로 직접 호출하고,
 * 요청/응답은 Map으로 직렬화해서 별도 reflection hint가 필요 없다.
 */
@Service
@Profile("!test")
internal class BigQueryPushHistoryRecorder(
    @param:Value("\${google.bigquery.push-history.enabled:false}") private val enabled: Boolean,
    @param:Value("\${google.bigquery.push-history.project-id:}") projectId: String,
    @param:Value("\${google.bigquery.push-history.dataset:server_events}") private val dataset: String,
    @param:Value("\${google.bigquery.push-history.table:push_history}") private val table: String,
    @param:Value("\${google.firebase.project-id}") firebaseProjectId: String,
    @param:Value("\${google.firebase.service-account}") private val serviceAccountString: String,
    @param:Value("\${http.response-timeout}") responseTimeout: Duration,
) : PushHistoryRecorder {
    private val log = LoggerFactory.getLogger(javaClass)

    private val projectId = projectId.ifBlank { firebaseProjectId }

    private val credentials: GoogleCredentials by lazy {
        GoogleCredentials.fromStream(serviceAccountString.byteInputStream()).createScoped(BIGQUERY_INSERT_DATA_SCOPE)
    }

    private val webClient =
        WebClient
            .builder()
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(responseTimeout)))
            .baseUrl(BIGQUERY_BASE_URL)
            .build()

    companion object {
        private const val BIGQUERY_BASE_URL = "https://bigquery.googleapis.com/bigquery/v2/"
        private const val BIGQUERY_INSERT_DATA_SCOPE = "https://www.googleapis.com/auth/bigquery.insertdata"
    }

    override fun record(pushHistory: PushHistory) {
        if (!enabled) return
        CoroutineUtils.applicationScope.launch {
            runCatching { insert(pushHistory) }
                .onFailure { log.warn("push_history BigQuery 적재 실패 (push_id={})", pushHistory.pushId, it) }
        }
    }

    private suspend fun insert(pushHistory: PushHistory) {
        val accessToken =
            withContext(Dispatchers.IO) {
                credentials.refreshIfExpired()
                credentials.accessToken.tokenValue
            }
        val requestBody =
            mapOf(
                "rows" to
                    listOf(
                        mapOf(
                            // 같은 push_id 재전송 시 BigQuery가 best-effort로 중복 제거
                            "insertId" to pushHistory.pushId,
                            "json" to pushHistory.toRow(),
                        ),
                    ),
            )
        val response =
            webClient
                .post()
                .uri("projects/{project}/datasets/{dataset}/tables/{table}/insertAll", projectId, dataset, table)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .bodyValue(requestBody)
                .retrieve()
                .awaitBody<Map<String, Any?>>()
        response["insertErrors"]?.let {
            log.warn("push_history BigQuery 적재 실패 (push_id={}, insertErrors={})", pushHistory.pushId, it)
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
