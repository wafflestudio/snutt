package com.wafflestudio.snutt.filter

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class HandlerAnnotationResolver(
    private val handlerMapping: RequestMappingHandlerMapping,
) {
    fun isFilterTarget(
        exchange: ServerWebExchange,
        annotationType: Class<out Annotation>,
    ): Mono<Boolean> =
        handlerMapping
            .getHandler(exchange)
            .map { handler ->
                if (handler is HandlerMethod) {
                    AnnotatedElementUtils.hasAnnotation(handler.method, annotationType) ||
                        AnnotatedElementUtils.hasAnnotation(handler.beanType, annotationType)
                } else {
                    false
                }
            }.defaultIfEmpty(false)
}
