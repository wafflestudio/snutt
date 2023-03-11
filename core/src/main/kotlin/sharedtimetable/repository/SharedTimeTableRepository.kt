package com.wafflestudio.snu4t.sharedtimetable.repository

import com.wafflestudio.snu4t.sharedtimetable.data.SharedTimeTable
import com.wafflestudio.snu4t.sharedtimetable.service.SharedTimeTableService
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.StringJoiner

@Repository
interface SharedTimeTableRepository : CoroutineCrudRepository<SharedTimeTable, String> {
    suspend fun  findSharedTimeTableById(timetableId: String): SharedTimeTable?
    suspend fun findSharedTimeTableByIdAndDeletedFalse(timetableId: String): SharedTimeTable?
    suspend fun findAllByUserIdAndDeletedFalse(userId: String): List<SharedTimeTable>
}
