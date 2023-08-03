package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.common.dynamiclink.client.DynamicLinkClient
import com.wafflestudio.snu4t.common.dynamiclink.dto.DynamicLinkResponse
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.enum.TimetableTheme
import com.wafflestudio.snu4t.common.exception.TimetableNotFoundException
import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.timetables.data.SemesterDto
import com.wafflestudio.snu4t.timetables.data.Timetable
import com.wafflestudio.snu4t.timetables.dto.TimetableDto
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import timetables.dto.TimetableBriefDto
import java.time.Instant

interface TimetableService {
    suspend fun getBriefs(userId: String): List<TimetableBriefDto>
    suspend fun getLink(timetableId: String): DynamicLinkResponse
    suspend fun getUserPrimaryTable(userId: String, year: Int, semester: Semester): TimetableDto

    suspend fun getRegisteredSemesters(userId: String): List<SemesterDto>

    suspend fun copy(userId: String, timetableId: String, title: String? = null): Timetable
    suspend fun createDefaultTable(userId: String)
    suspend fun setPrimary(userId: String, timetableId: String)
}

@Service
class TimetableServiceImpl(
    private val timetableRepository: TimetableRepository,
    private val dynamicLinkClient: DynamicLinkClient,
    private val coursebookService: CoursebookService,
    @Value("\${google.firebase.dynamic-link.link-prefix}") val linkPrefix: String,
) : TimetableService {
    companion object {
        private val titleCountRegex = """\((\d+)\)""".toRegex()
    }
    override suspend fun getBriefs(userId: String): List<TimetableBriefDto> =
        timetableRepository.findAllByUserId(userId)
            .map(::TimetableBriefDto)
            .toList()

    override suspend fun getLink(timetableId: String): DynamicLinkResponse {
        timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        return dynamicLinkClient.generateLink(
            linkPrefix,
            "snutt://share?timetableId=$timetableId"
        )
    }

    override suspend fun getUserPrimaryTable(userId: String, year: Int, semester: Semester): TimetableDto {
        return timetableRepository.findByUserIdAndYearAndSemesterAndIsPrimaryTrue(userId, year, semester)
            ?.let(::TimetableDto)
            ?: throw TimetableNotFoundException
    }

    override suspend fun getRegisteredSemesters(userId: String): List<SemesterDto> {
        return timetableRepository.findAllByUserId(userId)
            .map { SemesterDto(it.year, it.semester) }
            .distinctUntilChanged()
            .toList()
    }

    override suspend fun copy(userId: String, timetableId: String, title: String?): Timetable {
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

        listOfNotNull(
            newPrimaryTable.copy(isPrimary = true),
            primaryTableBefore?.copy(isPrimary = false)
        )
            .let(timetableRepository::saveAll)
            .collect()
    }
}
