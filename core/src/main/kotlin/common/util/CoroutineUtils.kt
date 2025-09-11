package com.wafflestudio.snutt.common.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

object CoroutineUtils {
    val Dispatchers.Loom: CoroutineDispatcher
        get() = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Loom)

    suspend fun <T> retryWithExponentialBackoff(
        retries: Int = 10,
        initialDelay: Long = 1000L,
        factor: Long = 2L,
        block: suspend () -> T,
    ): T {
        var currentDelay = initialDelay
        for (i in 1..retries) {
            try {
                return block()
            } catch (e: Exception) {
                if (i == retries) {
                    throw e
                }
                delay(currentDelay)
                currentDelay *= factor
            }
        }
        throw IllegalStateException("Can't reach here")
    }
}
