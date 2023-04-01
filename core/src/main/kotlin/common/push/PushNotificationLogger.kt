package com.wafflestudio.snu4t.common.push

import com.wafflestudio.snu4t.common.push.data.FcmLog
import com.wafflestudio.snu4t.common.push.repository.FcmLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

@Component
class PushNotificationLogger(
    private val fcmLogRepository: FcmLogRepository,
) {
    private val logs = MutableSharedFlow<FcmLogs>(extraBufferCapacity = 10)

    init {
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
            .launch(SupervisorJob()) { logs.collect { fcmLogRepository.saveAll(it.logs).collect() } }
    }

    suspend fun log(list: List<FcmLog>) {
        logs.emit(FcmLogs(list))
    }

    suspend fun log(single: FcmLog) {
        logs.emit(FcmLogs(listOf(single)))
    }

    private data class FcmLogs(val logs: List<FcmLog>)
}
