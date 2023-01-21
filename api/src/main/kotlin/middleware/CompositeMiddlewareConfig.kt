package com.wafflestudio.snu4t.middleware

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class SnuttRestApiDefaultMiddleware(middleware: Middleware) : Middleware by middleware
class SnuttRestApiNoAuthMiddleware(middleware: Middleware) : Middleware by middleware

@Configuration
class CompositeMiddlewareConfig {
    @Bean
    fun snuttRestApiDefaultMiddleware(
        apiKeyMiddleware: ApiKeyMiddleware,
        userAuthorizeMiddleware: UserAuthorizeMiddleware,
        nativeClientInfoMiddleware: NativeClientInfoMiddleware,
    ): SnuttRestApiDefaultMiddleware =
        SnuttRestApiDefaultMiddleware(apiKeyMiddleware + userAuthorizeMiddleware + nativeClientInfoMiddleware)

    @Bean
    fun snuttRestApiNoAuthMiddleware(
        apiKeyMiddleware: ApiKeyMiddleware,
        nativeClientInfoMiddleware: NativeClientInfoMiddleware,
    ): SnuttRestApiNoAuthMiddleware = SnuttRestApiNoAuthMiddleware(apiKeyMiddleware + nativeClientInfoMiddleware)
}
