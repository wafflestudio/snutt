package com.wafflestudio.snutt.diary.service

import com.wafflestudio.snutt.common.cache.Cache
import com.wafflestudio.snutt.common.cache.CacheKey
import com.wafflestudio.snutt.common.exception.DiaryActivityNotFoundException
import com.wafflestudio.snutt.common.exception.DiaryQuestionNotFoundException
import com.wafflestudio.snutt.common.exception.DiarySubmissionNotFoundException
import com.wafflestudio.snutt.common.exception.LectureNotFoundException
import com.wafflestudio.snutt.common.exception.TimetableNotFoundException
import com.wafflestudio.snutt.common.push.DeeplinkType
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.diary.data.DiaryActivity
import com.wafflestudio.snutt.diary.data.DiaryQuestion
import com.wafflestudio.snutt.diary.data.DiaryQuestionnaire
import com.wafflestudio.snutt.diary.data.DiarySubmission
import com.wafflestudio.snutt.diary.dto.DiaryShortQuestionReply
import com.wafflestudio.snutt.diary.dto.request.DiaryAddQuestionRequestDto
import com.wafflestudio.snutt.diary.dto.request.DiarySubmissionRequestDto
import com.wafflestudio.snutt.diary.repository.DiaryActivityRepository
import com.wafflestudio.snutt.diary.repository.DiaryQuestionRepository
import com.wafflestudio.snutt.diary.repository.DiarySubmissionRepository
import com.wafflestudio.snutt.lectures.service.LectureService
import com.wafflestudio.snutt.notification.data.PushPreferenceType
import com.wafflestudio.snutt.notification.service.PushService
import com.wafflestudio.snutt.semester.service.SemesterService
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

interface DiaryService {
    suspend fun generateQuestionnaire(
        userId: String,
        lectureId: String,
        activityNames: List<String>,
    ): DiaryQuestionnaire

    suspend fun getActiveActivities(): List<DiaryActivity>

    suspend fun getAllActivities(): List<DiaryActivity>

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

    suspend fun addOrEnableActivity(name: String)

    suspend fun disableActivity(name: String)

    suspend fun addQuestion(request: DiaryAddQuestionRequestDto)

    suspend fun removeQuestion(questionId: String)

    suspend fun sendNotifier()
}

