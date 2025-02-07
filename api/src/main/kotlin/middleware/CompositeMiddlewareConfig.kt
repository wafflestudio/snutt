package com.wafflestudio.snutt.middleware

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class SnuttRestApiDefaultMiddleware(middleware: Middleware) : Middleware by middleware

class SnuttRestAdminApiMiddleware(middleware: Middleware) : Middleware by middleware

class SnuttRestApiNoAuthMiddleware(middleware: Middleware) : Middleware by middleware

@Configuration
class CompositeMiddlewareConfig(
    private val apiKeyMiddleware: ApiKeyMiddleware,
    private val userAuthorizeMiddleware: UserAuthorizeMiddleware,
    private val adminApiMiddleware: AdminApiMiddleware,
    private val nativeClientInfoMiddleware: NativeClientInfoMiddleware,
) {
    @Bean
    fun snuttRestApiDefaultMiddleware(): SnuttRestApiDefaultMiddleware =
        SnuttRestApiDefaultMiddleware(apiKeyMiddleware + userAuthorizeMiddleware + nativeClientInfoMiddleware)

    @Bean
    fun snuttRestApiNoAuthMiddleware(): SnuttRestApiNoAuthMiddleware =
        SnuttRestApiNoAuthMiddleware(apiKeyMiddleware + nativeClientInfoMiddleware)

    @Bean
    fun snuttRestAdminApiMiddleware(): SnuttRestAdminApiMiddleware = SnuttRestAdminApiMiddleware(apiKeyMiddleware + adminApiMiddleware)
}
