package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import timetables.dto.TimetableBriefDto

interface TimetableService {
    suspend fun getBriefs(userId: String): List<TimetableBriefDto>
}

@Service
class TimetableServiceImpl(
    private val timeTableRepository: TimetableRepository,
) : TimetableService {
    override suspend fun getBriefs(userId: String): List<TimetableBriefDto> =
        timeTableRepository.findAllByUserId(userId)
            .map(::TimetableBriefDto)
            .toList()
}
