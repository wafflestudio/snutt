package com.wafflestudio.snutt.diary.service

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.exception.DiaryDailyClassTypeNotFoundException
import com.wafflestudio.snutt.common.exception.DiaryQuestionNotFoundException
import com.wafflestudio.snutt.common.exception.DiarySubmissionNotFoundException
import com.wafflestudio.snutt.common.exception.LectureNotFoundException
import com.wafflestudio.snutt.common.exception.TimetableNotFoundException
import com.wafflestudio.snutt.diary.data.DiaryDailyClassType
import com.wafflestudio.snutt.diary.data.DiaryQuestion
import com.wafflestudio.snutt.diary.data.DiaryQuestionnaire
import com.wafflestudio.snutt.diary.data.DiarySubmission
import com.wafflestudio.snutt.diary.dto.DiaryShortQuestionReply
import com.wafflestudio.snutt.diary.dto.request.DiaryAddQuestionRequestDto
import com.wafflestudio.snutt.diary.dto.request.DiarySubmissionRequestDto
import com.wafflestudio.snutt.diary.repository.DiaryDailyClassTypeRepository
import com.wafflestudio.snutt.diary.repository.DiaryQuestionRepository
import com.wafflestudio.snutt.diary.repository.DiarySubmissionRepository
import com.wafflestudio.snutt.lectures.service.LectureService
import com.wafflestudio.snutt.timetables.data.TimetableLecture
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface DiaryService {
    suspend fun generateQuestionnaire(
        userId: String,
        lectureId: String,
        dailyClassTypeNames: List<String>,
    ): DiaryQuestionnaire

    suspend fun getDiaryTargetLecture(
        userId: String,
        year: Int,
        semester: Semester,
        filterIds: List<String>,
    ): TimetableLecture?

    suspend fun getActiveDailyClassTypes(): List<DiaryDailyClassType>

    suspend fun getAllDailyClassTypes(): List<DiaryDailyClassType>

    suspend fun getActiveQuestions(): List<DiaryQuestion>

    suspend fun submitDiary(
        userId: String,
        request: DiarySubmissionRequestDto,
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
) : DiaryService {
    override suspend fun generateQuestionnaire(
        userId: String,
        lectureId: String,
        dailyClassTypeNames: List<String>,
    ): DiaryQuestionnaire {
        val dailyClassTypeIds = diaryDailyClassTypeRepository.findAllByNameIn(dailyClassTypeNames).map { it.id!! }
        val availableQuestions = diaryQuestionRepository.findByTargetDailyClassTypeIdsInAndActiveTrue(dailyClassTypeIds)
        val questions =
            availableQuestions
                .shuffled()
                .take(3)

        val lecture = lectureService.getByIdOrNull(lectureId) ?: throw LectureNotFoundException
        val nextLecture = getDiaryTargetLecture(userId, lecture.year, lecture.semester, listOf(lecture.id!!))

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
        filterIds: List<String>,
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
                .filterNot { it.lectureId in filterIds }
                .let { candidates ->
                    candidates
                        .filterNot { recentlySubmittedIds.contains(it.lectureId) }
                        .ifEmpty { candidates }
                }

        return nextLectureCandidates.randomOrNull()
    }

    override suspend fun getActiveDailyClassTypes(): List<DiaryDailyClassType> = diaryDailyClassTypeRepository.findAllByActiveTrue()

    override suspend fun getAllDailyClassTypes(): List<DiaryDailyClassType> = diaryDailyClassTypeRepository.findAll().toList()

    override suspend fun getActiveQuestions(): List<DiaryQuestion> = diaryQuestionRepository.findAllByActiveTrue()

    override suspend fun submitDiary(
        userId: String,
        request: DiarySubmissionRequestDto,
    ) {
        val lecture = lectureService.getByIdOrNull(request.lectureId) ?: throw LectureNotFoundException
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
