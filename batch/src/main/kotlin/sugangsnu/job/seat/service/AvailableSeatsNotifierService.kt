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
import com.wafflestudio.snu4t.sugangsnu.common.SugangSnuRepository
import com.wafflestudio.snu4t.sugangsnu.job.seat.data.AvailableSeatsNotificationResult
import com.wafflestudio.snu4t.sugangsnu.job.seat.data.RegistrationStatus
import com.wafflestudio.snu4t.users.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface AvailableSeatsNotifierService {
    suspend fun noti(coursebook: Coursebook): AvailableSeatsNotificationResult
}

@Service
class AvailableSeatsNotifierServiceImpl(
    private val lectureService: LectureService,
    private val seatNotificationRepository: SeatNotificationRepository,
    private val pushNotificationService: PushNotificationService,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val sugangSnuRepository: SugangSnuRepository,
) : AvailableSeatsNotifierService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val courseNumberRegex = """(?<courseNumber>.*)\((?<lectureNumber>\d+)\)""".toRegex()

    override suspend fun noti(coursebook: Coursebook): AvailableSeatsNotificationResult {
        logger.info("시작")
        val registrationStatus = runCatching {
            getRegistrationStatus()
        }.getOrElse {
            it.printStackTrace()
            logger.error("부하기간")
            return AvailableSeatsNotificationResult.OVERLOAD_PERIOD
        }
        if (registrationStatus.all { it.registrationCount == 0 }) return AvailableSeatsNotificationResult.REGISTRATION_IS_NOT_STARTED

        val lectures =
            lectureService.getLecturesByYearAndSemesterAsFlow(coursebook.year, coursebook.semester).toList()
        val lectureMap = lectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }
        val registrationStatusMap =
            registrationStatus.associateBy { info -> info.courseNumber + "##" + info.lectureNumber }

        val lectureAndRegistrationStatus = (lectureMap.keys intersect registrationStatusMap.keys)
            .map { lectureMap[it]!! to registrationStatusMap[it]!! }
        val updated = lectureAndRegistrationStatus
            .filter { (lecture, status) -> lecture.registrationCount != status.registrationCount }
            .map { (lecture, status) -> lecture.apply { registrationCount = status.registrationCount } }
        lectureService.upsertLectures(updated)

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
        }.filter { (lecture, status) -> (lecture.registrationCount ?: 0) > status.registrationCount }
            .map { (lecture, status) -> lecture }

        notiTargetLectures.forEach {
            logger.info("이름: ${it.courseTitle}, 강좌번호: ${it.courseNumber}, 분반번호: ${it.lectureNumber}")
        }

        coroutineScope {
            notiTargetLectures.map { lecture ->
                async {
                    val users = seatNotificationRepository.findAllByLectureId(lecture.id!!).map { it.userId }
                        .let { userRepository.findAllByIdIsIn(it) }.toList()
                    notificationRepository.saveAll(
                        users.map {
                            Notification(
                                userId = it.id!!,
                                message = lecture.courseTitle,
                                type = NotificationType.NORMAL
                            )
                        }
                    )
                    pushNotificationService.sendMessages(
                        users.mapNotNull { user ->
                            user.fcmKey?.let {
                                PushTargetMessage(it, PushMessage(lecture.courseTitle, "자리났다"))
                            }
                        }
                    )
                }
            }.awaitAll()
        }

        return AvailableSeatsNotificationResult.SUCCESS
    }

    private suspend fun getRegistrationStatus(): List<RegistrationStatus> =
        coroutineScope {
            val firstPageContent = getSugangSnuSearchContent(1)
            val totalCount =
                firstPageContent.select("div.content > div.search-result-con > small > em").text().toLong()
            val totalPage = (totalCount + 9) / 10

            ((2..totalPage).map { page -> async { getSugangSnuSearchContent(page) } }.awaitAll() + firstPageContent)
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

    private suspend fun getSugangSnuSearchContent(pageNo: Long): Element {
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
