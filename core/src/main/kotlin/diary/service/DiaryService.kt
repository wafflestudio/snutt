package com.wafflestudio.snutt.diary.service

import com.wafflestudio.snutt.common.enums.DayOfWeek
import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.exception.DiaryClassDateInvalidException
import com.wafflestudio.snutt.common.exception.DiaryCommentTooLongException
import com.wafflestudio.snutt.common.exception.DiaryDailyClassTypeNotFoundException
import com.wafflestudio.snutt.common.exception.DiaryQuestionInvalidException
import com.wafflestudio.snutt.common.exception.DiaryQuestionNotFoundException
import com.wafflestudio.snutt.common.exception.DiarySubmissionNotFoundException
import com.wafflestudio.snutt.common.exception.DiarySubmissionTooFrequentException
import com.wafflestudio.snutt.common.exception.LectureNotFoundException
import com.wafflestudio.snutt.common.exception.TimetableNotFoundException
import com.wafflestudio.snutt.diary.data.DiaryDailyClassType
import com.wafflestudio.snutt.diary.data.DiaryQuestion
import com.wafflestudio.snutt.diary.data.DiaryQuestionnaire
import com.wafflestudio.snutt.diary.data.DiarySubmission
import com.wafflestudio.snutt.diary.data.DiaryTargetCandidate
import com.wafflestudio.snutt.diary.dto.DiaryShortQuestionReply
import com.wafflestudio.snutt.diary.dto.request.DiaryAddQuestionRequestDto
import com.wafflestudio.snutt.diary.dto.request.DiarySubmissionRequestDto
import com.wafflestudio.snutt.diary.repository.DiaryDailyClassTypeRepository
import com.wafflestudio.snutt.diary.repository.DiaryQuestionRepository
import com.wafflestudio.snutt.diary.repository.DiarySubmissionRepository
import com.wafflestudio.snutt.lectures.data.ClassPlaceAndTime
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.lectures.service.LectureService
import com.wafflestudio.snutt.semester.service.SemesterService
import com.wafflestudio.snutt.timetables.data.TimetableLecture
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

interface DiaryService {
    suspend fun generateQuestionnaire(
        userId: String,
        lectureId: String,
        dailyClassTypeNames: List<String>,
        now: Instant = Instant.now(),
    ): DiaryQuestionnaire

    suspend fun getDiaryTargetLecture(
        userId: String,
        year: Int,
        semester: Semester,
        idsToExclude: List<String>,
    ): TimetableLecture?

    suspend fun getDiaryTargetCandidates(
        userId: String,
        year: Int,
        semester: Semester,
        now: Instant = Instant.now(),
    ): List<DiaryTargetCandidate>

    suspend fun getActiveDailyClassTypes(): List<DiaryDailyClassType>

    suspend fun getAllDailyClassTypes(): List<DiaryDailyClassType>

    suspend fun getActiveQuestions(): List<DiaryQuestion>

    suspend fun submitDiary(
        userId: String,
        request: DiarySubmissionRequestDto,
        now: Instant = Instant.now(),
    )

    suspend fun getMySubmissions(userId: String): List<DiarySubmission>

    suspend fun removeSubmission(
        submissionId: String,
        userId: String,
    )

    suspend fun getSubmissionIdShortQuestionRepliesMap(submissions: List<DiarySubmission>): Map<String, List<DiaryShortQuestionReply>>

    suspend fun addOrEnableDailyClassType(name: String)

    suspend fun disableDailyClassType(name: String)

    suspend fun addQuestion(request: DiaryAddQuestionRequestDto)

    suspend fun removeQuestion(questionId: String)
}

