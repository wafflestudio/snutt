package com.wafflestudio.snu4t.sugangsnu.job.seat.service

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.push.PushNotificationService
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.common.push.dto.PushTargetMessage
import com.wafflestudio.snu4t.coursebook.data.Coursebook
import com.wafflestudio.snu4t.lectures.service.LectureService
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.data.NotificationType
import com.wafflestudio.snu4t.notification.repository.NotificationRepository
import com.wafflestudio.snu4t.seatsnotification.repository.SeatNotificationRepository
import com.wafflestudio.snu4t.sugangsnu.common.enum.LectureCategory
import com.wafflestudio.snu4t.sugangsnu.common.service.SugangSnuFetchService
import com.wafflestudio.snu4t.sugangsnu.job.seat.data.AvailableSeatsNotificationResult
import com.wafflestudio.snu4t.users.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface AvailableSeatsNotifierService {
    suspend fun noti(coursebook: Coursebook): AvailableSeatsNotificationResult
}

@Service
class AvailableSeatsNotifierServiceImpl(
    private val sugangSnuFetchService: SugangSnuFetchService,
    private val lectureService: LectureService,
    private val seatNotificationRepository: SeatNotificationRepository,
    private val pushNotificationService: PushNotificationService,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
) : AvailableSeatsNotifierService {
    private val logger = LoggerFactory.getLogger(javaClass)
    override suspend fun noti(coursebook: Coursebook): AvailableSeatsNotificationResult {
        logger.info("시작")
        val newLectures = runCatching {
            sugangSnuFetchService.getSugangSnuLectures(coursebook.year, coursebook.semester, LectureCategory.NONE)
        }.getOrElse {
            logger.error("부하기간이다.")
            return AvailableSeatsNotificationResult.OVERLOAD_PERIOD
        }
        if (newLectures.all { it.registrationCount == 0 }) return AvailableSeatsNotificationResult.REGISTRATION_IS_NOT_STARTED

        val oldLectures =
            lectureService.getLecturesByYearAndSemesterAsFlow(coursebook.year, coursebook.semester).toList()
        val newMap = newLectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }
        val oldMap = oldLectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }

        val lecturePairs = (newMap.keys intersect oldMap.keys)
            .map { oldMap[it]!! to newMap[it]!! }
        val updated = lecturePairs
            .filter { (old, new) -> old.registrationCount != new.registrationCount }
            .map { (old, new) -> old.apply { registrationCount = new.registrationCount } }
        lectureService.upsertLectures(updated)

        val isCurrentStudentRegistrationPeriod =
            coursebook.semester == Semester.SPRING && newLectures.filter { (it.freshmanQuota ?: 0) > 0 }
                .find { it.quota - it.freshmanQuota!! < it.registrationCount } == null

        val notiTargetLectures = lecturePairs.filter { (old, new) ->
            if (isCurrentStudentRegistrationPeriod) {
                old.quota - (old.freshmanQuota ?: 0) == old.registrationCount
            } else {
                old.quota == old.registrationCount
            }
        }.filter { (old, new) -> (old.registrationCount ?: 0) > new.registrationCount }
            .map { (old, new) -> old }

        notiTargetLectures.forEach {
            logger.info("이름: ${it.courseTitle}, 강좌번호: ${it.courseNumber}, 분반번호: ${it.lectureNumber}")
        }

        coroutineScope {
            notiTargetLectures.map { lecture ->
                async {
                    val users = seatNotificationRepository.findAllByLectureId(lecture.id!!).map { it.userId }
                        .let { userRepository.findAllByIdIsIn(it) }.toList()
                    notificationRepository.saveAll(users.map {
                        Notification(
                            userId = it.id!!,
                            message = lecture.courseTitle,
                            type = NotificationType.NORMAL
                        )
                    })
                    pushNotificationService.sendMessages(users.mapNotNull { user ->
                        user.fcmKey?.let {
                            PushTargetMessage(it, PushMessage(lecture.courseTitle, "자리났다"))
                        }
                    })
                }
            }.awaitAll()
        }

        return AvailableSeatsNotificationResult.SUCCESS
    }
}
