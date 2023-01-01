package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.timetables.data.TimeTable
import com.wafflestudio.snu4t.timetables.repository.TimeTableRepository
import com.wafflestudio.snu4t.users.data.User
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

interface TimeTableService {
    suspend fun getTimeTablesOfUser(user: User): List<TimeTable>
}

@Service
class TimeTableServiceImpl(private val timeTableRepository: TimeTableRepository) : TimeTableService {
    override suspend fun getTimeTablesOfUser(user: User): List<TimeTable> = timeTableRepository.getAllByUserId(user.id!!).toList()
}
