package com.wafflestudio.snu4t.sharedtimetable.repository

import com.wafflestudio.snu4t.sharedtimetable.data.SharedTimetable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SharedTimetableRepository : CoroutineCrudRepository<SharedTimetable, String> {
    suspend fun findSharedTimetableByIdAndIsDeletedFalse(timetableId: String): SharedTimetable?
    suspend fun findAllByUserIdAndIsDeletedFalse(userId: String): List<SharedTimetable>
}
