package com.wafflestudio.snu4t.vacancynotification.service

import com.wafflestudio.snu4t.common.exception.DuplicateVacancyNotificationException
import com.wafflestudio.snu4t.common.exception.InvalidRegistrationForPreviousSemesterCourseException
import com.wafflestudio.snu4t.common.exception.LectureNotFoundException
import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.lectures.service.LectureService
import com.wafflestudio.snu4t.vacancynotification.data.VacancyNotification
import com.wafflestudio.snu4t.vacancynotification.repository.VacancyNotificationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

interface VacancyNotificationService {
    suspend fun addVacancyNotification(userId: String, lectureId: String): VacancyNotification
    suspend fun getVacancyNotifications(userId: String): List<VacancyNotification>
    suspend fun getVacancyNotification(userId: String, lectureId: String): VacancyNotification
    suspend fun deleteVacancyNotification(id: String)
}

@Service
class VacancyNotificationServiceImpl(
    private val vacancyNotificationRepository: VacancyNotificationRepository,
    private val lectureService: LectureService,
    private val coursebookService: CoursebookService
) : VacancyNotificationService {
    override suspend fun addVacancyNotification(userId: String, lectureId: String): VacancyNotification =
        coroutineScope {
            val deferredLecture = async { lectureService.getByIdOrNull(lectureId) }
            val deferredLatestCoursebook = async { coursebookService.getLatestCoursebook() }
            val (lecture, latestCoursebook) = deferredLecture.await() to deferredLatestCoursebook.await()

            if (lecture == null) throw LectureNotFoundException
            if (!(lecture.year == latestCoursebook.year && lecture.semester == latestCoursebook.semester)) {
                throw InvalidRegistrationForPreviousSemesterCourseException
            }

            trySave(VacancyNotification(userId = userId, lectureId = lectureId, coursebookId = latestCoursebook.id!!))
        }

    override suspend fun getVacancyNotifications(userId: String): List<VacancyNotification> =
        vacancyNotificationRepository.findAllByUserId(userId).toList()

    override suspend fun getVacancyNotification(userId: String, lectureId: String): VacancyNotification =
        vacancyNotificationRepository.findFirstByUserIdAndLectureId(userId, lectureId)

    override suspend fun deleteVacancyNotification(id: String) {
        vacancyNotificationRepository.deleteById(id)
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
