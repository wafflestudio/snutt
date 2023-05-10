package com.wafflestudio.snu4t.router

import com.wafflestudio.snu4t.handler.AdminHandler
import com.wafflestudio.snu4t.handler.AuthHandler
import com.wafflestudio.snu4t.handler.BookmarkHandler
import com.wafflestudio.snu4t.handler.LectureHandler
import com.wafflestudio.snu4t.handler.NotificationHandler
import com.wafflestudio.snu4t.handler.SharedTimetableHandler
import com.wafflestudio.snu4t.handler.TimetableHandler
import com.wafflestudio.snu4t.router.docs.AdminApi
import com.wafflestudio.snu4t.router.docs.AuthDocs
import com.wafflestudio.snu4t.router.docs.BookmarkDocs
import com.wafflestudio.snu4t.router.docs.LectureDocs
import com.wafflestudio.snu4t.router.docs.NotificationApi
import com.wafflestudio.snu4t.router.docs.SharedTimetableDocs
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
    private val adminHandler: AdminHandler,
    private val sharedTimetableHandler: SharedTimetableHandler,
    private val notificationHandler: NotificationHandler,
    private val lectureHandler: LectureHandler,
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
            POST("/login_local", authHandler::loginLocal)
        }
    }

    @Bean
    @TableDocs
    fun tableRoute() = v1CoRouter {
        "/tables".nest {
            GET("", timeTableHandler::getBriefs)
            GET("/{id}/links", timeTableHandler::getLink)
        }
    }

    @Bean
    @LectureDocs
    fun lectureRoute() = v1CoRouter {
        POST("/search_query", lectureHandler::searchLectures)
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
            POST("/{id}/copy", sharedTimetableHandler::copySharedTimetable)
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

    @AdminApi
    fun adminRoute() = v1CoRouter {
        "/admin".nest {
            POST("/insert_noti", adminHandler::insertNotification)
        }
    }

    private fun v1CoRouter(r: CoRouterFunctionDsl.() -> Unit) = coRouter {
        path("/v1").or("").nest(r)
    }
}
