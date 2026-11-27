package com.wafflestudio.snutt.diary

import BaseIntegTest
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.snutt.common.enums.DayOfWeek
import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.exception.DiaryClassDateInvalidException
import com.wafflestudio.snutt.common.exception.DiarySubmissionTooFrequentException
import com.wafflestudio.snutt.config.USER_ATTRIBUTE_KEY
import com.wafflestudio.snutt.coursebook.service.CoursebookService
import com.wafflestudio.snutt.diary.data.DiarySubmission
import com.wafflestudio.snutt.diary.dto.request.DiarySubmissionRequestDto
import com.wafflestudio.snutt.diary.repository.DiarySubmissionRepository
import com.wafflestudio.snutt.diary.service.DiaryService
import com.wafflestudio.snutt.evaluation.service.EvService
import com.wafflestudio.snutt.filter.ApiKeyWebFilter
import com.wafflestudio.snutt.filter.UserAuthenticationWebFilter
import com.wafflestudio.snutt.fixture.TimetableFixture
import com.wafflestudio.snutt.fixture.UserFixture
import com.wafflestudio.snutt.lectures.data.ClassPlaceAndTime
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.lectures.repository.LectureRepository
import com.wafflestudio.snutt.timetables.data.TimetableLecture
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import org.bson.types.ObjectId
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@SpringBootTest
@AutoConfigureWebTestClient
class DiaryIntegTest(
    @MockkBean private val mockSnuttEvService: EvService,
    @MockkBean private val mockCoursebookService: CoursebookService,
    @MockkBean private val apiKeyWebFilter: ApiKeyWebFilter,
    @MockkBean private val userAuthenticationWebFilter: UserAuthenticationWebFilter,
    val webTestClient: WebTestClient,
    val timetableFixture: TimetableFixture,
    val userFixture: UserFixture,
    val timetableRepository: TimetableRepository,
    val lectureRepository: LectureRepository,
    val diarySubmissionRepository: DiarySubmissionRepository,
    val diaryService: DiaryService,
    val repositories: List<CoroutineCrudRepository<*, *>>,
) : BaseIntegTest({
        coEvery {
            apiKeyWebFilter.filter(any(), any())
        } answers {
            val exchange = firstArg<ServerWebExchange>()
            val chain = secondArg<WebFilterChain>()
            chain.filter(exchange)
        }
        coEvery {
            userAuthenticationWebFilter.filter(any(), any())
        } answers {
            val exchange = firstArg<ServerWebExchange>()
            val chain = secondArg<WebFilterChain>()
            mono {
                exchange.attributes[USER_ATTRIBUTE_KEY] = userFixture.testUser
            }.then(chain.filter(exchange))
        }

        afterEach {
            repositories.forEach { it.deleteAll() }
        }

        val userId = userFixture.testUser.id!!

        // 2026-11-18(수) 14:00 KST, 2026년 2학기(09-01 ~ 12-19) 중
        val now = Instant.parse("2026-11-18T05:00:00Z")
        val today = LocalDate.of(2026, 11, 18)

        fun timetableLecture(
            courseTitle: String,
            vararg times: ClassPlaceAndTime,
            lectureId: String? = ObjectId.get().toHexString(),
        ) = TimetableLecture(
            lectureId = lectureId,
            academicYear = null,
            category = null,
            classPlaceAndTimes = times.toList(),
            classification = null,
            credit = 3,
            department = null,
            instructor = null,
            lectureNumber = "001",
            quota = null,
            freshmanQuota = null,
            remark = null,
            courseNumber = null,
            courseTitle = courseTitle,
            categoryPre2025 = null,
        )

        fun classTime(
            day: DayOfWeek,
            startMinute: Int,
            endMinute: Int,
        ) = ClassPlaceAndTime(day = day, place = "301-101", startMinute = startMinute, endMinute = endMinute)

        suspend fun saveLecture(timetableLecture: TimetableLecture) =
            lectureRepository.save(
                Lecture(
                    id = timetableLecture.lectureId,
                    academicYear = null,
                    category = null,
                    classPlaceAndTimes = timetableLecture.classPlaceAndTimes,
                    classification = null,
                    credit = 3,
                    department = null,
                    instructor = null,
                    lectureNumber = "001",
                    quota = 0,
                    remark = null,
                    semester = Semester.AUTUMN,
                    year = 2026,
                    courseNumber = "M0000.000000",
                    courseTitle = timetableLecture.courseTitle,
                    categoryPre2025 = null,
                ),
            )

        suspend fun saveSubmission(
            lecture: TimetableLecture,
            classDate: LocalDate?,
            createdAt: LocalDateTime = LocalDateTime.now(),
        ) = diarySubmissionRepository.save(
            DiarySubmission(
                userId = userId,
                dailyClassTypeIds = emptyList(),
                questionAnswers = emptyList(),
                lectureId = lecture.lectureId!!,
                courseTitle = lecture.courseTitle,
                comment = "",
                createdAt = createdAt,
                year = 2026,
                semester = Semester.AUTUMN,
                classDate = classDate,
            ),
        )

        fun submissionRequest(
            lecture: TimetableLecture,
            classDate: LocalDate? = null,
        ) = DiarySubmissionRequestDto(
            lectureId = lecture.lectureId!!,
            dailyClassTypes = emptyList(),
            questionAnswers = emptyList(),
            comment = "",
            classDate = classDate,
        )

        "submitDiary" should {
            val lecture = timetableLecture("수요일 수업", classTime(DayOfWeek.WEDNESDAY, 9 * 60, 10 * 60))

            "classDate 가 없으면 가장 최근 수업일로 저장하고, 같은 수업일에 다시 쓰면 거절한다" {
                saveLecture(lecture)

                diaryService.submitDiary(userId, submissionRequest(lecture), now)

                diarySubmissionRepository
                    .findAll()
                    .toList()
                    .single()
                    .classDate shouldBe today
                shouldThrow<DiarySubmissionTooFrequentException> {
                    diaryService.submitDiary(userId, submissionRequest(lecture, today), now)
                }
            }

            "지난 수업일을 지정하면 같은 강의라도 날짜별로 하나씩 쓸 수 있다" {
                saveLecture(lecture)

                diaryService.submitDiary(userId, submissionRequest(lecture), now)
                diaryService.submitDiary(userId, submissionRequest(lecture, LocalDate.of(2026, 11, 11)), now)

                diarySubmissionRepository.findAll().toList().map { it.classDate } shouldContainExactlyInAnyOrder
                    listOf(today, LocalDate.of(2026, 11, 11))
            }

            "수업 요일이 아니거나 미래이거나 학기 밖인 classDate 는 거절한다" {
                saveLecture(lecture)

                listOf(
                    LocalDate.of(2026, 11, 17),
                    LocalDate.of(2026, 11, 25),
                    LocalDate.of(2026, 6, 17),
                ).forEach { classDate ->
                    shouldThrow<DiaryClassDateInvalidException> {
                        diaryService.submitDiary(userId, submissionRequest(lecture, classDate), now)
                    }
                }
            }

            "수업 날짜 도입 전 기록(classDate 없음)은 중복 판정에 걸리지 않는다" {
                saveLecture(lecture)
                saveSubmission(lecture, null)

                diaryService.submitDiary(userId, submissionRequest(lecture), now)

                diarySubmissionRepository.findAll().toList().map { it.classDate } shouldContainExactlyInAnyOrder listOf(null, today)
            }
        }

        "POST /v1/diary" should {
            "POST /v1/diary 는 yyyy-MM-dd 형식의 classDate 를 받아 저장한다" {
                val lecture = timetableLecture("수요일 수업", classTime(DayOfWeek.WEDNESDAY, 9 * 60, 10 * 60))
                saveLecture(lecture)

                webTestClient
                    .post()
                    .uri("/v1/diary")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .bodyValue(
                        """{"lectureId":"${lecture.lectureId}","dailyClassTypes":[],"questionAnswers":[],"comment":"","classDate":"2026-11-18"}""",
                    ).exchange()
                    .expectStatus()
                    .isOk

                diarySubmissionRepository
                    .findAll()
                    .toList()
                    .single()
                    .classDate shouldBe today
            }

            "POST /v1/diary 의 classDate 가 유효하지 않으면 40029 로 거절한다" {
                val lecture = timetableLecture("수요일 수업", classTime(DayOfWeek.WEDNESDAY, 9 * 60, 10 * 60))
                saveLecture(lecture)

                webTestClient
                    .post()
                    .uri("/v1/diary")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .bodyValue(
                        """{"lectureId":"${lecture.lectureId}","dailyClassTypes":[],"questionAnswers":[],"comment":"","classDate":"2026-11-17"}""",
                    ).exchange()
                    .expectStatus()
                    .isBadRequest
                    .expectBody()
                    .jsonPath("$.errcode")
                    .isEqualTo(40029)
            }
        }
    })
