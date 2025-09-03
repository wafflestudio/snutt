package com.wafflestudio.snutt.common.util

import kotlinx.coroutines.delay

object CoroutineUtils {
    suspend fun <T> retryWithExponentialBackoff(
        retries: Int = 5,
        initialDelay: Long = 1000L,
        factor: Long = 2,
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
        throw IllegalStateException("재시도 실패")
    }
}
