package com.wafflestudio.snu4t.middleware

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CompositeMiddlewareConfig {
    @Bean("snuttRestApiDefaultMiddleware")
    fun snuttRestApiDefaultMiddleware(
        nativeClientInfoMiddleware: NativeClientInfoMiddleware,
        userAuthorizeMiddleware: UserAuthorizeMiddleware,
    ): Middleware = userAuthorizeMiddleware + nativeClientInfoMiddleware
}
