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

        suspend fun savePrimaryTimetable(
            vararg lectures: TimetableLecture,
            year: Int = 2026,
            semester: Semester = Semester.AUTUMN,
        ) = timetableRepository.save(
            timetableFixture
                .getTimetable("primary")
                .copy(year = year, semester = semester, lectures = lectures.toList(), isPrimary = true),
        )

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

        "getDiaryTargetCandidates" should {
            "수업이 시작된 오늘 강의를 방금 끝난 순으로 앞에 두고, 가장 최근 수업일을 이미 기록한 강의는 writable=false 로 내려준다" {
                val justEnded =
                    timetableLecture(
                        "방금 끝남",
                        classTime(DayOfWeek.MONDAY, 12 * 60 + 30, 13 * 60 + 45),
                        classTime(DayOfWeek.WEDNESDAY, 12 * 60 + 30, 13 * 60 + 45),
                    )
                val endedLongAgo = timetableLecture("오전에 끝남", classTime(DayOfWeek.WEDNESDAY, 9 * 60, 10 * 60 + 15))
                val inProgress = timetableLecture("진행 중", classTime(DayOfWeek.WEDNESDAY, 13 * 60 + 30, 15 * 60))
                val writtenToday = timetableLecture("오늘 기록함", classTime(DayOfWeek.WEDNESDAY, 8 * 60, 9 * 60))
                val notStarted = timetableLecture("오늘 아직 시작 전", classTime(DayOfWeek.WEDNESDAY, 15 * 60 + 30, 16 * 60 + 45))
                val writtenYesterday = timetableLecture("어제 수업 기록함", classTime(DayOfWeek.TUESDAY, 10 * 60, 11 * 60))
                val writtenTwoWeeksAgo = timetableLecture("지지난주 기록함", classTime(DayOfWeek.THURSDAY, 10 * 60, 11 * 60))
                val noClassTime = timetableLecture("시간 없음")
                val customLecture = timetableLecture("직접 추가", classTime(DayOfWeek.WEDNESDAY, 13 * 60, 14 * 60), lectureId = null)
                savePrimaryTimetable(
                    writtenTwoWeeksAgo,
                    endedLongAgo,
                    notStarted,
                    writtenYesterday,
                    customLecture,
                    inProgress,
                    noClassTime,
                    justEnded,
                    writtenToday,
                )
                saveSubmission(writtenToday, today)
                saveSubmission(writtenYesterday, LocalDate.of(2026, 11, 17))
                saveSubmission(writtenTwoWeeksAgo, LocalDate.of(2026, 11, 5))
                saveSubmission(noClassTime, today)

                val candidates = diaryService.getDiaryTargetCandidates(userId, 2026, Semester.AUTUMN, now)

                candidates.map { listOf(it.lecture.courseTitle, it.heldToday, it.writable, it.lastClassDate) } shouldBe
                    listOf(
                        listOf("방금 끝남", true, true, today),
                        listOf("오전에 끝남", true, true, today),
                        listOf("오늘 기록함", true, false, today),
                        listOf("진행 중", true, true, today),
                        listOf("지지난주 기록함", false, true, LocalDate.of(2026, 11, 12)),
                        listOf("오늘 아직 시작 전", false, true, LocalDate.of(2026, 11, 11)),
                        listOf("어제 수업 기록함", false, false, LocalDate.of(2026, 11, 17)),
                        listOf("시간 없음", false, false, null),
                    )
            }

            "학기가 끝났으면 학기 마지막 수업일, 학기 시작 전이면 null 을 lastClassDate 로 준다" {
                val lecture = timetableLecture("수요일 수업", classTime(DayOfWeek.WEDNESDAY, 9 * 60, 10 * 60))
                savePrimaryTimetable(lecture, year = 2026, semester = Semester.SPRING)
                savePrimaryTimetable(lecture, year = 2027, semester = Semester.SPRING)

                diaryService
                    .getDiaryTargetCandidates(userId, 2026, Semester.SPRING, now)
                    .single()
                    .lastClassDate shouldBe LocalDate.of(2026, 6, 17)
                diaryService
                    .getDiaryTargetCandidates(userId, 2027, Semester.SPRING, now)
                    .single()
                    .lastClassDate
                    .shouldBeNull()
            }

            "대표 시간표가 없으면 빈 목록을 내려준다" {
                diaryService.getDiaryTargetCandidates(userId, 2026, Semester.AUTUMN, now).shouldBeEmpty()
            }
        }

        "generateQuestionnaire 의 nextLecture" should {
            "가장 최근 수업일을 이미 기록한 강의와 현재 강의를 제외하고, lastClassDate 와 함께 고른다" {
                val current = timetableLecture("현재 강의", classTime(DayOfWeek.MONDAY, 9 * 60, 10 * 60))
                val written = timetableLecture("기록한 강의", classTime(DayOfWeek.TUESDAY, 9 * 60, 10 * 60))
                val writable = timetableLecture("쓸 수 있는 강의", classTime(DayOfWeek.FRIDAY, 9 * 60, 10 * 60))
                savePrimaryTimetable(current, written, writable)
                saveLecture(current)
                saveSubmission(written, LocalDate.of(2026, 11, 17))

                repeat(10) {
                    val nextLecture = diaryService.generateQuestionnaire(userId, current.lectureId!!, emptyList(), now).nextLecture!!
                    nextLecture.lecture.lectureId shouldBe writable.lectureId
                    nextLecture.lastClassDate shouldBe LocalDate.of(2026, 11, 13)
                }
            }

            "작성할 수 있는 다른 강의가 없으면 null 이다" {
                val current = timetableLecture("현재 강의", classTime(DayOfWeek.MONDAY, 9 * 60, 10 * 60))
                val written = timetableLecture("기록한 강의", classTime(DayOfWeek.TUESDAY, 9 * 60, 10 * 60))
                savePrimaryTimetable(current, written)
                saveLecture(current)
                saveSubmission(written, LocalDate.of(2026, 11, 17))

                diaryService.generateQuestionnaire(userId, current.lectureId!!, emptyList(), now).nextLecture.shouldBeNull()
            }
        }

        "GET /v1/diary/targets" should {
            "GET /v1/diary/targets 는 후보 목록을 클라이언트 스펙대로 내려준다" {
                val lecture = timetableLecture("자료구조")
                savePrimaryTimetable(lecture)

                webTestClient
                    .get()
                    .uri("/v1/diary/targets?year=2026&semester=3")
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody()
                    .jsonPath("$.length()")
                    .isEqualTo(1)
                    .jsonPath("$[0].lectureId")
                    .isEqualTo(lecture.lectureId!!)
                    .jsonPath("$[0].courseTitle")
                    .isEqualTo("자료구조")
                    .jsonPath("$[0].heldToday")
                    .isEqualTo(false)
                    .jsonPath("$[0].writable")
                    .isEqualTo(true)
                    .jsonPath("$[0].lastClassDate")
                    .isEmpty
            }
        }

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
