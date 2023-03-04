package com.wafflestudio.snu4t.sharedtimetable.service

import com.wafflestudio.snu4t.common.exception.SharedTimeTableNotFoundException
import com.wafflestudio.snu4t.common.exception.TimeTableNotFoundException
import com.wafflestudio.snu4t.sharedtimetable.data.SharedTimeTable
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimeTableDetailResponse
import com.wafflestudio.snu4t.sharedtimetable.repository.SharedTimeTableRepository
import com.wafflestudio.snu4t.timetables.repository.TimeTableRepository
import org.springframework.stereotype.Service

interface SharedTimeTableService {
    suspend fun getSharedTimeTables(userId: String): List<SharedTimeTable>
    suspend fun getSharedTimeTable(userId: String, sharedTimeTableId: String): SharedTimeTableDetailResponse
    suspend fun addSharedTimeTable(userId: String, title: String, timeTableId: String): SharedTimeTable
    suspend fun deleteSharedTimeTable(userId: String, timeTableId: String)
}

@Service
class SharedTimeTableServiceImpl(
    private val timeTableRepository: TimeTableRepository,
    private val sharedTimeTableRepository: SharedTimeTableRepository,
) : SharedTimeTableService {
    override suspend fun getSharedTimeTables(userId: String): List<SharedTimeTable> = sharedTimeTableRepository.findAllByUserId(userId)
    override suspend fun getSharedTimeTable(userId: String, sharedTimeTableId: String): SharedTimeTableDetailResponse {
        val sharedTimeTable = sharedTimeTableRepository.findSharedTimeTableByUserIdAndId(userId, sharedTimeTableId) ?: throw SharedTimeTableNotFoundException
        val timetable = timeTableRepository.findById(sharedTimeTable.timetableId) ?: throw TimeTableNotFoundException
        return SharedTimeTableDetailResponse(
            id = sharedTimeTableId,
            userId = sharedTimeTable.userId,
            title = sharedTimeTable.title,
            timetable = timetable
        )
    }
    override suspend fun addSharedTimeTable(userId: String, title: String, timeTableId: String): SharedTimeTable {
        val timeTable = timeTableRepository.findById(timeTableId) ?: throw TimeTableNotFoundException
        return sharedTimeTableRepository.save(
            SharedTimeTable(
                userId = userId,
                title = title,
                timetableId = timeTableId
            )
        )
    }

    override suspend fun deleteSharedTimeTable(userId: String, timeTableId: String) = sharedTimeTableRepository.deleteSharedTimeTableByUserIdAndId(userId, timeTableId)
}
