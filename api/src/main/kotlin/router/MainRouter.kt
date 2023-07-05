package com.wafflestudio.snu4t.router

import com.wafflestudio.snu4t.handler.AdminHandler
import com.wafflestudio.snu4t.handler.AuthHandler
import com.wafflestudio.snu4t.handler.BookmarkHandler
import com.wafflestudio.snu4t.handler.DeviceHandler
import com.wafflestudio.snu4t.handler.LectureSearchHandler
import com.wafflestudio.snu4t.handler.NotificationHandler
import com.wafflestudio.snu4t.handler.SharedTimetableHandler
import com.wafflestudio.snu4t.handler.TimetableHandler
import com.wafflestudio.snu4t.handler.VacancyNotifcationHandler
import com.wafflestudio.snu4t.router.docs.AdminDocs
import com.wafflestudio.snu4t.router.docs.AuthDocs
import com.wafflestudio.snu4t.router.docs.BookmarkDocs
import com.wafflestudio.snu4t.router.docs.LectureSearchDocs
import com.wafflestudio.snu4t.router.docs.NotificationDocs
import com.wafflestudio.snu4t.router.docs.SharedTimetableDocs
import com.wafflestudio.snu4t.router.docs.TableDocs
import com.wafflestudio.snu4t.router.docs.UserDocs
import com.wafflestudio.snu4t.router.docs.VacancyNotificationDocs
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.CoRouterFunctionDsl
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Component
class MainRouter(
    private val timeTableHandler: TimetableHandler,
    private val bookmarkHandler: BookmarkHandler,
    private val authHandler: AuthHandler,
    private val adminHandler: AdminHandler,
    private val deviceHandler: DeviceHandler,
    private val sharedTimetableHandler: SharedTimetableHandler,
    private val notificationHandler: NotificationHandler,
    private val lectureSearchHandler: LectureSearchHandler,
    private val vacancyNotificationHandler: VacancyNotifcationHandler,
) {
    @Bean
    fun healthCheck() = coRouter {
        GET("/health-check") { ServerResponse.ok().buildAndAwait() }
    }

    @Bean
    @AuthDocs
    fun authRoute() = v1CoRouter {
        "/auth".nest {
            POST("/register_local", authHandler::registerLocal)
            POST("/login_local", authHandler::loginLocal)
            POST("/logout", authHandler::logout)
        }
    }

    @Bean
    @UserDocs
    fun userRoute() = v1CoRouter {
        "/user".nest {
            POST("/device/{id}", deviceHandler::addRegistrationId)
            DELETE("/device/{id}", deviceHandler::removeRegistrationId)
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
    @LectureSearchDocs
    fun lectureRoute() = v1CoRouter {
        POST("/search_query", lectureSearchHandler::searchLectures)
    }

    @Bean
    @BookmarkDocs
    fun bookmarkRoute() = v1CoRouter {
        "/bookmarks".nest {
            GET("", bookmarkHandler::getBookmarks)
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

    @NotificationDocs
    fun notificationRoute() = v1CoRouter {
        "/notification".nest {
            GET("", notificationHandler::getNotifications)
            GET("/count", notificationHandler::getUnreadCounts)
        }
    }

    @AdminDocs
    fun adminRoute() = v1CoRouter {
        "/admin".nest {
            POST("/insert_noti", adminHandler::insertNotification)
        }
    }

    @Bean
    @VacancyNotificationDocs
    fun vacancyNotificationRoute() = coRouter {
        path("/v1").nest {
            "/vacancy-notifications".nest {
                GET("", vacancyNotificationHandler::getVacancyNotifications)
                GET("/lectures/{lectureId}", vacancyNotificationHandler::getVacancyNotification)
                POST("/lectures/{lectureId}", vacancyNotificationHandler::addVacancyNotification)
                DELETE("/{id}", vacancyNotificationHandler::deleteVacancyNotification)
            }
        }
    }

    private fun v1CoRouter(r: CoRouterFunctionDsl.() -> Unit) = coRouter {
        path("/v1").or("").nest(r)
    }
}
