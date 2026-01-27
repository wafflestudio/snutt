package com.wafflestudio.snutt.vacancynotification.service

import com.wafflestudio.snutt.common.exception.DuplicateVacancyNotificationException
import com.wafflestudio.snutt.common.exception.InvalidRegistrationForPreviousSemesterCourseException
import com.wafflestudio.snutt.common.exception.LectureNotFoundException
import com.wafflestudio.snutt.coursebook.service.CoursebookService
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.lectures.repository.LectureRepository
import com.wafflestudio.snutt.vacancynotification.data.VacancyNotification
import com.wafflestudio.snutt.vacancynotification.repository.VacancyNotificationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    ): VacancyNotification =
        coroutineScope {
            val deferredLecture = async { lectureRepository.findById(lectureId) }
            val deferredLatestCoursebook = async { coursebookService.getLatestCoursebook() }
            val (lecture, latestCoursebook) = deferredLecture.await() to deferredLatestCoursebook.await()

            if (lecture == null) throw LectureNotFoundException
            if (!(lecture.year == latestCoursebook.year && lecture.semester == latestCoursebook.semester)) {
                throw InvalidRegistrationForPreviousSemesterCourseException
            }

            trySave(VacancyNotification(userId = userId, lectureId = lectureId))
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
