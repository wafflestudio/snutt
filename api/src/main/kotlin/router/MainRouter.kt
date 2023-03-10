package com.wafflestudio.snu4t.router

import com.wafflestudio.snu4t.handler.AuthHandler
import com.wafflestudio.snu4t.handler.BookmarkHandler
import com.wafflestudio.snu4t.handler.TimetableHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Component
class MainRouter(
    private val timeTableHandler: TimetableHandler,
    private val bookmarkHandler: BookmarkHandler,
    private val authHandler: AuthHandler,
) {
    @Bean
    fun route(): RouterFunction<ServerResponse> = coRouter {
        GET("/ping") { ServerResponse.ok().bodyValueAndAwait("pong") }

        path("/v1").or("").nest {
            "/tables".nest {
                GET("", timeTableHandler::getBriefs)
            }
            "/auth".nest {
                POST("/register_local", authHandler::registerLocal)
            }
            "/bookmarks".nest {
                GET("", bookmarkHandler::getBookmark)
                POST("/lecture", bookmarkHandler::addLecture)
                DELETE("/lecture", bookmarkHandler::deleteBookmark)
            }
        }
    }
}
