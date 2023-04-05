package com.wafflestudio.snu4t.sharedtimetable.service

import com.wafflestudio.snu4t.common.exception.NotSharedTimetableOwnerException
import com.wafflestudio.snu4t.common.exception.SharedTimetableAlreadyExistsException
import com.wafflestudio.snu4t.common.exception.SharedTimetableNotFoundException
import com.wafflestudio.snu4t.common.exception.TimetableNotFoundException
import com.wafflestudio.snu4t.sharedtimetable.data.SharedTimetable
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableBriefDto
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableDetailDto
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableListDto
import com.wafflestudio.snu4t.sharedtimetable.repository.SharedTimetableRepository
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface SharedTimetableService {
    suspend fun gets(userId: String): SharedTimetableListDto
    suspend fun get(sharedTimetableId: String): SharedTimetableDetailDto
    suspend fun add(userId: String, title: String, timetableId: String): SharedTimetable
    suspend fun update(title: String, timetableId: String): SharedTimetable
    suspend fun delete(userId: String, timetableId: String)
}

@Service
class SharedTimetableServiceImpl(
    private val timetableRepository: TimetableRepository,
    private val sharedTimetableRepository: SharedTimetableRepository,
) : SharedTimetableService {
    override suspend fun gets(userId: String): SharedTimetableListDto {
        val sharedTimetables = sharedTimetableRepository.findAllByUserIdAndIsDeletedFalse(userId)
        val timetableIds = sharedTimetables.map { it.timetableId }
        val validTimetables = timetableRepository.findAllById(timetableIds).toList().map { it.id }
        val sharedTimetableList = sharedTimetables
            .map {
                SharedTimetableBriefDto(
                    id = it.id!!,
                    title = it.title,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    isValid = validTimetables.contains(it.timetableId),
                    year = it.year,
                    semester = it.semester.value
                )
            }
        return SharedTimetableListDto(sharedTimetableList)
    }
    override suspend fun get(sharedTimetableId: String): SharedTimetableDetailDto {
        val sharedTimetable = sharedTimetableRepository.findSharedTimetableByIdAndIsDeletedFalse(sharedTimetableId) ?: throw SharedTimetableNotFoundException
        // TODO: 시간표 삭제시 공유시간표도 같이 삭제하고 로그 찍기
        val timetable = timetableRepository.findById(sharedTimetable.timetableId) ?: throw TimetableNotFoundException
        return SharedTimetableDetailDto(
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
                timetableOwnerId = timetable.userId,
                title = title,
                timetableId = timetableId,
                year = timetable.year,
                semester = timetable.semester
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