@Service
class DiaryServiceImpl(
    private val diaryActivityRepository: DiaryActivityRepository,
    private val diaryQuestionRepository: DiaryQuestionRepository,
    private val diarySubmissionRepository: DiarySubmissionRepository,
    private val timetableRepository: TimetableRepository,
    private val lectureService: LectureService,
    private val semesterService: SemesterService,
    private val pushService: PushService,
    private val cache: Cache,
) : DiaryService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        const val SAMPLE_RATE = 0.1
    }

    override suspend fun generateQuestionnaire(
        userId: String,
        lectureId: String,
        activityNames: List<String>,
    ): DiaryQuestionnaire {
        val activityIds = diaryActivityRepository.findByNameIn(activityNames).map { it.id!! }
        val availableQuestions = diaryQuestionRepository.findByTargetActivityIdsContainsAndActiveTrue(activityIds)
        val recentlyAnsweredQuestionIds =
            diarySubmissionRepository
                .findAllByUserIdAndLectureIdAndCreatedAtAfterOrderByCreatedAtDesc(
                    userId,
                    lectureId,
                    Instant.now().minus(Duration.ofDays(14)),
                ).flatMap { submission -> submission.questionAnswers.map { it.questionId } }

        val lecture = lectureService.getByIdOrNull(lectureId) ?: throw LectureNotFoundException

        val userTimetable =
            timetableRepository.findByUserIdAndYearAndSemesterAndIsPrimaryTrue(userId, lecture.year, lecture.semester)
                ?: throw TimetableNotFoundException

        val nextLectureCandidates = userTimetable.lectures.filterNot { it.lectureId == lectureId }
        val nextLecture = nextLectureCandidates.random()

        val questions =
            availableQuestions
                .filterNot { question -> question.id in recentlyAnsweredQuestionIds }
                .shuffled()
                .take(3)

        return DiaryQuestionnaire(
            lectureTitle = lecture.courseTitle,
            questions = questions,
            nextLectureId = nextLecture.lectureId!!,
            nextLectureTitle = nextLecture.courseTitle,
        )
    }

    override suspend fun getActiveActivities(): List<DiaryActivity> = diaryActivityRepository.findAllByActiveTrue()

    override suspend fun getAllActivities(): List<DiaryActivity> = diaryActivityRepository.findAll().toList()

    override suspend fun getActiveQuestions(): List<DiaryQuestion> = diaryQuestionRepository.findAllByActiveTrue()

    override suspend fun submitDiary(
        userId: String,
        request: DiarySubmissionRequestDto,
    ) {
        val lecture = lectureService.getByIdOrNull(request.lectureId) ?: throw LectureNotFoundException
        val activities = diaryActivityRepository.findByNameIn(request.activities)
        val questionIds = request.questionAnswers.map { it.questionId }
        if (diaryQuestionRepository.countByIdIn(questionIds) != questionIds.size) {
            throw DiaryQuestionNotFoundException
        }
        if (activities.size != request.activities.size) {
            throw DiaryActivityNotFoundException
        }
        val submission =
            DiarySubmission(
                userId = userId,
                comment = request.comment,
                lectureId = request.lectureId,
                courseTitle = lecture.courseTitle,
                questionAnswers = request.questionAnswers,
                activityIds = activities.map { it.id!! },
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
        val submission = diarySubmissionRepository.findById(submissionId)
        if (submission?.userId == userId) {
            diarySubmissionRepository.deleteById(submissionId)
        } else {
            throw DiarySubmissionNotFoundException
        }
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

    override suspend fun addOrEnableActivity(name: String) {
        val activity = diaryActivityRepository.findByName(name) ?: DiaryActivity(name = name, active = true)
        activity.active = true
        diaryActivityRepository.save(activity)
    }

    override suspend fun disableActivity(name: String) {
        val activity = diaryActivityRepository.findByName(name) ?: return
        activity.active = false
        diaryActivityRepository.save(activity)
    }

    override suspend fun addQuestion(request: DiaryAddQuestionRequestDto) {
        val targetActivityIds = diaryActivityRepository.findByNameIn(request.targetActivities).mapNotNull { it.id }
        if (targetActivityIds.size != request.targetActivities.size) {
            throw DiaryActivityNotFoundException
        }
        val question =
            DiaryQuestion(
                answers = request.answers,
                shortAnswers = request.shortAnswers,
                question = request.question,
                shortQuestion = request.shortQuestion,
                targetActivityIds = targetActivityIds,
            )
        diaryQuestionRepository.save(question)
    }

    override suspend fun removeQuestion(questionId: String) {
        val question = diaryQuestionRepository.findById(questionId) ?: return
        question.active = false
        diaryQuestionRepository.save(question)
    }

    @Scheduled(cron = "0 0 18 * * MON")
    override suspend fun sendNotifier() {
        val lockKey = CacheKey.LOCK_SEND_LECTURE_DIARY_NOTIFICATION.build()
        cache.withLock(lockKey) {
            try {
                val currentTime = Instant.now()
                val (currentYear, currentSemester) =
                    semesterService.getCurrentYearAndSemester(currentTime) ?: run {
                        logger.debug("현재 진행 중인 학기가 없습니다.")
                        return@withLock
                    }
                val sampledUserIdPrimaryTimetableMap =
                    timetableRepository
                        .samplePrimaryOfRateByYearAndSemester(SAMPLE_RATE, currentYear, currentSemester)
                        .toList()
                        .associateBy { it.userId }
                val targetedPushMessages =
                    sampledUserIdPrimaryTimetableMap.mapValues {
                        val targetLecture = it.value.lectures.random()
                        buildPushMessage(targetLecture.lectureId!!, targetLecture.courseTitle)
                    }

                pushService.sendTargetPushes(targetedPushMessages, PushPreferenceType.DIARY)
            } catch (e: Exception) {
                logger.error("강의 일기장 알림 전송 중 문제 발생", e)
            }
        }
    }

    private fun buildPushMessage(
        lectureId: String,
        courseTitle: String,
    ): PushMessage =
        PushMessage(
            title = "이번주 강의일기를 작성해보세요.",
            body = "최근 수강한 <$courseTitle> 강의에 대한 강의일기를 작성해보세요.\uD83D\uDCD4 ",
            urlScheme = DeeplinkType.DIARY.build(lectureId),
            isUrgentOnAndroid = false,
        )
}
