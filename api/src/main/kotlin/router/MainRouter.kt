package com.wafflestudio.snu4t.router

import com.wafflestudio.snu4t.handler.AdminHandler
import com.wafflestudio.snu4t.handler.AuthHandler
import com.wafflestudio.snu4t.handler.BookmarkHandler
import com.wafflestudio.snu4t.handler.ConfigHandler
import com.wafflestudio.snu4t.handler.DeviceHandler
import com.wafflestudio.snu4t.handler.FriendHandler
import com.wafflestudio.snu4t.handler.FriendTableHandler
import com.wafflestudio.snu4t.handler.LectureSearchHandler
import com.wafflestudio.snu4t.handler.NotificationHandler
import com.wafflestudio.snu4t.handler.TimetableHandler
import com.wafflestudio.snu4t.handler.TimetableLectureHandler
import com.wafflestudio.snu4t.handler.TimetableThemeHandler
import com.wafflestudio.snu4t.handler.UserHandler
import com.wafflestudio.snu4t.handler.VacancyNotifcationHandler
import com.wafflestudio.snu4t.router.docs.AdminDocs
import com.wafflestudio.snu4t.router.docs.AuthDocs
import com.wafflestudio.snu4t.router.docs.BookmarkDocs
import com.wafflestudio.snu4t.router.docs.ConfigDocs
import com.wafflestudio.snu4t.router.docs.FriendDocs
import com.wafflestudio.snu4t.router.docs.LectureSearchDocs
import com.wafflestudio.snu4t.router.docs.NotificationDocs
import com.wafflestudio.snu4t.router.docs.ThemeDocs
import com.wafflestudio.snu4t.router.docs.TimetableDocs
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
    private val userHandler: UserHandler,
    private val authHandler: AuthHandler,
    private val deviceHandler: DeviceHandler,
    private val notificationHandler: NotificationHandler,
    private val vacancyNotificationHandler: VacancyNotifcationHandler,
    private val timeTableHandler: TimetableHandler,
    private val timeTableLectureHandler: TimetableLectureHandler,
    private val timetableThemeHandler: TimetableThemeHandler,
    private val bookmarkHandler: BookmarkHandler,
    private val lectureSearchHandler: LectureSearchHandler,
    private val friendHandler: FriendHandler,
    private val friendTableHandler: FriendTableHandler,
    private val configHandler: ConfigHandler,
    private val adminHandler: AdminHandler,
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
            GET("/info", userHandler::getUserInfo)
            POST("/device/{id}", deviceHandler::addRegistrationId)
            DELETE("/device/{id}", deviceHandler::removeRegistrationId)
            DELETE("/account", userHandler::deleteAccount)
        }
        "/users".nest {
            GET("/me", userHandler::getUserMe)
            PATCH("/me", userHandler::patchUserInfo)
        }
    }

    @Bean
    @TimetableDocs
    fun tableRoute() = v1CoRouter {
        "/tables".nest {
            GET("", timeTableHandler::getTimetableBriefs)
            GET("/recent", timeTableHandler::getMostRecentlyUpdatedTimetables)
            GET("/{year}/{semester}", timeTableHandler::getTimetablesBySemester)
            POST("", timeTableHandler::addTimetable)
            GET("/{timetableId}", timeTableHandler::getTimetable)
            PUT("/{timetableId}", timeTableHandler::modifyTimetable)
            DELETE("/{timetableId}", timeTableHandler::deleteTimetable)
            POST("/{timetableId}/copy", timeTableHandler::copyTimetable)
            PUT("/{timetableId}/theme", timeTableHandler::modifyTimetableTheme)
            POST("/{timetableId}/primary", timeTableHandler::setPrimary)
            DELETE("/{timetableId}/primary", timeTableHandler::unSetPrimary)
            "{timetableId}/lecture".nest {
                POST("", timeTableLectureHandler::addCustomLecture)
                POST("/{lectureId}", timeTableLectureHandler::addLecture)
                PUT("/{timetableLectureId}/reset", timeTableLectureHandler::resetTimetableLecture)
                PUT("/{timetableLectureId}", timeTableLectureHandler::modifyTimetableLecture)
                DELETE("/{timetableLectureId}", timeTableLectureHandler::deleteTimetableLecture)
            }
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
            GET("/lectures/{lectureId}/state", bookmarkHandler::existsBookmarkLecture)
            POST("/lecture", bookmarkHandler::addLecture)
            DELETE("/lecture", bookmarkHandler::deleteBookmark)
        }
    }

    @Bean
    @NotificationDocs
    fun notificationRoute() = v1CoRouter {
        "/notification".nest {
            GET("", notificationHandler::getNotifications)
            GET("/count", notificationHandler::getUnreadCounts)
        }
    }

    @Bean
    @AdminDocs
    fun adminRoute() = v1CoRouter {
        "/admin".nest {
            POST("/insert_noti", adminHandler::insertNotification)

            POST("/configs/{name}", adminHandler::postConfig)
            GET("/configs/{name}", adminHandler::getConfigs)
            DELETE("/configs/{name}/{id}", adminHandler::deleteConfig)
            PATCH("/configs/{name}/{id}", adminHandler::patchConfig)
        }
    }

    @Bean
    @VacancyNotificationDocs
    fun vacancyNotificationRoute() = v1CoRouter {
        "/vacancy-notifications".nest {
            GET("/lectures", vacancyNotificationHandler::getVacancyNotificationLectures)
            GET("/lectures/{lectureId}/state", vacancyNotificationHandler::existsVacancyNotification)
            POST("/lectures/{lectureId}", vacancyNotificationHandler::addVacancyNotification)
            DELETE("/lectures/{lectureId}", vacancyNotificationHandler::deleteVacancyNotification)
        }
    }

    @Bean
    @ConfigDocs
    fun configRoute() = v1CoRouter {
        "/configs".nest {
            GET("", configHandler::getConfigs)
        }
    }

    @Bean
    @FriendDocs
    fun friendRoute() = v1CoRouter {
        "/friends".nest {
            GET("", friendHandler::getFriends)
            POST("", friendHandler::requestFriend)
            POST("/{friendId}/accept", friendHandler::acceptFriend)
            POST("/{friendId}/decline", friendHandler::declineFriend)
            PATCH("/{friendId}/display-name", friendHandler::updateFriendDisplayName)
            DELETE("/{friendId}", friendHandler::breakFriend)
            GET("/{friendId}/primary-table", friendTableHandler::getPrimaryTable)
            GET("/{friendId}/coursebooks", friendTableHandler::getCoursebooks)
            GET("/{friendId}/registered-course-books", friendTableHandler::getCoursebooks) // TODO: delete
        }
    }

    @Bean
    @ThemeDocs
    fun timetableThemeRoute() = v1CoRouter {
        "/themes".nest {
            GET("", timetableThemeHandler::getThemes)
            POST("", timetableThemeHandler::addTheme)
            PATCH("{themeId}", timetableThemeHandler::modifyTheme)
            DELETE("{themeId}", timetableThemeHandler::deleteTheme)
            POST("{themeId}/copy", timetableThemeHandler::copyTheme)
            POST("{themeId}/default", timetableThemeHandler::setDefault)
            POST("basic/{basicThemeTypeValue}/default", timetableThemeHandler::setBasicThemeTypeDefault)
            DELETE("{themeId}/default", timetableThemeHandler::unsetDefault)
            DELETE("basic/{basicThemeTypeValue}/default", timetableThemeHandler::unsetBasicThemeTypeDefault)
        }
    }

    private fun v1CoRouter(r: CoRouterFunctionDsl.() -> Unit) = coRouter {
        path("/v1").or("").nest(r)
    }
}
