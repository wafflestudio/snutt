package com.wafflestudio.snutt.diary.service

import com.wafflestudio.snutt.common.cache.Cache
import com.wafflestudio.snutt.common.cache.CacheKey
import com.wafflestudio.snutt.common.exception.NotAllowedInProdException
import com.wafflestudio.snutt.common.push.DeeplinkType
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.config.PhaseUtils
import com.wafflestudio.snutt.notification.data.PushPreferenceType
import com.wafflestudio.snutt.notification.service.PushService
import com.wafflestudio.snutt.semester.service.SemesterService
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface DiaryNotifierService {
    suspend fun sendNotifier()

    suspend fun triggerNotifierManually()
}

@Service
class DiaryNotifierServiceImpl(
    private val timetableRepository: TimetableRepository,
    private val semesterService: SemesterService,
    private val pushService: PushService,
    private val cache: Cache,
) : DiaryNotifierService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val sampleRate: Double
        get() = if (PhaseUtils.getPhase().isProd) 0.1 else 1.0

    @Scheduled(cron = "0 0 19 * * MON,WED,FRI", zone = "Asia/Seoul")
    override suspend fun sendNotifier() {
        sendDiaryNotifications()
    }

    override suspend fun triggerNotifierManually() {
        if (PhaseUtils.getPhase().isProd) {
            throw NotAllowedInProdException
        }
        sendDiaryNotifications()
    }

    private suspend fun sendDiaryNotifications() {
        val lockKey = CacheKey.LOCK_SEND_LECTURE_DIARY_NOTIFICATION.build()
        cache.withLock(lockKey) {
            try {
                logger.debug("강의 일기장 알림 전송 작업을 시작합니다.")
                val currentTime = Instant.now()
                val pushId = PUSH_ID_FORMATTER.format(currentTime)
                val (currentYear, currentSemester) =
                    semesterService.getCurrentYearAndSemester(currentTime) ?: run {
                        logger.debug("현재 진행 중인 학기가 없습니다.")
                        return@withLock
                    }
                val totalCount = timetableRepository.countAllByIsPrimaryTrueAndYearAndSemester(currentYear, currentSemester)
                val sampledUserIdPrimaryTimetableMap =
                    timetableRepository
                        .samplePrimaryOfSizeByYearAndSemester((totalCount * sampleRate).toLong(), currentYear, currentSemester)
                        .toList()
                        .filter { it.lectures.size > 2 }
                        .associateBy { it.userId }
                val targetedPushMessages =
                    sampledUserIdPrimaryTimetableMap
                        .mapNotNull {
                            val targetLecture =
                                it.value.lectures
                                    .filterNot { lecture -> lecture.lectureId == null }
                                    .randomOrNull()
                                    ?: return@mapNotNull null
                            it.key to buildPushMessage(targetLecture.lectureId!!, targetLecture.courseTitle, pushId)
                        }.toMap()

                logger.info("[$pushId] ${targetedPushMessages.size}개의 강의 일기장 알림을 전송합니다.")
                val result = pushService.sendTargetPushes(targetedPushMessages, PushPreferenceType.DIARY)
                logger.info(
                    "[$pushId] 강의 일기장 알림 전송 완료: 대상 ${targetedPushMessages.size}명, " +
                        "푸시 설정 필터 후 ${result.userCount}명, FCM 토큰 ${result.tokenCount}개",
                )
            } catch (e: Exception) {
                logger.error("강의 일기장 알림 전송 중 문제 발생", e)
            }
        }
    }

    private fun buildPushMessage(
        lectureId: String,
        courseTitle: String,
        pushId: String,
    ): PushMessage =
        PushMessage(
            title = "이번주 강의일기를 작성해보세요.",
            body = "최근 수강한 <$courseTitle> 강의에 대한 강의일기를 작성해보세요.\uD83D\uDCD4 ",
            urlScheme = DeeplinkType.DIARY.build(lectureId, URLEncoder.encode(courseTitle, "UTF-8"), pushId = pushId),
            isUrgentOnAndroid = false,
        )

    companion object {
        private val PUSH_ID_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("'diary-'yyyyMMdd-HHmm").withZone(ZoneId.of("Asia/Seoul"))
    }
}
