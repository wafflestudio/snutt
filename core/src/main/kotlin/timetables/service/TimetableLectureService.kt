package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.common.exception.CustomLectureResetException
import com.wafflestudio.snu4t.common.exception.DuplicateTimetableLectureException
import com.wafflestudio.snu4t.common.exception.LectureNotFoundException
import com.wafflestudio.snu4t.common.exception.LectureTimeOverlapException
import com.wafflestudio.snu4t.common.exception.TimetableNotFoundException
import com.wafflestudio.snu4t.common.exception.WrongSemesterException
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import com.wafflestudio.snu4t.lectures.utils.ClassTimeUtils
import com.wafflestudio.snu4t.timetables.data.Timetable
import com.wafflestudio.snu4t.timetables.data.TimetableLecture
import com.wafflestudio.snu4t.timetables.dto.request.CustomTimetableLectureAddLegacyRequestDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableLectureModifyLegacyRequestDto
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import com.wafflestudio.snu4t.timetables.utils.ColorUtils
import org.springframework.stereotype.Service

interface TimetableLectureService {
    suspend fun addLecture(userId: String, timetableId: String, lectureId: String, isForced: Boolean)
    suspend fun addCustomTimetableLecture(
        userId: String,
        timetableId: String,
        timetableLectureRequest: CustomTimetableLectureAddLegacyRequestDto,
        isForced: Boolean
    )

    suspend fun resetTimetableLecture(userId: String, timetableId: String, timetableLectureId: String)
    suspend fun modifyTimetableLecture(
        userId: String,
        timetableId: String,
        modifyTimetableLectureRequestDto: TimetableLectureModifyLegacyRequestDto,
        isForced: Boolean
    )

    suspend fun deleteTimetableLecture(userId: String, timetableId: String, timetableLectureId: String)
}

@Service
class TimetableLectureServiceImpl(
    private val timetableRepository: TimetableRepository,
    private val lectureRepository: LectureRepository,
) : TimetableLectureService {
    override suspend fun addLecture(userId: String, timetableId: String, lectureId: String, isForced: Boolean) {
        val timetable = timetableRepository.findByUserIdAndId(userId, timetableId) ?: throw TimetableNotFoundException
        val lecture = lectureRepository.findById(lectureId) ?: throw LectureNotFoundException
        if (!(timetable.year == lecture.year && timetable.semester == lecture.semester)) {
            throw WrongSemesterException
        }
        val colorIndex = ColorUtils.getLeastUsedColorIndexByRandom(timetable.lectures.map { it.colorIndex })
        if (timetable.lectures.any { it.lectureId == lectureId }) throw DuplicateTimetableLectureException
        addTimetableLecture(timetable, TimetableLecture(lecture, colorIndex), isForced)
    }

    override suspend fun addCustomTimetableLecture(
        userId: String,
        timetableId: String,
        timetableLectureRequest: CustomTimetableLectureAddLegacyRequestDto,
        isForced: Boolean
    ) {
        val timetable = timetableRepository.findByUserIdAndId(userId, timetableId) ?: throw TimetableNotFoundException
        val timetableLecture = timetableLectureRequest.toTimetableLecture()
        addTimetableLecture(timetable, timetableLecture, isForced)
    }

    override suspend fun resetTimetableLecture(userId: String, timetableId: String, timetableLectureId: String) {
        val timetable = timetableRepository.findByUserIdAndId(userId, timetableId) ?: throw TimetableNotFoundException
        val timetableLecture = timetable.lectures.find { it.id == timetableLectureId } ?: throw LectureNotFoundException
        val originalLectureId = timetableLecture.lectureId ?: throw CustomLectureResetException
        val originalLecture = lectureRepository.findById(originalLectureId) ?: throw LectureNotFoundException
        timetableLecture.apply {
            courseTitle = originalLecture.courseTitle
            instructor = originalLecture.instructor
            classification = originalLecture.classification
            department = originalLecture.department
            credit = originalLecture.credit
            remark = originalLecture.remark
            classPlaceAndTimes = originalLecture.classPlaceAndTimes
        }
        timetableRepository.updateLecture(timetableId, timetableLecture)
    }

    override suspend fun modifyTimetableLecture(
        userId: String,
        timetableId: String,
        modifyTimetableLectureRequestDto: TimetableLectureModifyLegacyRequestDto,
        isForced: Boolean
    ) {
        val timetable = timetableRepository.findByUserIdAndId(userId, timetableId) ?: throw TimetableNotFoundException
        val timetableLecture = timetable.lectures.find { it.id == modifyTimetableLectureRequestDto.id } ?: throw LectureNotFoundException
        timetableLecture.apply {
            courseTitle = modifyTimetableLectureRequestDto.courseTitle
            instructor = modifyTimetableLectureRequestDto.instructor ?: instructor
            credit = modifyTimetableLectureRequestDto.credit ?: credit
            remark = modifyTimetableLectureRequestDto.remark ?: remark
            color = modifyTimetableLectureRequestDto.color ?: color
            colorIndex = modifyTimetableLectureRequestDto.colorIndex ?: colorIndex
            classPlaceAndTimes = modifyTimetableLectureRequestDto.classPlaceAndTimes?.map { it.toClassPlaceAndTime() } ?: classPlaceAndTimes
        }
        timetableRepository.updateLecture(timetableId, timetableLecture)
    }

    override suspend fun deleteTimetableLecture(userId: String, timetableId: String, timetableLectureId: String) {
        timetableRepository.findByUserIdAndId(userId, timetableId) ?: throw TimetableNotFoundException
        timetableRepository.pullLecture(timetableId, timetableLectureId)
    }

    private suspend fun addTimetableLecture(
        timetable: Timetable,
        timetableLecture: TimetableLecture,
        isForced: Boolean
    ) {
        val overlappingLectures = timetable.lectures.filter {
            timetableLecture.id != it.id &&
                ClassTimeUtils.timesOverlap(
                    timetableLecture.classPlaceAndTimes, it.classPlaceAndTimes
                )
        }
        when {
            overlappingLectures.isNotEmpty() && !isForced -> {
                val confirmMessage = makeOverwritingConfirmMessage(overlappingLectures)
                throw LectureTimeOverlapException(confirmMessage)
            }

            overlappingLectures.isNotEmpty() && isForced -> {
                timetableRepository.pullLectures(timetable.id!!, overlappingLectures.map { it.id!! })
            }
        }
        timetableRepository.pushLecture(timetable.id!!, timetableLecture)
    }

    private fun makeOverwritingConfirmMessage(overlappingLectures: List<TimetableLecture>): String {
        val overlappingLectureTitles =
            overlappingLectures.map { "'${it.courseTitle}'" }.subList(0, 2).joinToString(", ")
        val shortFormOfTitles = if (overlappingLectures.size < 3) "" else "외 ${overlappingLectures.size - 2}개의 "
        return "$overlappingLectureTitles ${shortFormOfTitles}강의가 중복되어 있습니다. 강의를 덮어쓰시겠습니까?"
    }
}
