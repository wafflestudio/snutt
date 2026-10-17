package com.wafflestudio.snutt.filter

import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.exception.ErrorType
import com.wafflestudio.snutt.common.exception.EvServiceProxyException
import com.wafflestudio.snutt.common.exception.SnuttException
import com.wafflestudio.snutt.config.USER_ATTRIBUTE_KEY
import com.wafflestudio.snutt.users.data.User
import kotlinx.coroutines.CancellationException
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.netty.channel.AbortedException
import tools.jackson.databind.ObjectMapper

@Component
@Order(0)
@RegisterReflectionForBinding(ErrorBody::class)
class ErrorWebFilter(
    private val objectMapper: ObjectMapper,
) : WebFilter {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> =
        chain
            .filter(exchange)
            .onErrorResume { throwable ->
                val errorBody: ErrorBody
                val httpStatusCode: HttpStatusCode
                when (throwable) {
                    is EvServiceProxyException -> {
                        httpStatusCode = throwable.statusCode
                        errorBody =
                            throwable.errorResponse.let {
                                ErrorBody(
                                    it.error.code,
                                    "",
                                    it.error.message,
                                    it.error.message,
                                )
                            }
                    }

                    is SnuttException -> {
                        httpStatusCode = throwable.error.httpStatus
                        errorBody = makeErrorBody(throwable)
                    }

                    is ResponseStatusException -> {
                        httpStatusCode = throwable.statusCode
                        errorBody =
                            makeErrorBody(
                                SnuttException(errorMessage = throwable.body.title ?: ErrorType.DEFAULT_ERROR.errorMessage),
                            )
                    }

                    is AbortedException, is CancellationException -> {
                        httpStatusCode = HttpStatus.NO_CONTENT
                        errorBody = makeErrorBody(SnuttException())
                    }

                    else -> {
                        httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR
                        errorBody = makeErrorBody(SnuttException())
                    }
                }

                logErrorResponse(exchange, throwable, httpStatusCode, errorBody)

                if (!exchange.response.isCommitted) {
                    exchange.response.statusCode = httpStatusCode
                    exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                    exchange.response.writeWith(
                        Mono.just(
                            exchange.response
                                .bufferFactory()
                                .wrap(objectMapper.writeValueAsBytes(errorBody)),
                        ),
                    )
                } else {
                    Mono.empty()
                }
            }

    /**
     * 5xx 응답을 남긴다.
     *
     * 지금까지는 예상하지 못한 예외(else 분기)만 로그를 남겼다. [SnuttException] 중에도
     * 500 을 내는 것이 있고 [EvServiceProxyException] 도 5xx 를 그대로 전달하는데,
     * 그런 경우 500 이 나가면서 서버 로그에는 아무것도 남지 않았다.
     *
     * 4xx 는 남기지 않는다. 비밀번호 오류나 토큰 만료처럼 예상된 동작이라 애플리케이션
     * 에러 로그의 대상이 아니고, 필요해지면 게이트웨이 접근 로그로 보는 편이 맞다.
     *
     * 경로는 path variable 이 치환되지 않은 매칭 패턴을 쓴다. 친구 초대 토큰처럼 경로에 담기는
     * 값이 로그로 새어 나가지 않게 하기 위함이다.
     */
    private fun logErrorResponse(
        exchange: ServerWebExchange,
        throwable: Throwable,
        httpStatusCode: HttpStatusCode,
        errorBody: ErrorBody,
    ) {
        if (!httpStatusCode.is5xxServerError) return

        val request = exchange.request
        val path = exchange.attributes[HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE]?.toString() ?: request.path.value()
        val userId = (exchange.attributes[USER_ATTRIBUTE_KEY] as? User)?.id
        val appVersion = (exchange.attributes[CLIENT_INFO_ATTRIBUTE_KEY] as? ClientInfo)?.appVersion?.appVersion

        log.error(
            "{} {} -> {} errcode={} userId={} appVersion={} query={}",
            request.method.name(),
            path,
            httpStatusCode.value(),
            errorBody.errcode,
            userId,
            appVersion,
            request.uri.rawQuery,
            throwable,
        )
    }

    private fun makeErrorBody(exception: SnuttException): ErrorBody =
        ErrorBody(exception.error.errorCode, exception.title, exception.errorMessage, exception.displayMessage)
}

data class ErrorBody(
    val errcode: Long,
    val title: String,
    val message: String,
    val displayMessage: String,
)
