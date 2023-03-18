package com.wafflestudio.snu4t.router

import com.wafflestudio.snu4t.handler.AuthHandler
import com.wafflestudio.snu4t.handler.BookmarkHandler
import com.wafflestudio.snu4t.handler.TimetableHandler
import com.wafflestudio.snu4t.router.docs.AuthDocs
import com.wafflestudio.snu4t.router.docs.BookmarkDocs
import com.wafflestudio.snu4t.router.docs.TableDocs
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.CoRouterFunctionDsl
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
    fun ping() = coRouter {
        GET("/ping") { ServerResponse.ok().bodyValueAndAwait("pong") }
    }

    @Bean
    @AuthDocs
    fun authRoute() = v1CoRouter(prefix = "/auth") {
        POST("/register_local", authHandler::registerLocal)
    }

    @Bean
    @TableDocs
    fun tableRoute() = v1CoRouter(prefix = "/tables") {
        GET("", timeTableHandler::getBriefs)
    }

    @Bean
    @BookmarkDocs
    fun bookmarkRoute() = v1CoRouter(prefix = "/bookmarks") {
        GET("", bookmarkHandler::getBookmark)
        POST("/lecture", bookmarkHandler::addLecture)
        DELETE("/lecture", bookmarkHandler::deleteBookmark)
    }

    private fun v1CoRouter(prefix: String = "", r: CoRouterFunctionDsl.() -> Unit) = coRouter {
        path("/v1").or("").nest {
            prefix.nest(r)
        }
    }
}
