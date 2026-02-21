package com.wafflestudio.snutt.common.extension

import com.oracle.bmc.responses.AsyncHandler
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Future
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun <REQUEST, RESPONSE> awaitOciCall(execute: (AsyncHandler<REQUEST, RESPONSE>) -> Future<RESPONSE>): RESPONSE =
    suspendCancellableCoroutine { continuation ->
        val future = execute(OciAsyncHandler(continuation))
        continuation.invokeOnCancellation {
            future.cancel(true)
        }
    }

private class OciAsyncHandler<REQUEST, RESPONSE>(
    private val continuation: CancellableContinuation<RESPONSE>,
) : AsyncHandler<REQUEST, RESPONSE> {
    override fun onSuccess(
        request: REQUEST,
        response: RESPONSE,
    ) {
        continuation.resume(response)
    }

    override fun onError(
        request: REQUEST,
        error: Throwable,
    ) {
        continuation.resumeWithException(error)
    }
}
