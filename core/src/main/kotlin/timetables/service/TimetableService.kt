package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.common.dynamiclink.client.DynamicLinkClient
import com.wafflestudio.snu4t.common.dynamiclink.dto.DynamicLinkResponse
import com.wafflestudio.snu4t.common.enum.TimetableTheme
import com.wafflestudio.snu4t.common.exception.TimetableNotFoundException
import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.timetables.data.Timetable
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import timetables.dto.TimetableBriefDto
import java.time.Instant

interface TimetableService {
    suspend fun getBriefs(userId: String): List<TimetableBriefDto>
    suspend fun getLink(timetableId: String): DynamicLinkResponse

    suspend fun copy(userId: String, timetableId: String, title: String? = null): Timetable
    suspend fun createDefaultTable(userId: String)
}

@Service
class TimetableServiceImpl(
    private val timetableRepository: TimetableRepository,
    private val dynamicLinkClient: DynamicLinkClient,
    private val coursebookService: CoursebookService,
    @Value("\${google.firebase.dynamic-link.link-prefix}") val linkPrefix: String,
) : TimetableService {
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

    override suspend fun copy(userId: String, timetableId: String, title: String?): Timetable {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        val copiedTimetable = timetable.copy(
            id = null,
            userId = userId,
            updatedAt = Instant.now(),
            title = title ?: timetable.title
        )
        return copyWithUniqueTitle(copiedTimetable, copiedTimetable.title)
    }

    private suspend fun copyWithUniqueTitle(timetable: Timetable, title: String): Timetable {
        val latestTitle = timetableRepository.findLatestChildTimetable(
            timetable.userId,
            timetable.year,
            timetable.semester,
            title
        )?.title ?: return timetableRepository.save(timetable)

        val countMatch = Regex("\\((\\d+)\\)")
            .findAll(latestTitle)
            .lastOrNull()
            ?.value
            ?: ""

        val count = countMatch
            .filter { it.isDigit() }
            .toIntOrNull()
            ?: 0
        val baseTitle = latestTitle.replace(Regex("\\s\\($count\\)$"), "")

        timetable.title = "$baseTitle (${count + 1})"

        return timetableRepository.save(timetable)
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
}
