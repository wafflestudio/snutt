package com.wafflestudio.snutt.diary.service

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.common.exception.DiaryActivityTypeNotFoundException
import com.wafflestudio.snutt.common.exception.DiaryQuestionNotFoundException
import com.wafflestudio.snutt.diary.data.DiaryActivity
import com.wafflestudio.snutt.diary.data.DiaryQuestion
import com.wafflestudio.snutt.diary.data.DiarySubmission
import com.wafflestudio.snutt.diary.dto.DiaryShortQuestionReply
import com.wafflestudio.snutt.diary.dto.request.DiaryAddQuestionRequestDto
import com.wafflestudio.snutt.diary.dto.request.DiarySubmissionRequestDto
import com.wafflestudio.snutt.diary.repository.DiaryActivityRepository
import com.wafflestudio.snutt.diary.repository.DiaryQuestionRepository
import com.wafflestudio.snutt.diary.repository.DiarySubmissionRepository
import com.wafflestudio.snutt.lectures.service.LectureService
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

interface DiaryService {
    suspend fun generateQuestionnaire(
        userId: String,
        lectureId: String,
        activityNames: List<String>,
    ): List<DiaryQuestion>

    suspend fun getActiveActivities(): List<DiaryActivity>

    suspend fun getAllActivities(): List<DiaryActivity>

    suspend fun getActiveQuestions(): List<DiaryQuestion>

    suspend fun submitDiary(
        userId: String,
        request: DiarySubmissionRequestDto,
    )

    suspend fun getMySubmissions(
        userId: String,
        year: Int,
        semester: Semester,
    ): List<DiarySubmission>

    suspend fun getSubmissionIdShortQuestionRepliesMap(submissions: List<DiarySubmission>): Map<String, List<DiaryShortQuestionReply>>

    suspend fun addOrEnableActivity(name: String)

    suspend fun disableActivity(name: String)

    suspend fun addQuestion(request: DiaryAddQuestionRequestDto)

    suspend fun removeQuestion(questionId: String)
}

@Service
class DiaryServiceImpl(
    private val diaryActivityRepository: DiaryActivityRepository,
    private val diaryQuestionRepository: DiaryQuestionRepository,
    private val diarySubmissionRepository: DiarySubmissionRepository,
    private val lectureService: LectureService,
) : DiaryService {
    override suspend fun generateQuestionnaire(
        userId: String,
        lectureId: String,
        activityNames: List<String>,
    ): List<DiaryQuestion> {
        val activities = diaryActivityRepository.findByNameIn(activityNames)
        val questions = diaryQuestionRepository.findByTargetActivitiesContainsAndActiveTrue(activities)
        val answeredQuestionIds =
            diarySubmissionRepository
                .findAllByUserIdAndLectureIdOrderByCreatedAt(userId, lectureId)
                .flatMap { it.questionIds }
                .toSet()

        return questions
            .filterNot { question -> question.id in answeredQuestionIds }
            .shuffled()
            .take(3)
    }

    override suspend fun getActiveActivities(): List<DiaryActivity> = diaryActivityRepository.findAllByActiveTrue()

    override suspend fun getAllActivities(): List<DiaryActivity> = diaryActivityRepository.findAll().toList()

    override suspend fun getActiveQuestions(): List<DiaryQuestion> = diaryQuestionRepository.findAllByActiveTrue()

    override suspend fun submitDiary(
        userId: String,
        request: DiarySubmissionRequestDto,
    ) {
        val lecture = lectureService.getByIdOrNull(request.lectureId)!!
        val activities = diaryActivityRepository.findByNameIn(request.activities).toList()
        if (diaryQuestionRepository.countByIdIn(request.questionIds) != request.questionIds.size) {
            throw DiaryQuestionNotFoundException
        }
        if (activities.size < request.activities.size) {
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
                activityTypeIds = activities.map { it.id!! },
                year = lecture.year,
                semester = lecture.semester,
            )
        diarySubmissionRepository.save(submission)
    }

    override suspend fun getMySubmissions(
        userId: String,
        year: Int,
        semester: Semester,
    ): List<DiarySubmission> =
        diarySubmissionRepository.findAllByUserIdAndYearAndSemesterOrderByCreatedAt(
            userId,
            year,
            semester,
        )

    override suspend fun getSubmissionIdShortQuestionRepliesMap(
        submissions: List<DiarySubmission>,
    ): Map<String, List<DiaryShortQuestionReply>> {
        val questions = diaryQuestionRepository.findAllByIdIn(submissions.flatMap { it.questionIds }).associateBy { it.id }
        return submissions.associate { submission ->
            val shortQuestionReplies =
                submission.questionIds.mapIndexedNotNull { index, questionId ->
                    questions[questionId]?.let { question ->
                        DiaryShortQuestionReply(
                            question = question.shortQuestion,
                            answer = question.shortAnswers[submission.answerIndexes[index]],
                        )
                    }
                }
            submission.id!! to shortQuestionReplies
        }
    }

    override suspend fun addOrEnableActivity(name: String) {
        val activityType = diaryActivityRepository.findByName(name) ?: DiaryActivity(name = name, active = true)
        activityType.active = true
        diaryActivityRepository.save(activityType)
    }

    override suspend fun disableActivity(name: String) {
        val activityType = diaryActivityRepository.findByName(name) ?: return
        activityType.active = false
        diaryActivityRepository.save(activityType)
    }

    override suspend fun addQuestion(request: DiaryAddQuestionRequestDto) {
        val question =
            DiaryQuestion(
                answers = request.answers,
                shortAnswers = request.shortAnswers,
                question = request.question,
                shortQuestion = request.shortQuestion,
                targetActivities = diaryActivityRepository.findByNameIn(request.targetActivities),
            )
        diaryQuestionRepository.save(question)
    }

    override suspend fun removeQuestion(questionId: String) {
        val question = diaryQuestionRepository.findById(questionId) ?: return
        question.active = false
        diaryQuestionRepository.save(question)
    }
}
