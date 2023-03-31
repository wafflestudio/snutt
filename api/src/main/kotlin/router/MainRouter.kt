package com.wafflestudio.snu4t.router

import com.wafflestudio.snu4t.handler.*
import com.wafflestudio.snu4t.router.docs.*
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
    private val sharedTimetableHandler: SharedTimetableHandler,
    private val notificationHandler: NotificationHandler,
) {
    @Bean
    fun ping() = coRouter {
        GET("/ping") { ServerResponse.ok().bodyValueAndAwait("pong") }
    }

    @Bean
    @AuthDocs
    fun authRoute() = v1CoRouter {
        "/auth".nest {
            POST("/register_local", authHandler::registerLocal)
        }
    }

    @Bean
    @TableDocs
    fun tableRoute() = v1CoRouter {
        "/tables".nest {
            GET("", timeTableHandler::getBriefs)
        }
    }

    @Bean
    @BookmarkDocs
    fun bookmarkRoute() = v1CoRouter {
        "/bookmarks".nest {
            GET("", bookmarkHandler::getBookmark)
            POST("/lecture", bookmarkHandler::addLecture)
            DELETE("/lecture", bookmarkHandler::deleteBookmark)
        }
    }

    @Bean
    @SharedTimetableDocs
    fun sharedTimetableRoute() = v1CoRouter {
        "/shared-tables".nest {
            GET("", sharedTimetableHandler::getSharedTimetables)
            GET("/{id}", sharedTimetableHandler::getSharedTimetable)
            POST("", sharedTimetableHandler::addSharedTimetable)
            PUT("/{id}", sharedTimetableHandler::updateSharedTimetable)
            DELETE("/{id}", sharedTimetableHandler::deleteSharedTimetable)
        }
    }

    @NotificationApi
    fun notificationRoute() = v1CoRouter {
        "/notification".nest {
            GET("", notificationHandler::getNotification)
            GET("/count", notificationHandler::getUnreadCounts)
        }
    }

    private fun v1CoRouter(r: CoRouterFunctionDsl.() -> Unit) = coRouter {
        path("/v1").or("").nest(r)
    }
}
