package com.wafflestudio.snutt.diary.service

import com.wafflestudio.snutt.common.cache.Cache
import com.wafflestudio.snutt.common.cache.CacheKey
import com.wafflestudio.snutt.common.push.DeeplinkType
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.notification.data.PushPreferenceType
import com.wafflestudio.snutt.notification.service.PushService
import com.wafflestudio.snutt.semester.service.SemesterService
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

interface DiaryNotifierService {
    suspend fun sendNotifier()
}

@Service
class DiaryNotifierServiceImpl(
    private val timetableRepository: TimetableRepository,
    private val semesterService: SemesterService,
    private val pushService: PushService,
    private val cache: Cache,
) : DiaryNotifierService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        const val SAMPLE_RATE = 0.1
    }

    @Scheduled(cron = "0 0 19 * * MON,WED,FRI", zone = "Asia/Seoul")
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
                        .filter { it.lectures.size > 2 }
                        .associateBy { it.userId }
                val targetedPushMessages =
                    sampledUserIdPrimaryTimetableMap
                        .mapNotNull {
                            val targetLecture = it.value.lectures.randomOrNull() ?: return@mapNotNull null
                            it.key to buildPushMessage(targetLecture.lectureId!!, targetLecture.courseTitle)
                        }.toMap()

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
