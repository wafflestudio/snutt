package com.wafflestudio.snu4t.sharedtimetable.repository

import com.wafflestudio.snu4t.sharedtimetable.data.SharedTimeTable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SharedTimeTableRepository : CoroutineCrudRepository<SharedTimeTable, String> {
    suspend fun findSharedTimeTableByIdAndIsDeletedFalse(timetableId: String): SharedTimeTable?
    suspend fun findAllByUserIdAndIsDeletedFalse(userId: String): List<SharedTimeTable>
}
