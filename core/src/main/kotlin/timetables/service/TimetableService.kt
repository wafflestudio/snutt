package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.common.dynamiclink.client.DynamicLinkClient
import com.wafflestudio.snu4t.common.dynamiclink.dto.DynamicLinkResponse
import com.wafflestudio.snu4t.common.exception.TimetableNotFoundException
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
    suspend fun copy(userId: String, timetableId: String): Timetable
}

@Service
class TimetableServiceImpl(
    private val timetableRepository: TimetableRepository,
    private val dynamicLinkClient: DynamicLinkClient,
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

    override suspend fun copy(userId: String, timetableId: String): Timetable {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        val copiedTimetable = timetable.copy(
            id = null,
            userId = userId,
            updatedAt = Instant.now()
        )
        return copyWithUniqueTitle(copiedTimetable, copiedTimetable.title)
    }

    private suspend fun copyWithUniqueTitle(timetable: Timetable, title: String): Timetable {
        var trialCnt = 0
        val newTitleSequence = generateSequence { if (trialCnt == 0) title else "$title(${trialCnt++})" }

        return newTitleSequence.first { isUniqueTimetableTitle(timetable) }.let {
            timetableRepository.save(timetable)
        }
    }

    private suspend fun isUniqueTimetableTitle(timetable: Timetable): Boolean = !timetableRepository.existsByUserIdAndYearAndSemesterAndTitle(timetable.userId, timetable.year, timetable.semester, timetable.title)
}
