package com.wafflestudio.snutt.diary.service

import com.wafflestudio.snutt.common.exception.DiaryActivityTypeNotFoundException
import com.wafflestudio.snutt.common.exception.DiaryQuestionNotFoundException
import com.wafflestudio.snutt.diary.data.DiaryActivityType
import com.wafflestudio.snutt.diary.data.DiaryQuestion
import com.wafflestudio.snutt.diary.data.DiarySubmission
import com.wafflestudio.snutt.diary.dto.DiaryShortQuestionReply
import com.wafflestudio.snutt.diary.dto.request.DiaryAddQuestionRequestDto
import com.wafflestudio.snutt.diary.dto.request.DiarySubmissionRequestDto
import com.wafflestudio.snutt.diary.repository.DiaryActivityTypeRepository
import com.wafflestudio.snutt.diary.repository.DiaryQuestionRepository
import com.wafflestudio.snutt.diary.repository.DiarySubmissionRepository
import com.wafflestudio.snutt.lectures.service.LectureService
import kotlinx.coroutines.flow.map
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

        println(answeredQuestionIds)
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
        val activityTypes = diaryActivityTypeRepository.findByNameIn(request.activityTypes).toList()
        if (!diaryQuestionRepository.existsAllById(request.questionIds)) {
            throw DiaryQuestionNotFoundException
        }
        if (activityTypes.size < request.activityTypes.size) {
            throw DiaryActivityTypeNotFoundException
        }

        val submission =
            DiarySubmission(
                userId = userId,
                comment = request.comment,
                lectureId = request.lectureId,
                courseTitle = lecture.courseTitle,
                answerIndexes = request.answerIndexes,
                questionIds = request.questionIds,
                activityTypeIds = activityTypes.map { it.id!! },
            )
        diarySubmissionRepository.save(submission)
    }

    suspend fun getMySubmissions(userId: String): List<DiarySubmission> = diarySubmissionRepository.findAllByUserIdOrderByCreatedAt(userId)

    suspend fun getSubmissionIdShortQuestionRepliesMap(submissions: List<DiarySubmission>): Map<String, List<DiaryShortQuestionReply>> {
        val questions = diaryQuestionRepository.findAllByIdIn(submissions.flatMap { it.questionIds }).associateBy { it.id }
        return submissions.associate { submission ->
            val shortQuestionReplies =
                submission.questionIds.mapIndexedNotNull { index, questionId ->
                    questions[questionId]?.let { question ->
                        DiaryShortQuestionReply(
                            question = question.shortQuestion,
                            answer = question.answers[submission.answerIndexes[index]],
                        )
                    }
                }
            submission.id!! to shortQuestionReplies
        }
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
                shortAnswers = request.shortAnswers,
                question = request.question,
                shortQuestion = request.shortQuestion,
                targetTopics = diaryActivityTypeRepository.findByNameIn(request.targetActivityTypes),
            )
        diaryQuestionRepository.save(question)
    }

    suspend fun removeQuestion(questionId: String) {
        val question = diaryQuestionRepository.findById(questionId) ?: return
        question.active = false
        diaryQuestionRepository.save(question)
    }
}
