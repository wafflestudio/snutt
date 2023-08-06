package com.wafflestudio.snu4t.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.reactivestreams.Publisher
import org.springframework.context.annotation.Configuration
import org.springframework.core.ResolvableType
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.http.codec.EncoderHttpMessageWriter
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Mono

private const val CASE_STRATEGY_HEADER =
    "X-Case-Strategy"

private const val CASE_STRATEGY_ATTRIBUTE =
    "CASE_STRATEGY"
private enum class CaseStrategy {
    SNAKE_CASE, CAMEL_CASE
}

@Configuration
class HttpMessageWriterConfig(
    private val objectMapper: ObjectMapper,
) : WebFluxConfigurer {

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.customCodecs()
            .register(CaseStrategySelectingHttpMessageWriter(objectMapper))
    }

    class CaseStrategySelectingHttpMessageWriter(baseObjectMapper: ObjectMapper) : HttpMessageWriter<Any> {

        private val camelCaseWriter = EncoderHttpMessageWriter(
            Jackson2JsonEncoder(
                baseObjectMapper.copy()
                    .setPropertyNamingStrategy(
                        com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE
                    )
            )
        )

        private val snakeCaseWriter = EncoderHttpMessageWriter(
            Jackson2JsonEncoder(
                baseObjectMapper.copy()
                    .setPropertyNamingStrategy(
                        com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
                    )
            )
        )

        override fun getWritableMediaTypes(): List<MediaType> {
            return (camelCaseWriter.writableMediaTypes + snakeCaseWriter.writableMediaTypes).distinct()
        }

        override fun canWrite(elementType: ResolvableType, mediaType: MediaType?): Boolean {
            return (camelCaseWriter.canWrite(elementType, mediaType) && snakeCaseWriter.canWrite(elementType, mediaType))
        }

        override fun write(
            inputStream: Publisher<out Any>,
            actualType: ResolvableType,
            elementType: ResolvableType,
            mediaType: MediaType?,
            request: ServerHttpRequest,
            response: ServerHttpResponse,
            hints: MutableMap<String, Any>
        ): Mono<Void> {
            val caseStrategy = CaseStrategy.values()
                .find { it.name == request.headers.getFirst(CASE_STRATEGY_HEADER) }

            val writer = when (caseStrategy) {
                CaseStrategy.SNAKE_CASE -> snakeCaseWriter
                else -> camelCaseWriter
            }

            return writer.write(inputStream, actualType, elementType, mediaType, request, response, hints)
        }

        override fun write(
            inputStream: Publisher<out Any>,
            elementType: ResolvableType,
            mediaType: MediaType?,
            message: ReactiveHttpOutputMessage,
            hints: MutableMap<String, Any>
        ): Mono<Void> {
            val caseStrategy = CaseStrategy.values()
                .find { it.name == message.headers.getFirst(CASE_STRATEGY_HEADER) }

            val writer = when (caseStrategy) {
                CaseStrategy.SNAKE_CASE -> snakeCaseWriter
                else -> camelCaseWriter
            }

            return writer.write(inputStream, elementType, mediaType, message, hints)
        }
    }
}
