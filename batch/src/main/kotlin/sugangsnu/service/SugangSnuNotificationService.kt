package com.wafflestudio.snu4t.sugangsnu.service

import com.wafflestudio.snu4t.common.enum.UrlScheme
import com.wafflestudio.snu4t.common.push.PushNotificationService
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.common.push.dto.PushTargetMessage
import com.wafflestudio.snu4t.coursebook.data.Coursebook
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.data.NotificationType
import com.wafflestudio.snu4t.notification.repository.NotificationRepository
import com.wafflestudio.snu4t.sugangsnu.data.BookmarkLectureDeleteResult
import com.wafflestudio.snu4t.sugangsnu.data.BookmarkLectureUpdateResult
import com.wafflestudio.snu4t.sugangsnu.data.TimetableLectureDeleteByOverlapResult
import com.wafflestudio.snu4t.sugangsnu.data.TimetableLectureDeleteResult
import com.wafflestudio.snu4t.sugangsnu.data.TimetableLectureUpdateResult
import com.wafflestudio.snu4t.sugangsnu.data.UserLectureSyncResult
import com.wafflestudio.snu4t.sugangsnu.utils.toKoreanFieldName
import com.wafflestudio.snu4t.users.repository.UserRepository
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service

interface SugangSnuNotificationService {
    suspend fun notifyUserLectureChanges(syncSavedLecturesResults: Iterable<UserLectureSyncResult>)
    suspend fun notifyCoursebookUpdate(coursebook: Coursebook)
}

@Service
class SugangSnuNotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val pushNotificationService: PushNotificationService,
) : SugangSnuNotificationService {

    override suspend fun notifyUserLectureChanges(syncSavedLecturesResults: Iterable<UserLectureSyncResult>) {

        syncSavedLecturesResults
            .map { it.toNotification() }
            .let { notificationRepository.saveAll(it) }
            .collect()

        val pushMessages = getMessagesForEachUser(syncSavedLecturesResults)
        pushNotificationService.sendMessages(pushMessages)
    }

    private suspend fun getMessagesForEachUser(syncSavedLecturesResults: Iterable<UserLectureSyncResult>): List<PushTargetMessage> {
        val userUpdatedLectureCountMap =
            syncSavedLecturesResults.filterIsInstance<TimetableLectureUpdateResult>().toCountMap()
        val userDeletedLectureCountMap =
            syncSavedLecturesResults.filter { it is TimetableLectureDeleteResult || it is TimetableLectureDeleteByOverlapResult }
                .toCountMap()

        val tokenAndMessage = (userUpdatedLectureCountMap.keys - userDeletedLectureCountMap.keys).map {
            userRepository.findById(it)?.fcmKey to "수강편람이 업데이트되어 ${userUpdatedLectureCountMap[it]}개 강의가 변경되었습니다."
        } + (userUpdatedLectureCountMap.keys intersect userDeletedLectureCountMap.keys).map {
            userRepository.findById(it)?.fcmKey to "수강편람이 업데이트되어 ${userUpdatedLectureCountMap[it]}개 강의가 변경되고 ${userDeletedLectureCountMap[it]}개 강의가 삭제되었습니다."
        } + (userDeletedLectureCountMap.keys - userUpdatedLectureCountMap.keys).map {
            userRepository.findById(it)?.fcmKey to "수강편람이 업데이트되어 ${userDeletedLectureCountMap[it]}개 강의가 삭제되었습니다."
        }

        return tokenAndMessage
            .filterNot { (token, _) -> token.isNullOrBlank() }
            .map { (token, message) -> PushTargetMessage(token!!, PushMessage("수강편람 업데이트", message, UrlScheme.NOTIFICATIONS)) }
    }

    override suspend fun notifyCoursebookUpdate(coursebook: Coursebook) {
        val message = "${coursebook.year}년도 ${coursebook.semester.fullName} 수강편람이 추가되었습니다."
        notificationRepository.save(Notification(userId = null, message = message, type = NotificationType.COURSEBOOK))
        pushNotificationService.sendGlobalMessage(PushMessage("신규 수강편람", message))
    }

    private fun List<UserLectureSyncResult>.toCountMap() =
        this.map { result -> result.userId to result.lectureId }.distinct().groupingBy { it.first }.eachCount()

    // 도메인으로 내려도 될 것 같다.
    private fun UserLectureSyncResult.toNotification(): Notification {
        val (message, notificationType) = when (this) {
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
                '$courseTitle' 강의가 업데이트되었으나, 시간표가 겹쳐 삭제되었습니다.
                """.trimIndent().replace("\n", "") to NotificationType.LECTURE_REMOVE
            }
        }

        return Notification(userId = userId, message = message, type = notificationType)
    }
}
