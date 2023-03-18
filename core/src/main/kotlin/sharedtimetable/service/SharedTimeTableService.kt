package com.wafflestudio.snu4t.sharedtimetable.service

import com.wafflestudio.snu4t.common.exception.NotSharedTimetableOwnerException
import com.wafflestudio.snu4t.common.exception.SharedTimetableAlreadyExistsException
import com.wafflestudio.snu4t.common.exception.SharedTimetableNotFoundException
import com.wafflestudio.snu4t.common.exception.TimetableNotFoundException
import com.wafflestudio.snu4t.sharedtimetable.data.SharedTimetable
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableDetailResponse
import com.wafflestudio.snu4t.sharedtimetable.repository.SharedTimetableRepository
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface SharedTimetableService {
    suspend fun gets(userId: String): List<SharedTimetable>
    suspend fun get(sharedTimetableId: String): SharedTimetableDetailResponse
    suspend fun add(userId: String, title: String, timetableId: String): SharedTimetable
    suspend fun update(title: String, timetableId: String): SharedTimetable
    suspend fun delete(userId: String, timetableId: String)
}

@Service
class SharedTimetableServiceImpl(
    private val timetableRepository: TimetableRepository,
    private val sharedTimetableRepository: SharedTimetableRepository,
) : SharedTimetableService {
    override suspend fun gets(userId: String): List<SharedTimetable> {
        val sharedTimetables = sharedTimetableRepository.findAllByUserIdAndIsDeletedFalse(userId)
        val timetableIds = sharedTimetables.map { it.timetableId }
        val validTimetableIds = timetableRepository.findAllById(timetableIds).toList().map { it.id }
        return sharedTimetables
            .filter { validTimetableIds.contains(it.timetableId) }
    }
    override suspend fun get(sharedTimetableId: String): SharedTimetableDetailResponse {
        val sharedTimetable = sharedTimetableRepository.findSharedTimetableByIdAndIsDeletedFalse(sharedTimetableId) ?: throw SharedTimetableNotFoundException
        // TODO: 시간표 삭제시 공유시간표도 같이 삭제하고 로그 찍기
        val timetable = timetableRepository.findById(sharedTimetable.timetableId) ?: throw TimetableNotFoundException
        return SharedTimetableDetailResponse(
            id = sharedTimetableId,
            userId = sharedTimetable.userId,
            title = sharedTimetable.title,
            timetable = timetable,
        )
    }
    override suspend fun add(userId: String, title: String, timetableId: String): SharedTimetable {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        val existingTimetableIds = sharedTimetableRepository.findAllByUserIdAndIsDeletedFalse(userId).map { it.timetableId }
        if (existingTimetableIds.contains(timetableId)) {
            throw SharedTimetableAlreadyExistsException
        }
        return sharedTimetableRepository.save(
            SharedTimetable(
                userId = userId,
                title = title,
                timetableId = timetableId,
            )
        )
    }

    override suspend fun update(title: String, timetableId: String): SharedTimetable {
        val sharedTimetable = sharedTimetableRepository.findSharedTimetableByIdAndIsDeletedFalse(timetableId) ?: throw SharedTimetableNotFoundException
        sharedTimetable.title = title
        sharedTimetable.updatedAt = LocalDateTime.now()
        sharedTimetableRepository.save(sharedTimetable)
        return sharedTimetable
    }

    override suspend fun delete(userId: String, timetableId: String) {
        val sharedTimetable = sharedTimetableRepository.findSharedTimetableByIdAndIsDeletedFalse(timetableId) ?: throw SharedTimetableNotFoundException
        if (sharedTimetable.userId != userId) {
            throw NotSharedTimetableOwnerException
        }
        sharedTimetable.isDeleted = true
        sharedTimetableRepository.save(sharedTimetable)
    }
}
