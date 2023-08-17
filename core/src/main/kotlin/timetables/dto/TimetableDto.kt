package com.wafflestudio.snu4t.timetables.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.enum.TimetableTheme
import com.wafflestudio.snu4t.timetables.data.Timetable
import java.time.Instant

data class TimetableDto(
    var id: String? = null,
    var userId: String,
    var year: Int,
    var semester: Semester,
    var lectures: List<TimetableLectureDto> = emptyList(),
    var title: String,
    val theme: TimetableTheme,
    val isPrimary: Boolean,
    var updatedAt: Instant = Instant.now(),
)

fun TimetableDto(timetable: Timetable) = TimetableDto(
    id = timetable.id,
    userId = timetable.userId,
    year = timetable.year,
    semester = timetable.semester,
    lectures = timetable.lectures.map { TimetableLectureDto(it) },
    title = timetable.title,
    theme = timetable.theme,
    isPrimary = timetable.isPrimary ?: false,
    updatedAt = timetable.updatedAt,
)

data class TimetableLegacyDto(
    @JsonProperty("_id")
    var id: String? = null,
    @JsonProperty("user_id")
    var userId: String,
    var year: Int,
    var semester: Semester,
    @JsonProperty("lecture_list")
    var lectures: List<TimetableLectureLegacyDto> = emptyList(),
    var title: String,
    val theme: TimetableTheme,
    val isPrimary: Boolean,
    @JsonProperty("updated_at")
    var updatedAt: Instant = Instant.now(),
)

fun TimetableLegacyDto(timetable: Timetable) = TimetableLegacyDto(
    id = timetable.id,
    userId = timetable.userId,
    year = timetable.year,
    semester = timetable.semester,
    lectures = timetable.lectures.map { TimetableLectureLegacyDto(it) },
    title = timetable.title,
    theme = timetable.theme,
    isPrimary = timetable.isPrimary ?: false,
    updatedAt = timetable.updatedAt,
)
