package com.wafflestudio.snutt.sugangsnu.common.service

import com.wafflestudio.snutt.common.push.DeeplinkType
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.coursebook.data.Coursebook
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.notification.data.Notification
import com.wafflestudio.snutt.notification.data.NotificationType
import com.wafflestudio.snutt.notification.data.PushPreferenceType
import com.wafflestudio.snutt.notification.service.NotificationService
import com.wafflestudio.snutt.notification.service.PushService
import com.wafflestudio.snutt.notification.service.PushWithNotificationService
import com.wafflestudio.snutt.sugangsnu.common.utils.toKoreanFieldName
import com.wafflestudio.snutt.sugangsnu.job.sync.data.BookmarkLectureDeleteResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.BookmarkLectureUpdateResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.TimetableLectureDeleteByOverlapResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.TimetableLectureDeleteResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.TimetableLectureSyncResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.TimetableLectureUpdateResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.UserLectureSyncResult
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

interface SugangSnuNotificationService {
    suspend fun notifyUserLectureChanges(userLectureSyncResults: List<UserLectureSyncResult>)

    suspend fun notifyCoursebookUpdate(coursebook: Coursebook)
}

@Service
class SugangSnuNotificationServiceImpl(
    private val pushWithNotificationService: PushWithNotificationService,
    private val notificationService: NotificationService,
    private val pushService: PushService,
) : SugangSnuNotificationService {
    // equalsMetadata 에 없어 단독으로는 변경 판정을 만들지 못하는 필드들.
    // 빈자리 알림 job이 수시로 갱신하므로 _en 변경에 섞여 들어와 아래 억제 판정을 무너뜨리지 않도록 함께 무시한다.
    private val nonNotifyingFieldNames = setOf(Lecture::registrationCount.name, Lecture::wasFull.name)

    override suspend fun notifyUserLectureChanges(userLectureSyncResults: List<UserLectureSyncResult>): Unit =
        coroutineScope {
            // 스냅샷 동기화(_en 복사)는 sync 단계에서 이미 반영됐다. 여기서는 알릴 만한 변경이 없는 업데이트를
            // 알림/푸시 대상에서 제외한다. (영문 데이터 최초 채움 sync에서 전 유저 대량 알림 방지)
            val notifiableResults = userLectureSyncResults.filter { it.isNotifiable() }
            val notifications = notifiableResults.map { it.toNotification() }
            notificationService.sendNotifications(notifications)
            sendPushForTimetable(notifiableResults.filterIsInstance<TimetableLectureSyncResult>())
        }

    /**
     * 한국어 알림 문구로 의미가 있는 변경이 하나라도 있으면 발송 대상.
     * 영문(_en) 필드와 nonNotifyingFieldNames 만 바뀐 경우는 제외한다.
     * 폐강 등 업데이트가 아닌 결과는 항상 발송한다.
     */
    private fun UserLectureSyncResult.isNotifiable(): Boolean {
        val updatedFields =
            when (this) {
                is TimetableLectureUpdateResult -> updatedFields
                is BookmarkLectureUpdateResult -> updatedFields
                else -> return true
            }
        return updatedFields.any { it.name !in nonNotifyingFieldNames && !it.name.endsWith("En") }
    }

    private suspend fun sendPushForTimetable(userLectureSyncResults: List<TimetableLectureSyncResult>) =
        coroutineScope {
            val userUpdatedLectureCountMap =
                userLectureSyncResults.filterIsInstance<TimetableLectureUpdateResult>().toCountMap()
            val userDeletedLectureCountMap =
                userLectureSyncResults
                    .filter { it is TimetableLectureDeleteResult || it is TimetableLectureDeleteByOverlapResult }
                    .toCountMap()

            val allUserIds = userUpdatedLectureCountMap.keys + userDeletedLectureCountMap.keys

            val userIdToMessage =
                allUserIds.associateWith { userId ->
                    val updatedCount = userUpdatedLectureCountMap[userId]
                    val deletedCount = userDeletedLectureCountMap[userId]

                    val messageBody =
                        when {
                            updatedCount != null && deletedCount != null -> {
                                "강의 ${updatedCount}개가 변경, ${deletedCount}개가 삭제되었습니다. 알림함에서 자세히 확인하세요."
                            }

                            updatedCount != null -> {
                                "강의 ${updatedCount}개가 변경되었습니다. 알림함에서 자세히 확인하세요."
                            }

                            deletedCount != null -> {
                                "강의 ${deletedCount}개가 삭제되었습니다. 알림함에서 자세히 확인하세요."
                            }

                            else -> {
                                error("This should not happen")
                            }
                        }
                    PushMessage(
                        title = "수강편람 업데이트",
                        body = messageBody,
                        urlScheme = DeeplinkType.NOTIFICATIONS.build(),
                    )
                }
            pushService.sendTargetPushes(userIdToMessage, PushPreferenceType.LECTURE_UPDATE)
        }

    override suspend fun notifyCoursebookUpdate(coursebook: Coursebook) {
        val messageBody = "${coursebook.year}년도 ${coursebook.semester.fullName} 수강편람이 추가되었습니다."

        pushWithNotificationService.sendGlobalPushAndNotification(
            PushMessage(title = "신규 수강편람", body = messageBody),
            NotificationType.COURSEBOOK,
        )
    }

    private fun List<UserLectureSyncResult>.toCountMap() =
        this
            .map { result -> result.userId to result.lectureId }
            .distinct()
            .groupingBy { it.first }
            .eachCount()

    private fun UserLectureSyncResult.toNotification(): Notification {
        val (message, notificationType, deeplink) =
            when (this) {
                // 업데이트 알림
                is TimetableLectureUpdateResult -> {
                    Triple(
                        """
                        $year-${semester.fullName} '$timetableTitle' 시간표의 
                        '$courseTitle' 강의가 업데이트 되었습니다.
                        (항목: ${updatedFields.map { field -> field.toKoreanFieldName() }.distinct().joinToString()})
                        """.trimIndent().replace("\n", ""),
                        NotificationType.LECTURE_UPDATE,
                        DeeplinkType.TIMETABLE_LECTURE.build(timetableId, lectureId),
                    )
                }

                is BookmarkLectureUpdateResult -> {
                    Triple(
                        """
                        $year-${semester.fullName} 관심강좌 목록의 '$courseTitle' 강의가 업데이트 되었습니다.
                        (항목: ${updatedFields.map { field -> field.toKoreanFieldName() }.distinct().joinToString()})
                        """.trimIndent().replace("\n", ""),
                        NotificationType.LECTURE_UPDATE,
                        DeeplinkType.BOOKMARKS.build(year, semester, lectureId),
                    )
                }

                // 폐강 알림
                is TimetableLectureDeleteResult -> {
                    Triple(
                        """
                        $year-${semester.fullName} '$timetableTitle' 시간표의 
                        '$courseTitle' 강의가 폐강되어 삭제되었습니다.
                        """.trimIndent().replace("\n", ""),
                        NotificationType.LECTURE_REMOVE,
                        DeeplinkType.NOTIFICATIONS.build(),
                    )
                }

                is BookmarkLectureDeleteResult -> {
                    Triple(
                        """
                        $year-${semester.fullName} 관심강좌 목록의 
                        '$courseTitle' 강의가 폐강되어 삭제되었습니다.
                        """.trimIndent().replace("\n", ""),
                        NotificationType.LECTURE_REMOVE,
                        DeeplinkType.NOTIFICATIONS.build(),
                    )
                }

                is TimetableLectureDeleteByOverlapResult -> {
                    Triple(
                        """
                        $year-${semester.fullName} '$timetableTitle' 시간표의 
                        '$courseTitle' 강의가 업데이트되었으나, 시간표의 다른 강의와 겹쳐 삭제되었습니다.
                        """.trimIndent().replace("\n", ""),
                        NotificationType.LECTURE_REMOVE,
                        DeeplinkType.NOTIFICATIONS.build(),
                    )
                }
            }

        return Notification(
            userId = userId,
            title = "수강편람 업데이트",
            message = message,
            type = notificationType,
            deeplink = deeplink.value,
        )
    }
}
