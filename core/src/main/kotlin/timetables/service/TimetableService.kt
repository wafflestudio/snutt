package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.common.dynamiclink.client.DynamicLinkClient
import com.wafflestudio.snu4t.common.dynamiclink.dto.DynamicLinkResponse
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.enum.TimetableTheme
import com.wafflestudio.snu4t.common.exception.DuplicateTimetableTitleException
import com.wafflestudio.snu4t.common.exception.PrimaryTimetableNotFoundException
import com.wafflestudio.snu4t.common.exception.TableDeleteErrorException
import com.wafflestudio.snu4t.common.exception.TimetableNotFoundException
import com.wafflestudio.snu4t.common.exception.TimetableNotPrimaryException
import com.wafflestudio.snu4t.coursebook.data.CoursebookDto
import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.timetables.data.Timetable
import com.wafflestudio.snu4t.timetables.dto.request.TimetableAddRequestDto
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

interface TimetableService {
    suspend fun getTimetables(userId: String): List<Timetable>
    suspend fun getMostRecentlyUpdatedTimetable(userId: String): Timetable
    fun getTimetablesBySemester(userId: String, year: Int, semester: Semester): Flow<Timetable>
    suspend fun addTimetable(userId: String, timetableRequest: TimetableAddRequestDto): Timetable
    suspend fun getTimetableLink(userId: String, timetableId: String): DynamicLinkResponse
    suspend fun getTimetable(userId: String, timetableId: String): Timetable
    suspend fun modifyTimetableTitle(userId: String, timetableId: String, title: String): Timetable
    suspend fun deleteTimetable(userId: String, timetableId: String)
    suspend fun modifyTimetableTheme(userId: String, timetableId: String, theme: TimetableTheme): Timetable
    suspend fun copyTimetable(userId: String, timetableId: String, title: String? = null): Timetable
    suspend fun getUserPrimaryTable(userId: String, year: Int, semester: Semester): Timetable
    suspend fun getCoursebooksWithPrimaryTable(userId: String): List<CoursebookDto>
    suspend fun createDefaultTable(userId: String)
    suspend fun setPrimary(userId: String, timetableId: String)
    suspend fun unSetPrimary(userId: String, timetableId: String)
}

@Service
class TimetableServiceImpl(
    private val timetableRepository: TimetableRepository,
    private val dynamicLinkClient: DynamicLinkClient,
    private val coursebookService: CoursebookService,
    @Value("\${google.firebase.dynamic-link.link-prefix}") val linkPrefix: String,
) : TimetableService {
    override suspend fun getTimetables(userId: String): List<Timetable> =
        timetableRepository.findAllByUserId(userId).toList()

    override suspend fun getMostRecentlyUpdatedTimetable(userId: String): Timetable =
        timetableRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId) ?: throw TimetableNotFoundException

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
            isPrimary = timetableRepository
                .findAllByUserIdAndYearAndSemester(userId, timetableRequest.year, timetableRequest.semester)
                .toList()
                .isEmpty()
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
        if (timetableRepository.countAllByUserId(userId) <= 1L) throw TableDeleteErrorException
        getTimetable(userId, timetableId)
        timetableRepository.deleteById(timetableId)
    }

    override suspend fun copyTimetable(userId: String, timetableId: String, title: String?): Timetable {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        val baseTitle = (title ?: timetable.title).replace(Regex("""\s\(\d+\)$"""), "")
        val latestCopiedTimetableNumber =
            getLatestCopiedTimetableNumber(userId, timetable.year, timetable.semester, title ?: timetable.title)
        return timetable.copy(
            id = null,
            userId = userId,
            updatedAt = Instant.now(),
            title = baseTitle + " (${latestCopiedTimetableNumber + 1})",
            isPrimary = false,
        ).let { timetableRepository.save(it) }
    }

    override suspend fun modifyTimetableTheme(userId: String, timetableId: String, theme: TimetableTheme): Timetable =
        getTimetable(userId, timetableId).apply { this.theme = theme }.let { timetableRepository.save(it) }

    override suspend fun getUserPrimaryTable(userId: String, year: Int, semester: Semester): Timetable {
        return timetableRepository.findByUserIdAndYearAndSemester(userId, year, semester)
            .toList()
            .ifEmpty { throw TimetableNotFoundException }
            .find { it.isPrimary == true } ?: throw PrimaryTimetableNotFoundException
    }

    override suspend fun getCoursebooksWithPrimaryTable(userId: String): List<CoursebookDto> {
        return timetableRepository.findAllByUserIdAndIsPrimaryTrue(userId)
            .map { CoursebookDto(it.year, it.semester) }
            .toSet()
            .sortedByDescending { it.order }
    }

    override suspend fun createDefaultTable(userId: String) {
        val coursebook = coursebookService.getLatestCoursebook()
        val timetable = Timetable(
            userId = userId,
            year = coursebook.year,
            semester = coursebook.semester,
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
        if(timetableRepository.existsByUserIdAndYearAndSemesterAndTitle(userId, year, semester, title)) {
            throw DuplicateTimetableTitleException
        }
    }

    override suspend fun setPrimary(userId: String, timetableId: String) {
        val newPrimaryTable = timetableRepository.findById(timetableId)
            ?: throw TimetableNotFoundException

        if (newPrimaryTable.isPrimary == true) {
            return
        }

        val primaryTableBefore = timetableRepository.findByUserIdAndYearAndSemesterAndIsPrimaryTrue(
            userId,
            newPrimaryTable.year,
            newPrimaryTable.semester
        )

        if (primaryTableBefore == null) {
            timetableRepository.save(newPrimaryTable.copy(isPrimary = true))
        } else {
            timetableRepository
                .saveAll(listOf(newPrimaryTable.copy(isPrimary = true), primaryTableBefore.copy(isPrimary = false)))
                .collect()
        }
    }

    override suspend fun unSetPrimary(userId: String, timetableId: String) {
        val table = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        if (table.isPrimary != true) throw TimetableNotPrimaryException
        timetableRepository.save(table.copy(isPrimary = false))
    }
}
