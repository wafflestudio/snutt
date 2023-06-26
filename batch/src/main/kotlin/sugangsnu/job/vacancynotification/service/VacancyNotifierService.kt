package com.wafflestudio.snu4t.sugangsnu.job.vacancynotification.service

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.coursebook.data.Coursebook
import com.wafflestudio.snu4t.lectures.service.LectureService
import com.wafflestudio.snu4t.notification.data.NotificationType
import com.wafflestudio.snu4t.notification.service.NotificationService
import com.wafflestudio.snu4t.sugangsnu.common.SugangSnuRepository
import com.wafflestudio.snu4t.sugangsnu.job.vacancynotification.data.RegistrationStatus
import com.wafflestudio.snu4t.sugangsnu.job.vacancynotification.data.VacancyNotificationJobResult
import com.wafflestudio.snu4t.vacancynotification.repository.VacancyNotificationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface VacancyNotifierService {
    suspend fun noti(coursebook: Coursebook): VacancyNotificationJobResult
}

@Service
class VacancyNotifierServiceImpl(
    private val lectureService: LectureService,
    private val notificationService: NotificationService,
    private val vacancyNotificationRepository: VacancyNotificationRepository,
    private val sugangSnuRepository: SugangSnuRepository,
) : VacancyNotifierService {
    companion object {
        private const val COUNT_PER_PAGE = 10
        private const val DELAY_PER_CHUNK = 300L
    }

    private val log = LoggerFactory.getLogger(javaClass)
    private val courseNumberRegex = """(?<courseNumber>.*)\((?<lectureNumber>\d+)\)""".toRegex()

    override suspend fun noti(coursebook: Coursebook): VacancyNotificationJobResult {
        log.info("시작")
        val registrationStatus = runCatching {
            getRegistrationStatus()
        }.getOrElse {
            log.error("부하기간")
            return VacancyNotificationJobResult.OVERLOAD_PERIOD
        }
        if (registrationStatus.all { it.registrationCount == 0 }) return VacancyNotificationJobResult.REGISTRATION_IS_NOT_STARTED

        val lectures =
            lectureService.getLecturesByYearAndSemesterAsFlow(coursebook.year, coursebook.semester).toList()
        val lectureMap = lectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }
        val registrationStatusMap =
            registrationStatus.associateBy { info -> info.courseNumber + "##" + info.lectureNumber }

        val lectureAndRegistrationStatus = (lectureMap.keys intersect registrationStatusMap.keys)
            .map { lectureMap[it]!! to registrationStatusMap[it]!! }

        val isCurrentStudentRegistrationPeriod =
            coursebook.semester == Semester.SPRING && lectureAndRegistrationStatus.filter { (lecture, status) ->
                (lecture.freshmanQuota ?: 0) > 0
            }
                .find { (lecture, status) -> lecture.quota - lecture.freshmanQuota!! < status.registrationCount } == null

        val notiTargetLectures = lectureAndRegistrationStatus.filter { (lecture, status) ->
            if (isCurrentStudentRegistrationPeriod) {
                lecture.quota - (lecture.freshmanQuota ?: 0) == lecture.registrationCount
            } else {
                lecture.quota == lecture.registrationCount
            }
        }.filter { (lecture, status) -> lecture.registrationCount > status.registrationCount }
            .map { (lecture, _) -> lecture }

        val updated = lectureAndRegistrationStatus
            .filter { (lecture, status) -> lecture.registrationCount != status.registrationCount }
            .map { (lecture, status) -> lecture.apply { registrationCount = status.registrationCount } }
        lectureService.upsertLectures(updated)

        notiTargetLectures.forEach {
            log.info("이름: ${it.courseTitle}, 강좌번호: ${it.courseNumber}, 분반번호: ${it.lectureNumber}")
        }

        supervisorScope {
            notiTargetLectures.forEach { lecture ->
                launch {
                    val userIds = vacancyNotificationRepository.findAllByLectureId(lecture.id!!).map { it.userId }.toList()
                    val pushMessage = PushMessage(lecture.courseTitle, "자리났다", NotificationType.NORMAL)
                    notificationService.sendPushes(userIds, pushMessage)
                }
            }
        }

        return VacancyNotificationJobResult.SUCCESS
    }

    private suspend fun getRegistrationStatus(): List<RegistrationStatus> =
        coroutineScope {
            val firstPageContent = getSugangSnuSearchContent(1)
            val totalCount =
                firstPageContent.select("div.content > div.search-result-con > small > em").text().toInt()
            val totalPage = (totalCount + 9) / COUNT_PER_PAGE
            // 강의 전체를 20등분해서 요청, 각 chunk 마다 DELAY_PER_CHUNK 의 간격
            val totalContent = (2..totalPage).chunked(totalPage / 20).flatMap { iter ->
                delay(DELAY_PER_CHUNK)
                iter.map { page -> async { getSugangSnuSearchContent(page) } }.awaitAll()
            } + firstPageContent

            totalContent
                .flatMap { content ->
                    content
                        .select("div.content > div.course-list-wrap.pd-r > div.course-info-list > div.course-info-item")
                        .map { course ->
                            course.select("div.course-info-item ul.course-info").first()!!
                                .let { info ->
                                    val (courseNumber, lectureNumber) = info
                                        .select("ul.course-info > li:nth-of-type(1) > span:nth-of-type(3)").text()
                                        .takeIf { courseNumberRegex.matches(it) }!!
                                        .let { courseNumberRegex.find(it)!!.groups }
                                        .let { it["courseNumber"]!!.value to (it["lectureNumber"]!!.value) }
                                    val registrationCount =
                                        info.select("ul.course-info > li:nth-of-type(2) > span:nth-of-type(1) > em")
                                            .text()
                                            .split("/").first().toInt()
                                    RegistrationStatus(
                                        courseNumber = courseNumber,
                                        lectureNumber = lectureNumber,
                                        registrationCount = registrationCount
                                    )
                                }
                        }
                }
        }

    private suspend fun getSugangSnuSearchContent(pageNo: Int): Element {
        val webPageDataBuffer = sugangSnuRepository.getSearchPageHtml(pageNo)
        return try {
            Jsoup.parse(webPageDataBuffer.asInputStream(), Charsets.UTF_8.name(), "")
                .select("html > body > form#CC100 > div#wrapper > div#skip-con > div.content")
                .first()!!
        } finally {
            webPageDataBuffer.release()
        }
    }
}