@Service
class DiaryServiceImpl(
    private val diaryDailyClassTypeRepository: DiaryDailyClassTypeRepository,
    private val diaryQuestionRepository: DiaryQuestionRepository,
    private val diarySubmissionRepository: DiarySubmissionRepository,
    private val timetableRepository: TimetableRepository,
    private val lectureService: LectureService,
    private val semesterService: SemesterService,
) : DiaryService {
    companion object {
        const val COMMENT_MAX_LENGTH = 1000
        private val KST = ZoneId.of("Asia/Seoul")
    }

    override suspend fun generateQuestionnaire(
        userId: String,
        lectureId: String,
        dailyClassTypeNames: List<String>,
        now: Instant,
    ): DiaryQuestionnaire {
        val dailyClassTypeIds = diaryDailyClassTypeRepository.findAllByNameIn(dailyClassTypeNames).map { it.id!! }
        val availableQuestions = diaryQuestionRepository.findByTargetDailyClassTypeIdsInAndActiveTrue(dailyClassTypeIds)
        val questions =
            availableQuestions
                .shuffled()
                .take(3)

        val lecture = lectureService.getByIdOrNull(lectureId) ?: throw LectureNotFoundException
        val nextLecture =
            getDiaryTargetCandidates(userId, lecture.year, lecture.semester, now)
                .filter { it.writable && it.lecture.lectureId != lecture.id }
                .let { writable -> writable.firstOrNull { it.heldToday } ?: writable.randomOrNull() }

        return DiaryQuestionnaire(
            courseTitle = lecture.courseTitle,
            questions = questions,
            nextLecture = nextLecture,
        )
    }

    override suspend fun getDiaryTargetLecture(
        userId: String,
        year: Int,
        semester: Semester,
        idsToExclude: List<String>,
    ): TimetableLecture? {
        val userTimetable =
            timetableRepository.findByUserIdAndYearAndSemesterAndIsPrimaryTrue(userId, year, semester)
                ?: throw TimetableNotFoundException
        val recentlySubmittedIds =
            diarySubmissionRepository
                .findAllByUserIdAndCreatedAtIsAfter(
                    userId,
                    LocalDateTime.now().minusDays(1),
                ).map { it.lectureId }
        val nextLectureCandidates =
            userTimetable.lectures
                .filterNot { it.lectureId == null }
                .filterNot { it.lectureId in idsToExclude }
                .let { candidates ->
                    candidates
                        .filterNot { recentlySubmittedIds.contains(it.lectureId) }
                        .ifEmpty { candidates }
                }

        return nextLectureCandidates.randomOrNull()
    }

    override suspend fun getDiaryTargetCandidates(
        userId: String,
        year: Int,
        semester: Semester,
        now: Instant,
    ): List<DiaryTargetCandidate> {
        val userTimetable =
            timetableRepository.findByUserIdAndYearAndSemesterAndIsPrimaryTrue(userId, year, semester)
                ?: return emptyList()
        val nowInKst = now.atZone(KST)
        val today = nowInKst.toLocalDate()
        val nowMinute = nowInKst.hour * 60 + nowInKst.minute
        val semesterDateRange = semesterService.getDateRange(year, semester)

        val lecturesWithLastClassDate =
            userTimetable.lectures
                .filterNot { it.lectureId == null }
                .distinctBy { it.lectureId }
                .map { it to lastClassDate(it.classPlaceAndTimes, semesterDateRange, nowInKst) }
        val submittedLectureIdClassDates =
            diarySubmissionRepository
                .findAllByUserIdAndClassDateIn(userId, lecturesWithLastClassDate.map { (_, date) -> date ?: today }.toSet())
                .map { it.lectureId to it.classDate }
                .toSet()

        return lecturesWithLastClassDate
            .map { (lecture, lastClassDate) ->
                val heldToday = lastClassDate == today
                val writable = (lecture.lectureId to (lastClassDate ?: today)) !in submittedLectureIdClassDates
                val order =
                    if (heldToday) {
                        val todayEndMinutes = lecture.classPlaceAndTimes.filter { it.day == today.toSnuttDayOfWeek() }.map { it.endMinute }
                        todayClassOrder(todayEndMinutes, nowMinute)
                    } else {
                        2 to 0
                    }
                DiaryTargetCandidate(lecture, heldToday, writable, lastClassDate) to order
            }.sortedWith(compareBy({ (_, order) -> order.first }, { (_, order) -> order.second }))
            .map { (candidate, _) -> candidate }
    }

    // 오늘 이미 끝난 수업이 있는 강의는 가장 최근에 끝난 순, 아직 끝나지 않은 강의는 곧 끝나는 순, 오늘 수업이 없는 강의는 시간표 순.
    private fun todayClassOrder(
        todayEndMinutes: List<Int>,
        nowMinute: Int,
    ): Pair<Int, Int> {
        if (todayEndMinutes.isEmpty()) return 2 to 0
        val lastEndedMinute = todayEndMinutes.filter { it <= nowMinute }.maxOrNull()
        if (lastEndedMinute != null) return 0 to nowMinute - lastEndedMinute
        return 1 to todayEndMinutes.min() - nowMinute
    }

    // 학기 안에서 at 시점까지 이 강의 수업이 시작된 가장 최근 날(KST). 수업 시간이 없거나 그런 날이 없으면 null.
    private fun lastClassDate(
        classPlaceAndTimes: List<ClassPlaceAndTime>,
        semesterDateRange: ClosedRange<LocalDate>,
        at: ZonedDateTime,
    ): LocalDate? {
        val today = at.toLocalDate()
        val nowMinute = at.hour * 60 + at.minute
        val latest = minOf(today, semesterDateRange.endInclusive)
        return generateSequence(latest) { it.minusDays(1) }
            .take(8)
            .filter { it >= semesterDateRange.start }
            .firstOrNull { date ->
                classPlaceAndTimes.any { it.day == date.toSnuttDayOfWeek() && (date < today || it.startMinute <= nowMinute) }
            }
    }

    // 서버가 정해 주는 날짜이거나, 학기 안의 오늘 이전 수업 요일이면 유효하다.
    private fun isValidClassDate(
        classDate: LocalDate,
        lecture: Lecture,
        nowInKst: ZonedDateTime,
    ): Boolean {
        val today = nowInKst.toLocalDate()
        val semesterDateRange = semesterService.getDateRange(lecture.year, lecture.semester)
        if (classDate == (lastClassDate(lecture.classPlaceAndTimes, semesterDateRange, nowInKst) ?: today)) return true
        return classDate <= today &&
            classDate in semesterDateRange &&
            lecture.classPlaceAndTimes.any { it.day == classDate.toSnuttDayOfWeek() }
    }

    private fun LocalDate.toSnuttDayOfWeek(): DayOfWeek = DayOfWeek.getOfValue(dayOfWeek.value - 1)!!

    override suspend fun getActiveDailyClassTypes(): List<DiaryDailyClassType> = diaryDailyClassTypeRepository.findAllByActiveTrue()

    override suspend fun getAllDailyClassTypes(): List<DiaryDailyClassType> = diaryDailyClassTypeRepository.findAll().toList()

    override suspend fun getActiveQuestions(): List<DiaryQuestion> = diaryQuestionRepository.findAllByActiveTrue()

    override suspend fun submitDiary(
        userId: String,
        request: DiarySubmissionRequestDto,
        now: Instant,
    ) {
        if (request.comment.length > COMMENT_MAX_LENGTH) {
            throw DiaryCommentTooLongException
        }
        val lecture = lectureService.getByIdOrNull(request.lectureId) ?: throw LectureNotFoundException
        val nowInKst = now.atZone(KST)
        val classDate =
            request.classDate
                ?.also { if (!isValidClassDate(it, lecture, nowInKst)) throw DiaryClassDateInvalidException }
                ?: lastClassDate(lecture.classPlaceAndTimes, semesterService.getDateRange(lecture.year, lecture.semester), nowInKst)
                ?: nowInKst.toLocalDate()
        if (diarySubmissionRepository.existsByUserIdAndLectureIdAndClassDate(userId, request.lectureId, classDate)) {
            throw DiarySubmissionTooFrequentException
        }
        val dailyClassTypes = diaryDailyClassTypeRepository.findAllByNameIn(request.dailyClassTypes)
        val questionIds = request.questionAnswers.map { it.questionId }
        if (diaryQuestionRepository.countByIdIn(questionIds) != questionIds.size) {
            throw DiaryQuestionNotFoundException
        }
        if (dailyClassTypes.size != request.dailyClassTypes.size) {
            throw DiaryDailyClassTypeNotFoundException
        }
        val submission =
            DiarySubmission(
                userId = userId,
                comment = request.comment,
                lectureId = request.lectureId,
                courseTitle = lecture.courseTitle,
                questionAnswers = request.questionAnswers,
                dailyClassTypeIds = dailyClassTypes.map { it.id!! },
                year = lecture.year,
                semester = lecture.semester,
                classDate = classDate,
            )
        diarySubmissionRepository.save(submission)
    }

    override suspend fun getMySubmissions(userId: String): List<DiarySubmission> =
        diarySubmissionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)

    override suspend fun removeSubmission(
        submissionId: String,
        userId: String,
    ) {
        diarySubmissionRepository
            .findById(submissionId)
            ?.takeIf { it.userId == userId } ?: throw DiarySubmissionNotFoundException

        diarySubmissionRepository.deleteById(submissionId)
    }

    override suspend fun getSubmissionIdShortQuestionRepliesMap(
        submissions: List<DiarySubmission>,
    ): Map<String, List<DiaryShortQuestionReply>> {
        val questionIds = submissions.flatMap { submission -> submission.questionAnswers.map { it.questionId } }
        val questionsIdMap = diaryQuestionRepository.findAllByIdIn(questionIds).associateBy { it.id }
        return submissions.associate { submission ->
            val shortQuestionReplies =
                submission.questionAnswers.map { (questionId, answerIndex) ->
                    val question = questionsIdMap[questionId]!!
                    DiaryShortQuestionReply(
                        question = question.shortQuestion,
                        answer = question.shortAnswers[answerIndex],
                    )
                }
            submission.id!! to shortQuestionReplies
        }
    }

    override suspend fun addOrEnableDailyClassType(name: String) {
        val dailyClassType = diaryDailyClassTypeRepository.findByName(name) ?: DiaryDailyClassType(name = name, active = true)
        dailyClassType.active = true
        diaryDailyClassTypeRepository.save(dailyClassType)
    }

    override suspend fun disableDailyClassType(name: String) {
        val dailyClassType = diaryDailyClassTypeRepository.findByName(name) ?: return
        dailyClassType.active = false
        diaryDailyClassTypeRepository.save(dailyClassType)
    }

    override suspend fun addQuestion(request: DiaryAddQuestionRequestDto) {
        val targetDailyClassTypeIds = diaryDailyClassTypeRepository.findAllByNameIn(request.targetDailyClassTypes).mapNotNull { it.id }
        if (targetDailyClassTypeIds.size != request.targetDailyClassTypes.size) {
            throw DiaryDailyClassTypeNotFoundException
        }
        if (request.answers.size != request.shortAnswers.size) {
            throw DiaryQuestionInvalidException
        }
        val question =
            DiaryQuestion(
                answers = request.answers,
                shortAnswers = request.shortAnswers,
                question = request.question,
                shortQuestion = request.shortQuestion,
                targetDailyClassTypeIds = targetDailyClassTypeIds,
            )
        diaryQuestionRepository.save(question)
    }

    override suspend fun removeQuestion(questionId: String) {
        val question = diaryQuestionRepository.findById(questionId) ?: return
        question.active = false
        diaryQuestionRepository.save(question)
    }
}
