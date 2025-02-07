package com.wafflestudio.snutt.common.extension

import com.wafflestudio.snutt.common.util.WebClientUtils.logWebClientError
import com.wafflestudio.snutt.common.util.addHeaders
import com.wafflestudio.snutt.common.util.uriWithParams
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient

suspend inline fun <reified T : Any> WebClient.get(
    uri: String,
    params: Map<String, Any>? = null,
    headers: Map<String, Any>? = null,
): Result<T?> {
    return runCatching {
        get()
            .apply {
                headers?.let { addHeaders(it) }
                uriWithParams(uri, params)
            }
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<T>() {})
            .awaitSingleOrNull()
    }.onFailure {
        logWebClientError(uri, it)
    }
}

suspend inline fun <reified ResponseT : Any> WebClient.post(
    uri: String,
    body: Any? = null,
    headers: Map<String, Any>? = null,
): Result<ResponseT?> {
    return runCatching {
        post()
            .uri(uri)
            .apply {
                headers?.let { addHeaders(it) }
                body?.let { bodyValue(it) }
            }
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<ResponseT>() {})
            .awaitSingleOrNull()
    }.onFailure {
        logWebClientError(uri, it)
    }
}
