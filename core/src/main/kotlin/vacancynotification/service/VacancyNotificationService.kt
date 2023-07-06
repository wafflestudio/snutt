package com.wafflestudio.snu4t.vacancynotification.service

import com.wafflestudio.snu4t.common.exception.DuplicateVacancyNotificationException
import com.wafflestudio.snu4t.common.exception.InvalidRegistrationForPreviousSemesterCourseException
import com.wafflestudio.snu4t.common.exception.LectureNotFoundException
import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import com.wafflestudio.snu4t.vacancynotification.data.VacancyNotification
import com.wafflestudio.snu4t.vacancynotification.repository.VacancyNotificationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

interface VacancyNotificationService {
    suspend fun getVacancyNotificationLectures(userId: String): List<Lecture>
    suspend fun addVacancyNotification(userId: String, lectureId: String): VacancyNotification
    suspend fun deleteVacancyNotification(lectureId: String)
    suspend fun deleteAll()
}

@Service
class VacancyNotificationServiceImpl(
    private val vacancyNotificationRepository: VacancyNotificationRepository,
    private val lectureRepository: LectureRepository,
    private val coursebookService: CoursebookService
) : VacancyNotificationService {
    override suspend fun getVacancyNotificationLectures(userId: String): List<Lecture> =
        vacancyNotificationRepository.findAllByUserId(userId).map { it.lectureId }
            .let { lectureRepository.findAllById(it) }.toList()

    override suspend fun addVacancyNotification(userId: String, lectureId: String): VacancyNotification =
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

    override suspend fun deleteVacancyNotification(lectureId: String) {
        vacancyNotificationRepository.deleteByLectureId(lectureId)
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
