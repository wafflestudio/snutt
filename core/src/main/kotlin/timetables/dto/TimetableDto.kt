package com.wafflestudio.snu4t.timetables.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.enum.TimetableTheme
import com.wafflestudio.snu4t.timetables.data.Timetable
import java.time.Instant

data class TimetableDto(
    @JsonProperty("_id")
    var id: String? = null,
    var userId: String,
    var year: Int,
    var semester: Semester,
    var lectures: List<TimetableLectureDto> = emptyList(),
    var title: String,
    val theme: TimetableTheme,
    @JsonProperty("updated_at")
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
    updatedAt = timetable.updatedAt,
)
