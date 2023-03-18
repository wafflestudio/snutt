package com.wafflestudio.snu4t.router

import com.wafflestudio.snu4t.handler.*
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
    private val sharedTimetableHandler: SharedTimetableHandler,
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
            "/shared_timetables".nest {
                GET("", sharedTimetableHandler::getSharedTimetables)
                GET("/{id}", sharedTimetableHandler::getSharedTimetable)
                POST("", sharedTimetableHandler::addSharedTimetable)
                PUT("/{id}", sharedTimetableHandler::updateSharedTimetable)
                DELETE("/{id}", sharedTimetableHandler::deleteSharedTimetable)
            }
        }
    }
}
