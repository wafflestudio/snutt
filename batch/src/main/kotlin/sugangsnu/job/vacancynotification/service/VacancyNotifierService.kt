package com.wafflestudio.snutt.sugangsnu.job.vacancynotification.service

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.push.DeeplinkType
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.coursebook.data.Coursebook
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.lectures.service.LectureService
import com.wafflestudio.snutt.notification.service.PushWithNotificationService
import com.wafflestudio.snutt.sugangsnu.common.service.SugangSnuFetchService
import com.wafflestudio.snutt.sugangsnu.job.vacancynotification.data.VacancyNotificationJobResult
import com.wafflestudio.snutt.vacancynotification.repository.VacancyNotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds

interface VacancyNotifierService {
    suspend fun noti(coursebook: Coursebook): VacancyNotificationJobResult
}

@Service
class VacancyNotifierServiceImpl(
    private val lectureService: LectureService,
    private val pushWithNotificationService: PushWithNotificationService,
    private val vacancyNotificationRepository: VacancyNotificationRepository,
    private val sugangSnuFetchService: SugangSnuFetchService,
) : VacancyNotifierService {
    companion object {
        private const val DELAY_PER_CHUNK = 300L
        private val pushCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    private val log = LoggerFactory.getLogger(javaClass)
    private val isFreshmanRegistrationCompleted =
        Calendar.getInstance() >
            Calendar.getInstance().apply {
                set(Calendar.MONTH, Calendar.FEBRUARY)
                set(Calendar.DAY_OF_MONTH, 14)
            }

    override suspend fun noti(coursebook: Coursebook): VacancyNotificationJobResult {
        log.info("시작")
        val pageCount =
            runCatching {
                sugangSnuFetchService.getPageCount(coursebook.year, coursebook.semester)
            }.getOrElse {
                log.error("에러가 발생했거나 부하 기간입니다. {}", it.message, it)
                delay(30L.seconds)
                return VacancyNotificationJobResult.OVERLOAD_PERIOD
            }
        val lectures =
            lectureService.getLecturesByYearAndSemesterAsFlow(coursebook.year, coursebook.semester).toList()
        val lectureMap = lectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }

        // 수강 사이트 부하를 분산하기 위해 강의 전체를 20등분해서 각각 요청
        (1..pageCount).chunked(pageCount / 20).forEach { chunkedPages ->
            val registrationStatus = sugangSnuFetchService.getRegistrationStatus(coursebook.year, coursebook.semester, chunkedPages)
            if (registrationStatus.all { it.registrationCount == 0 }) return VacancyNotificationJobResult.REGISTRATION_IS_NOT_STARTED

            val registrationStatusMap =
                registrationStatus.associateBy { info -> info.courseNumber + "##" + info.lectureNumber }

            val lectureAndRegistrationStatus =
                (lectureMap.keys intersect registrationStatusMap.keys)
                    .map { lectureMap[it]!! to registrationStatusMap[it]!! }

            val notiTargetLectures =
                lectureAndRegistrationStatus
                    .filter { (lecture, _) -> lecture.isFull() }
                    .filter { (lecture, status) -> lecture.registrationCount > status.registrationCount }
                    .map { (lecture, _) -> lecture }

            val updated =
                lectureAndRegistrationStatus
                    .filter { (lecture, status) -> lecture.registrationCount != status.registrationCount }
                    .map { (lecture, status) ->
                        lecture.apply {
                            registrationCount = status.registrationCount
                            wasFull = status.wasFull
                        }
                    }
            lectureService.upsertLectures(updated)

            pushCoroutineScope.launch {
                notiTargetLectures.forEach { lecture ->
                    log.info(
                        "이름: {}, 강좌번호: {}, 분반번호: {}",
                        lecture.courseTitle,
                        lecture.courseNumber,
                        lecture.lectureNumber,
                    )
                    launch {
                        val userIds =
                            vacancyNotificationRepository.findAllByLectureId(lecture.id!!).map { it.userId }.toList()
                        val pushMessage =
                            PushMessage(
                                title = "빈자리 알림",
                                body = """"${lecture.courseTitle} (${lecture.lectureNumber})" 강의에 빈자리가 생겼습니다. 수강신청 사이트를 확인해보세요!""",
                                urlScheme = DeeplinkType.VACANCY.build(),
                                isUrgentOnAndroid = true,
                            )
                        /*
                        pushWithNotificationService.sendPushesAndNotifications(
                            pushMessage,
                            NotificationType.LECTURE_VACANCY,
                            userIds,
                        )
                         */
                    }
                }
            }
        }

        return VacancyNotificationJobResult.SUCCESS
    }

    private fun Lecture.isFull(): Boolean {
        val isCurrentStudentRegistrationPeriod = this.semester == Semester.SPRING && !isFreshmanRegistrationCompleted
        return if (isCurrentStudentRegistrationPeriod) {
            this.quota - (this.freshmanQuota ?: 0) == this.registrationCount
        } else {
            this.quota == this.registrationCount
        }
    }
}
