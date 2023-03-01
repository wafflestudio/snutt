package com.wafflestudio.snu4t.sharedtimetable.service

import com.wafflestudio.snu4t.common.exception.TimeTableNotFoundException
import com.wafflestudio.snu4t.sharedtimetable.data.SharedTimeTable
import com.wafflestudio.snu4t.sharedtimetable.repository.SharedTimeTableRepository
import com.wafflestudio.snu4t.timetables.repository.TimeTableRepository
import com.wafflestudio.snu4t.timetables.service.TimeTableService
import org.springframework.stereotype.Service

interface SharedTimeTableService {
    suspend fun getSharedTimeTables(userId: String): List<SharedTimeTable>
    suspend fun addSharedTimeTable(userId: String, title: String, timeTableId: String): SharedTimeTable
}

@Service
class SharedTimeTableServiceImpl(
    private val timeTableRepository: TimeTableRepository,
    private val sharedTimeTableRepository: SharedTimeTableRepository,
): SharedTimeTableService {
    override suspend fun getSharedTimeTables(userId: String): List<SharedTimeTable> = sharedTimeTableRepository.findAllByUserId(userId)

    override suspend fun addSharedTimeTable(userId: String, title: String, timeTableId: String): SharedTimeTable {
        val timeTable = timeTableRepository.findById(timeTableId)?: throw TimeTableNotFoundException
        return sharedTimeTableRepository.save(
            SharedTimeTable(
                userId = userId,
                title = title,
                timeTable = timeTable
            )
        )
    }
}