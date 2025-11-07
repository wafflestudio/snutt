package com.wafflestudio.snutt.vacancynotification.service

import com.wafflestudio.snutt.common.exception.DuplicateVacancyNotificationException
import com.wafflestudio.snutt.common.exception.VacancyNotificationTemporarilyUnavailableException
import com.wafflestudio.snutt.coursebook.service.CoursebookService
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.lectures.repository.LectureRepository
import com.wafflestudio.snutt.vacancynotification.data.VacancyNotification
import com.wafflestudio.snutt.vacancynotification.repository.VacancyNotificationRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

interface VacancyNotificationService {
    suspend fun getVacancyNotificationLectures(userId: String): List<Lecture>

    suspend fun existsVacancyNotification(
        userId: String,
        lectureId: String,
    ): Boolean

    suspend fun addVacancyNotification(
        userId: String,
        lectureId: String,
    ): VacancyNotification

    suspend fun deleteVacancyNotification(
        userId: String,
        lectureId: String,
    )

    suspend fun deleteAll()
}

@Service
class VacancyNotificationServiceImpl(
    private val vacancyNotificationRepository: VacancyNotificationRepository,
    private val lectureRepository: LectureRepository,
    private val coursebookService: CoursebookService,
) : VacancyNotificationService {
    override suspend fun getVacancyNotificationLectures(userId: String): List<Lecture> =
        vacancyNotificationRepository
            .findAllByUserId(userId)
            .map { it.lectureId }
            .let { lectureRepository.findAllById(it) }
            .toList()

    override suspend fun existsVacancyNotification(
        userId: String,
        lectureId: String,
    ): Boolean = vacancyNotificationRepository.existsByUserIdAndLectureId(userId, lectureId)

    override suspend fun addVacancyNotification(
        userId: String,
        lectureId: String,
    ): VacancyNotification {
        throw VacancyNotificationTemporarilyUnavailableException // 2025-11-06 정보화본부 취소여석 정책이 바뀜에 따라 빈자리알림을 대응 전까지 일시 중단한다
    }

    override suspend fun deleteVacancyNotification(
        userId: String,
        lectureId: String,
    ) {
        vacancyNotificationRepository.deleteByUserIdAndLectureId(userId, lectureId)
    }

    override suspend fun deleteAll() {
        vacancyNotificationRepository.deleteAll()
    }

    private suspend fun trySave(vacancyNotification: VacancyNotification) =
        runCatching {
            vacancyNotificationRepository.save(vacancyNotification)
        }.getOrElse {
            when (it) {
                is DuplicateKeyException -> throw DuplicateVacancyNotificationException
                else -> throw it
            }
        }
}
