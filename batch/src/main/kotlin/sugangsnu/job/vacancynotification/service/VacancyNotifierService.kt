package com.wafflestudio.snutt.sugangsnu.job.vacancynotification.service

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.push.DeeplinkType
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.coursebook.data.Coursebook
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.lectures.service.LectureService
import com.wafflestudio.snutt.lectures.utils.minuteToString
import com.wafflestudio.snutt.notification.data.NotificationType
import com.wafflestudio.snutt.notification.service.PushWithNotificationService
import com.wafflestudio.snutt.registrationperiod.data.RegistrationPhase
import com.wafflestudio.snutt.registrationperiod.data.RegistrationTimeSlot
import com.wafflestudio.snutt.sugangsnu.common.SugangSnuRepository
import com.wafflestudio.snutt.sugangsnu.job.vacancynotification.data.RegistrationStatus
import com.wafflestudio.snutt.sugangsnu.job.vacancynotification.data.VacancyNotificationJobResult
import com.wafflestudio.snutt.vacancynotification.repository.VacancyNotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.seconds

interface VacancyNotifierService {
    suspend fun notify(
        coursebook: Coursebook,
        registrationPhase: RegistrationPhase,
        vacantSeatRegistrationTimes: List<RegistrationTimeSlot>,
    ): VacancyNotificationJobResult
}

@Service
class VacancyNotifierServiceImpl(
    private val lectureService: LectureService,
    private val pushWithNotificationService: PushWithNotificationService,
    private val vacancyNotificationRepository: VacancyNotificationRepository,
    private val sugangSnuRepository: SugangSnuRepository,
) : VacancyNotifierService {
    companion object {
        private const val COUNT_PER_PAGE = 10
        private const val DELAY_PER_CHUNK = 300L
        private val pushCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    private val log = LoggerFactory.getLogger(javaClass)
    private val courseNumberRegex = """(?<courseNumber>.*)\((?<lectureNumber>.+)\)""".toRegex()

    override suspend fun notify(
        coursebook: Coursebook,
        registrationPhase: RegistrationPhase,
        vacantSeatRegistrationTimes: List<RegistrationTimeSlot>,
    ): VacancyNotificationJobResult {
        log.info("시작: ${registrationPhase.name}")
        val pageCount =
            runCatching {
                getPageCount()
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
            val registrationStatus = getRegistrationStatus(chunkedPages)
            if (registrationStatus.all { it.registrationCount == 0 }) return VacancyNotificationJobResult.REGISTRATION_IS_NOT_STARTED

            val registrationStatusMap =
                registrationStatus.associateBy { info -> info.courseNumber + "##" + info.lectureNumber }

            val lectureAndRegistrationStatus =
                (lectureMap.keys intersect registrationStatusMap.keys)
                    .map { lectureMap[it]!! to registrationStatusMap[it]!! }

            val notiTargetLectures =
                lectureAndRegistrationStatus
                    .filter { (lecture, _) -> lecture.isFull(registrationPhase) }
                    .filter { (_, status) -> status.wasFull }
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

            val now = Instant.now().atZone(ZoneId.of("Asia/Seoul"))
            val currentMinute = now.hour * 60 + now.minute
            val targetTimeString =
                vacantSeatRegistrationTimes
                    .filter { currentMinute < it.endMinute }
                    .minOfOrNull { it.startMinute }
                    ?.let { minuteToString(it) } ?: "다음 수강신청 일자"

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
                                body =
                                    """
                                    "${lecture.courseTitle} (${lecture.lectureNumber})" 강의에 빈자리가 생겼습니다. 
                                    ${targetTimeString}에 수강신청 사이트를 확인해보세요!
                                    """.trimIndent(),
                                urlScheme = DeeplinkType.VACANCY.build(),
                                isUrgentOnAndroid = true,
                            )

                        pushWithNotificationService.sendPushesAndNotifications(
                            pushMessage,
                            NotificationType.LECTURE_VACANCY,
                            userIds,
                        )
                    }
                }
            }
            delay(DELAY_PER_CHUNK)
        }

        return VacancyNotificationJobResult.SUCCESS
    }

    private fun Lecture.isFull(registrationPhase: RegistrationPhase): Boolean =
        if (this.semester == Semester.SPRING && registrationPhase == RegistrationPhase.CURRENT_STUDENT) {
            this.quota - (this.freshmanQuota ?: 0) == this.registrationCount
        } else {
            this.quota == this.registrationCount
        }

    private suspend fun getPageCount(): Int {
        val firstPageContent = getSugangSnuSearchContent(1)
        val totalCount =
            firstPageContent.select("div.content > div.search-result-con > small > em").text().toInt()
        return (totalCount + 9) / COUNT_PER_PAGE
    }

    private suspend fun getRegistrationStatus(pages: List<Int>): List<RegistrationStatus> =
        supervisorScope {
            pages
                .map { page ->
                    async {
                        getSugangSnuSearchContent(page).extractRegistrationStatus()
                    }
                }.awaitAll()
                .flatten()
        }

    private fun Element.extractRegistrationStatus() =
        this
            .select("div.content > div.course-list-wrap.pd-r > div.course-info-list > div.course-info-item")
            .map { course ->
                course
                    .select("div.course-info-item ul.course-info")
                    .first()!!
                    .let { info ->
                        val (courseNumber, lectureNumber) =
                            info
                                .select("li:nth-of-type(1) > span:nth-of-type(3)")
                                .text()
                                .takeIf { courseNumberRegex.matches(it) }!!
                                .let { courseNumberRegex.find(it)!!.groups }
                                .let { it["courseNumber"]!!.value to (it["lectureNumber"]!!.value) }
                        val registrationCount =
                            info
                                .select("ul.course-info > li:nth-of-type(2) > span:nth-of-type(1) > em")
                                .text()
                                .split("/")
                                .first()
                                .toInt()
                        val wasFull =
                            info
                                .select("li.state > span[data-dialog-target='remaining-place-dialog']")
                                .isNotEmpty()

                        RegistrationStatus(
                            courseNumber = courseNumber,
                            lectureNumber = lectureNumber,
                            registrationCount = registrationCount,
                            wasFull = wasFull,
                        )
                    }
            }

    private suspend fun getSugangSnuSearchContent(pageNo: Int): Element {
        val webPageDataBuffer = sugangSnuRepository.getSearchPageHtml(pageNo)
        return try {
            Jsoup
                .parse(webPageDataBuffer.asInputStream(), Charsets.UTF_8.name(), "")
                .select("html > body > form#CC100 > div#wrapper > div#skip-con > div.content")
                .first()!!
        } finally {
            webPageDataBuffer.release()
        }
    }
}
