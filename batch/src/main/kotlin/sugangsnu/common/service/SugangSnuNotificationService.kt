package com.wafflestudio.snu4t.sugangsnu.common.service

import com.wafflestudio.snu4t.common.push.UrlScheme
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.config.Phase
import com.wafflestudio.snu4t.coursebook.data.Coursebook
import com.wafflestudio.snu4t.notification.data.NotificationType
import com.wafflestudio.snu4t.notification.service.NotificationService
import com.wafflestudio.snu4t.sugangsnu.common.utils.toKoreanFieldName
import com.wafflestudio.snu4t.sugangsnu.job.sync.data.BookmarkLectureDeleteResult
import com.wafflestudio.snu4t.sugangsnu.job.sync.data.BookmarkLectureUpdateResult
import com.wafflestudio.snu4t.sugangsnu.job.sync.data.TimetableLectureDeleteByOverlapResult
import com.wafflestudio.snu4t.sugangsnu.job.sync.data.TimetableLectureDeleteResult
import com.wafflestudio.snu4t.sugangsnu.job.sync.data.TimetableLectureUpdateResult
import com.wafflestudio.snu4t.sugangsnu.job.sync.data.UserLectureSyncResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Service

interface SugangSnuNotificationService {
    suspend fun notifyUserLectureChanges(syncSavedLecturesResults: List<UserLectureSyncResult>)
    suspend fun notifyCoursebookUpdate(coursebook: Coursebook)
}

@Service
class SugangSnuNotificationServiceImpl(
    private val notificationService: NotificationService,
    private val phase: Phase,
) : SugangSnuNotificationService {
    override suspend fun notifyUserLectureChanges(syncSavedLecturesResults: List<UserLectureSyncResult>) = supervisorScope {
        getPushMessagesForUsers(syncSavedLecturesResults).forEach { (userId, pushMessage) ->
            launch { notificationService.sendPush(userId, pushMessage) }
        }
    }

    private suspend fun getPushMessagesForUsers(userLectureSyncResults: List<UserLectureSyncResult>): Map<String, PushMessage> {
        val notificationScheme = UrlScheme.NOTIFICATIONS.compileWith(phase)

        val userUpdatedLectureCountMap =
            userLectureSyncResults.filterIsInstance<TimetableLectureUpdateResult>().toCountMap()
        val userDeletedLectureCountMap =
            userLectureSyncResults.filter { it is TimetableLectureDeleteResult || it is TimetableLectureDeleteByOverlapResult }
                .toCountMap()

        val allUserIds = userUpdatedLectureCountMap.keys + userDeletedLectureCountMap.keys

        val userIdToMessageBody = allUserIds.associateWith { userId ->
            val updatedCount = userUpdatedLectureCountMap[userId]
            val deletedCount = userDeletedLectureCountMap[userId]

            when {
                updatedCount != null && deletedCount != null -> {
                    "수강편람이 업데이트되어 ${updatedCount}개 강의가 변경되고 ${deletedCount}개 강의가 삭제되었습니다."
                }
                updatedCount != null -> {
                    "수강편람이 업데이트되어 ${updatedCount}개 강의가 변경되었습니다."
                }
                deletedCount != null -> {
                    "수강편람이 업데이트되어 ${deletedCount}개 강의가 삭제되었습니다."
                }
                else -> {
                    error("This should not happen")
                }
            }
        }

        return userLectureSyncResults.associate { syncResult ->
            val userId = syncResult.userId
            val (detailMessage, notificationType) = syncResult.toDetailMessageAndNotificationType()

            userId to PushMessage(
                title = "수강편람 업데이트",
                body = userIdToMessageBody[userId]!!,
                type = notificationType,
                urlScheme = notificationScheme,
                detailMessage = detailMessage,
            )
        }
    }

    override suspend fun notifyCoursebookUpdate(coursebook: Coursebook) {
        val messageBody = "${coursebook.year}년도 ${coursebook.semester.fullName} 수강편람이 추가되었습니다."
        notificationService.sendGlobalPush(
            PushMessage(
                title = "신규 수강편람",
                body = messageBody,
                type = NotificationType.COURSEBOOK,
            )
        )
    }

    private fun List<UserLectureSyncResult>.toCountMap() =
        this.map { result -> result.userId to result.lectureId }.distinct().groupingBy { it.first }.eachCount()

    private fun UserLectureSyncResult.toDetailMessageAndNotificationType(): Pair<String, NotificationType> {
        return when (this) {
            // 업데이트 알림
            is TimetableLectureUpdateResult -> {
                """
                   $year-${semester.fullName} '$timetableTitle' 시간표의
                   '$courseTitle' 강의가 업데이트 되었습니다.
                   (항목: ${updatedFields.map { field -> field.toKoreanFieldName() }.distinct().joinToString()})
                """.trimIndent().replace("\n", "") to NotificationType.LECTURE_UPDATE
            }
            is BookmarkLectureUpdateResult -> {
                """
                $year-${semester.fullName} 관심강좌 목록의 '$courseTitle' 강의가 업데이트 되었습니다.
                (항목: ${updatedFields.map { field -> field.toKoreanFieldName() }.distinct().joinToString()})
                """.trimIndent().replace("\n", "") to NotificationType.LECTURE_UPDATE
            }
            // 폐강 알림
            is TimetableLectureDeleteResult -> {
                """
                $year-${semester.fullName} '$timetableTitle' 시간표의 
                '$courseTitle' 강의가 폐강되어 삭제되었습니다.
                """.trimIndent().replace("\n", "") to NotificationType.LECTURE_REMOVE
            }
            is BookmarkLectureDeleteResult -> {
                """
                $year-${semester.fullName} 관심강좌 목록의 
                '$courseTitle' 강의가 폐강되어 삭제되었습니다.
                """.trimIndent().replace("\n", "") to NotificationType.LECTURE_REMOVE
            }
            is TimetableLectureDeleteByOverlapResult -> {
                """
                $year-${semester.fullName} '$timetableTitle' 시간표의 
                '$courseTitle' 강의가 업데이트되었으나, 시간표의 다른 강의와 겹쳐 삭제되었습니다.
                """.trimIndent().replace("\n", "") to NotificationType.LECTURE_REMOVE
            }
        }
    }
}
