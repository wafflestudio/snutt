package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.common.dynamiclink.client.DynamicLinkClient
import com.wafflestudio.snu4t.common.dynamiclink.dto.DynamicLinkResponse
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.enum.TimetableTheme
import com.wafflestudio.snu4t.common.exception.DuplicateTimetableTitleException
import com.wafflestudio.snu4t.common.exception.TimetableNotFoundException
import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.timetables.data.Timetable
import com.wafflestudio.snu4t.timetables.dto.request.TimetableAddRequestDto
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

interface TimetableService {
    fun getTimetables(userId: String): Flow<Timetable>
    suspend fun getMostRecentlyUpdatedTimetable(userId: String): Timetable
    fun getTimetablesBySemester(userId: String, year: Int, semester: Semester): Flow<Timetable>
    suspend fun addTimetable(userId: String, timetableRequest: TimetableAddRequestDto): Timetable
    suspend fun getTimetableLink(userId: String, timetableId: String): DynamicLinkResponse
    suspend fun getTimetable(userId: String, timetableId: String): Timetable
    suspend fun modifyTimetableTitle(userId: String, timetableId: String, title: String): Timetable
    suspend fun deleteTimetable(userId: String, timetableId: String)
    suspend fun modifyTimetableTheme(userId: String, timetableId: String, theme: TimetableTheme): Timetable
    suspend fun copyTimetable(userId: String, timetableId: String, title: String? = null): Timetable
    suspend fun createDefaultTable(userId: String)
}

@Service
class TimetableServiceImpl(
    private val timetableRepository: TimetableRepository,
    private val dynamicLinkClient: DynamicLinkClient,
    private val coursebookService: CoursebookService,
    @Value("\${google.firebase.dynamic-link.link-prefix}") val linkPrefix: String,
) : TimetableService {
    override fun getTimetables(userId: String): Flow<Timetable> =
        timetableRepository.findAllByUserId(userId)

    override suspend fun getMostRecentlyUpdatedTimetable(userId: String): Timetable =
        timetableRepository.findByUserIdOrderByUpdatedAtDesc(userId) ?: throw TimetableNotFoundException

    override fun getTimetablesBySemester(userId: String, year: Int, semester: Semester): Flow<Timetable> =
        timetableRepository.findAllByUserIdAndYearAndSemester(userId, year, semester)

    override suspend fun addTimetable(userId: String, timetableRequest: TimetableAddRequestDto): Timetable {
        validateTimetableTitle(userId, timetableRequest.year, timetableRequest.semester, timetableRequest.title)
        return Timetable(
            userId = userId,
            year = timetableRequest.year,
            semester = timetableRequest.semester,
            title = timetableRequest.title,
            theme = TimetableTheme.SNUTT,
        ).let { timetableRepository.save(it) }
    }

    override suspend fun getTimetableLink(userId: String, timetableId: String): DynamicLinkResponse {
        timetableRepository.findByUserIdAndId(userId, timetableId) ?: throw TimetableNotFoundException
        return dynamicLinkClient.generateLink(
            linkPrefix,
            "snutt://share?timetableId=$timetableId"
        )
    }

    override suspend fun getTimetable(userId: String, timetableId: String): Timetable {
        return timetableRepository.findByUserIdAndId(userId, timetableId) ?: throw TimetableNotFoundException
    }

    override suspend fun modifyTimetableTitle(userId: String, timetableId: String, title: String): Timetable =
        getTimetable(userId, timetableId)
            .also { validateTimetableTitle(userId, it.year, it.semester, title) }
            .apply { this.title = title }
            .let { timetableRepository.save(it) }

    override suspend fun deleteTimetable(userId: String, timetableId: String) {
        getTimetable(userId, timetableId)
        timetableRepository.deleteById(timetableId)
    }

    override suspend fun modifyTimetableTheme(userId: String, timetableId: String, theme: TimetableTheme): Timetable =
        getTimetable(userId, timetableId).apply { this.theme = theme }.let { timetableRepository.save(it) }

    override suspend fun copyTimetable(userId: String, timetableId: String, title: String?): Timetable {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        val baseTitle = (title ?: timetable.title).replace(Regex("""\s\(\d+\)$"""), "")
        val latestCopiedTimetableNumber =
            getLatestCopiedTimetableNumber(userId, timetable.year, timetable.semester, title ?: timetable.title)
        return timetable.copy(
            id = null,
            userId = userId,
            updatedAt = Instant.now(),
            title = baseTitle + " (${latestCopiedTimetableNumber + 1})"
        ).let { timetableRepository.save(it) }
    }

    override suspend fun createDefaultTable(userId: String) {
        val courseBook = coursebookService.getLatestCoursebook()
        val timetable = Timetable(
            userId = userId,
            year = courseBook.year,
            semester = courseBook.semester,
            title = "나의 시간표",
            theme = TimetableTheme.SNUTT,
        )
        timetableRepository.save(timetable)
    }

    private suspend fun getLatestCopiedTimetableNumber(
        userId: String,
        year: Int,
        semester: Semester,
        title: String
    ): Int {
        val baseTitle = title.replace(Regex("""\s\(\d+\)$"""), "")
        return timetableRepository.findLatestChildTimetable(userId, year, semester, baseTitle)?.title
            ?.replace(baseTitle, "")?.filter { it.isDigit() }?.toIntOrNull() ?: 0
    }

    private suspend fun validateTimetableTitle(userId: String, year: Int, semester: Semester, title: String) {
        timetableRepository.findByUserIdAndYearAndSemesterAndTitle(userId, year, semester, title)
            ?.let { throw DuplicateTimetableTitleException }
    }
}
