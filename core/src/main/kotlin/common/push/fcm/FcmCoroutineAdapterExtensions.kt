package com.wafflestudio.snutt.common.push.fcm

import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <F : Any> ApiFuture<F>.await(): F = this.await { it }

suspend fun <F : Any, R : Any?> ApiFuture<F>.await(successHandler: (F) -> R): R =
    suspendCancellableCoroutine { cont ->
        ApiFutures.addCallback(
            this,
            object : ApiFutureCallback<F> {
                override fun onFailure(t: Throwable?) {
                    cont.resumeWithException(t ?: IOException("Unknown error"))
                }

                override fun onSuccess(result: F) {
                    cont.resume(successHandler(result))
                }
            },
            Dispatchers.IO.asExecutor(),
        )
        cont.invokeOnCancellation {
            this.cancel(true)
        }
    }
