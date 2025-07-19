package com.wafflestudio.snutt.diary.service

import com.wafflestudio.snutt.diary.data.DiaryActivityType
import com.wafflestudio.snutt.diary.data.DiaryQuestion
import com.wafflestudio.snutt.diary.data.DiarySubmission
import com.wafflestudio.snutt.diary.dto.DiaryShortQuestionReply
import com.wafflestudio.snutt.diary.dto.DiarySubmissionSummaryDto
import com.wafflestudio.snutt.diary.dto.request.DiaryAddQuestionRequestDto
import com.wafflestudio.snutt.diary.dto.request.DiarySubmissionRequestDto
import com.wafflestudio.snutt.diary.repository.DiaryActivityTypeRepository
import com.wafflestudio.snutt.diary.repository.DiaryQuestionRepository
import com.wafflestudio.snutt.diary.repository.DiarySubmissionRepository
import com.wafflestudio.snutt.lectures.service.LectureService
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class DiaryService(
    private val diaryActivityTypeRepository: DiaryActivityTypeRepository,
    private val diaryQuestionRepository: DiaryQuestionRepository,
    private val diarySubmissionRepository: DiarySubmissionRepository,
    private val lectureService: LectureService,
) {
    suspend fun generateQuestionnaire(
        userId: String,
        lectureId: String,
        activityTypeNames: List<String>,
    ): List<DiaryQuestion> {
        val activityTypes = diaryActivityTypeRepository.findByNameIn(activityTypeNames)
        val questions = diaryQuestionRepository.findByTargetTopicsContainsAndActiveTrue(activityTypes)
        val answeredQuestionIds =
            diarySubmissionRepository.findAllByUserIdOrderByCreatedAt(userId)
                .filter { it.lectureId == lectureId }
                .flatMap { it.questionIds }
                .toSet()

        return questions.filterNot { question -> question.id in answeredQuestionIds }
            .shuffled()
            .take(3)
    }

    suspend fun getActiveActivityTypes(): List<DiaryActivityType> = diaryActivityTypeRepository.findAllByActiveTrue()

    suspend fun getAllActivityTypes(): List<DiaryActivityType> = diaryActivityTypeRepository.findAll().toList()

    suspend fun getActiveQuestions(): List<DiaryQuestion> = diaryQuestionRepository.findAllByActiveTrue()

    suspend fun submitDiary(
        userId: String,
        request: DiarySubmissionRequestDto,
    ) {
        val lecture = lectureService.getByIdOrNull(request.lectureId)!!
        val submission =
            DiarySubmission(
                userId = userId,
                comment = request.comment,
                lectureId = request.lectureId,
                courseTitle = lecture.courseTitle,
                answerIndexes = request.answerIndexes,
                questionIds = request.questionIds,
            )
        diarySubmissionRepository.save(submission)
    }

    suspend fun getMySubmissions(userId: String): List<DiarySubmissionSummaryDto> =
        diarySubmissionRepository.findAllByUserIdOrderByCreatedAt(userId).map { submission ->
            val shortQuestionReplies =
                submission.questionIds.mapIndexed { index, questionId ->
                    val question = diaryQuestionRepository.findById(questionId)!!
                    DiaryShortQuestionReply(
                        question = question.shortenedQuestion,
                        answer = question.shortenedAnswers[index],
                    )
                }
            DiarySubmissionSummaryDto(
                date = submission.createdAt,
                lectureTitle = submission.courseTitle,
                shortQuestionReplies = shortQuestionReplies,
                comment = submission.comment,
            )
        }

    suspend fun addOrEnableActivityType(name: String) {
        val activityType = diaryActivityTypeRepository.findByName(name) ?: DiaryActivityType(name = name, active = true)
        activityType.active = true
        diaryActivityTypeRepository.save(activityType)
    }

    suspend fun disableActivityType(name: String) {
        val activityType = diaryActivityTypeRepository.findByName(name) ?: return
        activityType.active = false
        diaryActivityTypeRepository.save(activityType)
    }

    suspend fun addQuestion(request: DiaryAddQuestionRequestDto) {
        val question =
            DiaryQuestion(
                answers = request.answers,
                shortenedAnswers = request.shortenedAnswers,
                question = request.question,
                shortenedQuestion = request.shortenedQuestion,
                targetTopics = diaryActivityTypeRepository.findByNameIn(request.targetTopics),
            )
        diaryQuestionRepository.save(question)
    }

    suspend fun removeQuestion(questionId: String) {
        val question = diaryQuestionRepository.findById(questionId) ?: return
        question.active = false
        diaryQuestionRepository.save(question)
    }
}
