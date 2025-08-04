package com.wafflestudio.snutt.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.net.URI

object WebClientUtils {
    private val log = LoggerFactory.getLogger(javaClass)
    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModules(JavaTimeModule())

    fun logWebClientError(
        uri: String,
        it: Throwable,
    ) {
        if (it is WebClientResponseException) {
            log.error("[WEBCLIENT ERROR] {}\n{}", uri, it.responseBodyAsString, it)
        } else {
            log.error("[WEBCLIENT ERROR] {}\n{}", uri, it.message, it)
        }
    }

    inline fun <reified T> Result<*>.tryConvertToErrorBody(): T? {
        return exceptionOrNull()?.let {
            return if (it is WebClientResponseException) {
                runCatching {
                    objectMapper.readValue<T>(it.responseBodyAsString)
                }.getOrNull()
            } else {
                null
            }
        }
    }
}

fun WebClient.RequestHeadersSpec<*>.addHeaders(headers: Map<String, Any>) =
    apply {
        headers.forEach { (key, value) -> header(key, value.toString()) }
    }

fun WebClient.UriSpec<*>.uriWithParams(
    uri: String,
    params: Map<String, Any>?,
) = apply {
    params?.let {
        uri { builder ->
            URI(uri).let {
                builder
                    .host(it.host)
                    .scheme(it.scheme)
                    .port(it.port)
                    .path(it.path)
                    .queryParams(buildMultiValueMap(params))
                    .build()
            }
        }
    } ?: uri(uri)
}

fun buildMultiValueMap(params: Map<String, Any>) =
    LinkedMultiValueMap<String, String>().apply {
        params.forEach { (key, value) ->
            if (value is Iterable<*>) {
                value.filterNotNull().forEach { v -> add(key, v.toString()) }
            } else {
                add(key, value.toString())
            }
        }
    }
