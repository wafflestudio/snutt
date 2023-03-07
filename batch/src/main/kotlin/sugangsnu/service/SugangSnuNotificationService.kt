package com.wafflestudio.snu4t.sugangsnu.service

import com.wafflestudio.snu4t.common.push.PushNotificationService
import com.wafflestudio.snu4t.common.push.PushTargetMessage
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.data.NotificationType
import com.wafflestudio.snu4t.notification.repository.NotificationRepository
import com.wafflestudio.snu4t.sugangsnu.data.BookmarkLectureDeleteResult
import com.wafflestudio.snu4t.sugangsnu.data.BookmarkLectureUpdateResult
import com.wafflestudio.snu4t.sugangsnu.data.TimetableLectureDeleteResult
import com.wafflestudio.snu4t.sugangsnu.data.TimetableLectureUpdateResult
import com.wafflestudio.snu4t.sugangsnu.data.UserLectureSyncResult
import com.wafflestudio.snu4t.users.repository.UserRepository
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service
import kotlin.reflect.KProperty1

interface SugangSnuNotificationService {
    suspend fun notifyUserLectureChanges(syncSavedLecturesResults: Iterable<UserLectureSyncResult>)
}

@Service
class SugangSnuNotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val pushNotificationService: PushNotificationService,
) : SugangSnuNotificationService {

    override suspend fun notifyUserLectureChanges(syncSavedLecturesResults: Iterable<UserLectureSyncResult>) {
        syncSavedLecturesResults.map {
            val message = when(it) {
                is TimetableLectureUpdateResult -> "${it.year}-${it.semester.fullName} '${it.timetableTitle}' 시간표의 " +
                    "'${it.courseTitle}' 강의가 업데이트 되었습니다." +
                    "(항목: ${it.updatedField.map { field -> field.koreanName() }.distinct().joinToString(",")})"
                is TimetableLectureDeleteResult -> "${it.year}-${it.semester.fullName} '${it.timetableTitle}' 시간표의 " +
                    "'${it.courseTitle}' 강의가 폐강되어 삭제되었습니다."
                is BookmarkLectureUpdateResult ->"${it.year}-${it.semester.fullName} 관심강좌 목록의 " +
                    "'${it.courseTitle}' 강의가 업데이트 되었습니다." +
                    "(항목: ${it.updatedField.map { field -> field.koreanName() }.distinct().joinToString(",")})"
                is BookmarkLectureDeleteResult -> "${it.year}-${it.semester.fullName} 관심강좌 목록의 " +
                    "'${it.courseTitle}' 강의가 폐강되어 삭제되었습니다."
            }
            Notification(userId = it.userId, message = message, type = NotificationType.LECTURE_UPDATE)
        }.let { notificationRepository.saveAll(it) }.collect()

        val userUpdatedLectureCountMap =
            syncSavedLecturesResults.filterIsInstance<TimetableLectureUpdateResult>().toCountMap()
        val userDeletedLectureCountMap =
            syncSavedLecturesResults.filterIsInstance<TimetableLectureDeleteResult>().toCountMap()
        val tokenAndMessage = (userUpdatedLectureCountMap.keys - userDeletedLectureCountMap.keys).map {
            userRepository.findById(it)?.fcmKey to "수강편람이 업데이트되어 ${userUpdatedLectureCountMap[it]}개 강의가 변경되었습니다."
        } + (userUpdatedLectureCountMap.keys intersect userDeletedLectureCountMap.keys).map {
            userRepository.findById(it)?.fcmKey to "수강편람이 업데이트되어 ${userUpdatedLectureCountMap[it]}개 강의가 변경되고 ${userDeletedLectureCountMap[it]}개 강의가 삭제되었습니다.";
        } + (userDeletedLectureCountMap.keys - userUpdatedLectureCountMap.keys).map {
            userRepository.findById(it)?.fcmKey to "수강편람이 업데이트되어 ${userDeletedLectureCountMap[it]}개 강의가 삭제되었습니다."
        }

        tokenAndMessage.filterNot { (token, _) -> token.isNullOrBlank() }
            .map { (token, message) -> PushTargetMessage(targetToken = token!!, "수강편람 업데이트", message) }
            .let { pushNotificationService.sendMessages(it) }
    }

    private fun KProperty1<Lecture, *>.koreanName() = when (this) {
        Lecture::classification -> "교과 구분"
        Lecture::department -> "학부"
        Lecture::academicYear -> "학년"
        Lecture::courseTitle -> "강의명"
        Lecture::credit -> "학점"
        Lecture::instructor -> "교수"
        Lecture::quota -> "정원"
        Lecture::remark -> "비고"
        Lecture::category -> "교양 구분"
        Lecture::classTime -> "강의 시간/장소"
        else -> "기타"
    }

    private fun List<UserLectureSyncResult>.toCountMap() =
        this.map { result -> result.userId to result.lectureId }.distinct().groupingBy { it.first }.eachCount()
}
