package com.wafflestudio.snutt.router

import com.wafflestudio.snutt.handler.AdminHandler
import com.wafflestudio.snutt.handler.AuthHandler
import com.wafflestudio.snutt.handler.BookmarkHandler
import com.wafflestudio.snutt.handler.BuildingHandler
import com.wafflestudio.snutt.handler.ConfigHandler
import com.wafflestudio.snutt.handler.CoursebookHandler
import com.wafflestudio.snutt.handler.DeviceHandler
import com.wafflestudio.snutt.handler.EvHandler
import com.wafflestudio.snutt.handler.EvServiceHandler
import com.wafflestudio.snutt.handler.FeedbackHandler
import com.wafflestudio.snutt.handler.FriendHandler
import com.wafflestudio.snutt.handler.FriendTableHandler
import com.wafflestudio.snutt.handler.LectureSearchHandler
import com.wafflestudio.snutt.handler.NotificationHandler
import com.wafflestudio.snutt.handler.PopupHandler
import com.wafflestudio.snutt.handler.StaticPageHandler
import com.wafflestudio.snutt.handler.TagHandler
import com.wafflestudio.snutt.handler.TimetableHandler
import com.wafflestudio.snutt.handler.TimetableLectureHandler
import com.wafflestudio.snutt.handler.TimetableThemeHandler
import com.wafflestudio.snutt.handler.UserHandler
import com.wafflestudio.snutt.handler.VacancyNotifcationHandler
import com.wafflestudio.snutt.router.docs.AdminDocs
import com.wafflestudio.snutt.router.docs.AuthDocs
import com.wafflestudio.snutt.router.docs.BookmarkDocs
import com.wafflestudio.snutt.router.docs.BuildingsDocs
import com.wafflestudio.snutt.router.docs.ConfigDocs
import com.wafflestudio.snutt.router.docs.CoursebookDocs
import com.wafflestudio.snutt.router.docs.EvDocs
import com.wafflestudio.snutt.router.docs.FeedbackDocs
import com.wafflestudio.snutt.router.docs.FriendDocs
import com.wafflestudio.snutt.router.docs.LectureSearchDocs
import com.wafflestudio.snutt.router.docs.NotificationDocs
import com.wafflestudio.snutt.router.docs.PopupDocs
import com.wafflestudio.snutt.router.docs.TagDocs
import com.wafflestudio.snutt.router.docs.ThemeDocs
import com.wafflestudio.snutt.router.docs.TimetableDocs
import com.wafflestudio.snutt.router.docs.UserDocs
import com.wafflestudio.snutt.router.docs.VacancyNotificationDocs
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
    private val popupHandler: PopupHandler,
    private val adminHandler: AdminHandler,
    private val buildingHandler: BuildingHandler,
    private val evHandler: EvHandler,
    private val coursebookHandler: CoursebookHandler,
    private val tagHandler: TagHandler,
    private val feedbackHandler: FeedbackHandler,
    private val staticPageHandler: StaticPageHandler,
    private val evServiceHandler: EvServiceHandler,
) {
    @Bean
    fun healthCheck() =
        coRouter {
            GET("/health-check") { ServerResponse.ok().buildAndAwait() }
        }

    @Bean
    @AuthDocs
    fun authRoute() =
        v1CoRouter {
            "/auth".nest {
                POST("/register_local", authHandler::registerLocal)
                POST("/login_local", authHandler::loginLocal)
                POST("/login_fb", authHandler::loginFacebookLegacy)
                POST("/login/facebook", authHandler::loginFacebook)
                POST("/login/google", authHandler::loginGoogle)
                POST("/login/kakao", authHandler::loginKakao)
                POST("/login_apple", authHandler::loginAppleLegacy)
                POST("/login/apple", authHandler::loginApple)
                POST("/logout", userHandler::logout)
                POST("/password/reset/email/check", authHandler::getMaskedEmail)
                POST("/password/reset/email/send", authHandler::sendResetPasswordCode)
                POST("/password/reset/verification/code", authHandler::verifyResetPasswordCode)
                POST("/password/reset", authHandler::resetPassword)
                POST("/id/find", authHandler::findId)
            }
        }

    @Bean
    @UserDocs
    fun userRoute() =
        v1CoRouter {
            "/user".nest {
                GET("/info", userHandler::getUserInfo)
                POST("/device/{id}", deviceHandler::addRegistrationId)
                DELETE("/device/{id}", deviceHandler::removeRegistrationId)
                DELETE("/account", userHandler::deleteAccount)
                POST("/email/verification", userHandler::sendVerificationEmail)
                GET("/email/verification", userHandler::getEmailVerification)
                DELETE("/email/verification", userHandler::resetEmailVerification)
                POST("/email/verification/code", userHandler::confirmEmailVerification)
                POST("/password", userHandler::attachLocal)
                PUT("/password", userHandler::changePassword)
                POST("/facebook", userHandler::attachFacebook)
                POST("/google", userHandler::attachGoogle)
                POST("/kakao", userHandler::attachKakao)
                POST("/apple", userHandler::attachApple)
                DELETE("/facebook", userHandler::detachFacebook)
                DELETE("/google", userHandler::detachGoogle)
                DELETE("/kakao", userHandler::detachKakao)
                DELETE("/apple", userHandler::detachApple)
            }
            "/users".nest {
                GET("/me", userHandler::getUserMe)
                PATCH("/me", userHandler::patchUserInfo)
                GET("/me/social_providers", userHandler::checkAuthProviders)
                GET("/me/auth-providers", userHandler::checkAuthProviders)
            }
        }

    @Bean
    @TimetableDocs
    fun tableRoute() =
        v1CoRouter {
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
    fun lectureRoute() =
        v1CoRouter {
            POST("/search_query", lectureSearchHandler::searchLectures)
        }

    @Bean
    @BuildingsDocs
    fun buildingRoute() =
        v1CoRouter {
            GET("/buildings", buildingHandler::searchBuildings)
        }

    @Bean
    @BookmarkDocs
    fun bookmarkRoute() =
        v1CoRouter {
            "/bookmarks".nest {
                GET("", bookmarkHandler::getBookmarks)
                GET("/lectures/{lectureId}/state", bookmarkHandler::existsBookmarkLecture)
                POST("/lecture", bookmarkHandler::addLecture)
                DELETE("/lecture", bookmarkHandler::deleteBookmark)
            }
        }

    @Bean
    @NotificationDocs
    fun notificationRoute() =
        v1CoRouter {
            "/notification".nest {
                GET("", notificationHandler::getNotifications)
                GET("/count", notificationHandler::getUnreadCounts)
            }
        }

    @Bean
    @AdminDocs
    fun adminRoute() =
        v1CoRouter {
            "/admin".nest {
                POST("/insert_noti", adminHandler::insertNotification)

                POST("/configs/{name}", adminHandler::postConfig)
                GET("/configs/{name}", adminHandler::getConfigs)
                DELETE("/configs/{name}/{id}", adminHandler::deleteConfig)
                PATCH("/configs/{name}/{id}", adminHandler::patchConfig)

                POST("/images/{source}/upload-uris", adminHandler::getUploadSignedUris)

                POST("/popups", adminHandler::postPopup)
                DELETE("/popups/{id}", adminHandler::deletePopup)
            }
        }

    @Bean
    @VacancyNotificationDocs
    fun vacancyNotificationRoute() =
        v1CoRouter {
            "/vacancy-notifications".nest {
                GET("/lectures", vacancyNotificationHandler::getVacancyNotificationLectures)
                GET("/lectures/{lectureId}/state", vacancyNotificationHandler::existsVacancyNotification)
                POST("/lectures/{lectureId}", vacancyNotificationHandler::addVacancyNotification)
                DELETE("/lectures/{lectureId}", vacancyNotificationHandler::deleteVacancyNotification)
            }
        }

    @Bean
    @ConfigDocs
    fun configRoute() =
        v1CoRouter {
            "/configs".nest {
                GET("", configHandler::getConfigs)
            }
        }

    @Bean
    @PopupDocs
    fun popupRoute() =
        v1CoRouter {
            "/popups".nest {
                GET("", popupHandler::getPopups)
            }
        }

    @Bean
    @FriendDocs
    fun friendRoute() =
        v1CoRouter {
            "/friends".nest {
                GET("", friendHandler::getFriends)
                POST("", friendHandler::requestFriend)
                POST("/{friendId}/accept", friendHandler::acceptFriend)
                POST("/{friendId}/decline", friendHandler::declineFriend)
                PATCH("/{friendId}/display-name", friendHandler::updateFriendDisplayName)
                DELETE("/{friendId}", friendHandler::breakFriend)
                GET("/{friendId}/primary-table", friendTableHandler::getPrimaryTable)
                GET("/{friendId}/coursebooks", friendTableHandler::getCoursebooks)
                GET("/generate-link", friendHandler::generateFriendLink)
                POST("/accept-link/{requestToken}", friendHandler::acceptFriendByLink)
                GET("/{friendId}/registered-course-books", friendTableHandler::getCoursebooks) // TODO: delete
            }
        }

    @Bean
    @ThemeDocs
    fun timetableThemeRoute() =
        v1CoRouter {
            "/themes".nest {
                GET("", timetableThemeHandler::getThemes)
                GET("/best", timetableThemeHandler::getBestThemes)
                GET("/friends", timetableThemeHandler::getFriendsThemes)
                POST("", timetableThemeHandler::addTheme)
                GET("/{themeId}", timetableThemeHandler::getTheme)
                PATCH("/{themeId}", timetableThemeHandler::modifyTheme)
                DELETE("/{themeId}", timetableThemeHandler::deleteTheme)
                POST("/{themeId}/copy", timetableThemeHandler::copyTheme)
                POST("/{themeId}/default", timetableThemeHandler::setDefault)
                POST("/{themeId}/publish", timetableThemeHandler::publishTheme)
                POST("/{themeId}/download", timetableThemeHandler::downloadTheme)
                POST("/search", timetableThemeHandler::searchThemes)
                POST("/basic/{basicThemeTypeValue}/default", timetableThemeHandler::setBasicThemeTypeDefault)
                DELETE("/{themeId}/default", timetableThemeHandler::unsetDefault)
                DELETE("/basic/{basicThemeTypeValue}/default", timetableThemeHandler::unsetBasicThemeTypeDefault)
            }
        }

    @Bean
    @TagDocs
    fun tagRoute() =
        v1CoRouter {
            "/tags".nest {
                GET("/{year}/{semester}", tagHandler::getTagList)
                GET("/{year}/{semester}/update_time", tagHandler::getTagListUpdateTime)
            }
        }

    @Bean
    @EvDocs
    fun evRouter() =
        v1CoRouter {
            GET("/ev/lectures/{lectureId}/summary", evHandler::getLectureEvaluationSummary)
        }

    @Bean
    fun evServiceRouter() =
        v1CoRouter {
            GET("/ev-service/v1/users/me/lectures/latest", evServiceHandler::getMyLatestLectures)
            GET("/ev-service/{*requestPath}", evServiceHandler::handleGet)
            POST("/ev-service/{*requestPath}", evServiceHandler::handlePost)
            DELETE("/ev-service/{*requestPath}", evServiceHandler::handleDelete)
            PATCH("/ev-service/{*requestPath}", evServiceHandler::handlePatch)
        }

    @Bean
    @CoursebookDocs
    fun coursebookRouter() =
        v1CoRouter {
            "/course_books".nest {
                GET("", coursebookHandler::getCoursebooks)
                GET("/recent", coursebookHandler::getLatestCoursebook)
                GET("/official", coursebookHandler::getCoursebookOfficial)
            }
        }

    @Bean
    @FeedbackDocs
    fun feedbackRouter() =
        v1CoRouter {
            "/feedback".nest {
                POST("", feedbackHandler::postFeedback)
            }
        }

    private fun v1CoRouter(r: CoRouterFunctionDsl.() -> Unit) =
        coRouter {
            path("/v1").or("").nest(r)
        }

    @Bean
    fun staticPageRouter() =
        coRouter {
            GET("/member").invoke { staticPageHandler.member() }
            GET("/privacy_policy").invoke { staticPageHandler.privacyPolicy() }
            GET("/terms_of_service").invoke { staticPageHandler.termsOfService() }
        }
}
