package timetables.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.timetables.data.Timetable
import java.time.Instant

data class TimetableBriefDto(
    @JsonProperty("_id")
    val id: String,
    val year: Int,
    val semester: Int,
    val title: String,
    val isPrimary: Boolean,
    @JsonProperty("updated_at")
    val updatedAt: Instant,
    @JsonProperty("total_credit")
    val totalCredit: Long,
)
fun TimetableBriefDto(timeTable: Timetable): TimetableBriefDto = TimetableBriefDto(
    id = timeTable.id.let(::requireNotNull),
    year = timeTable.year,
    semester = timeTable.semester.value,
    title = timeTable.title,
    isPrimary = timeTable.isPrimary ?: false,
    updatedAt = timeTable.updatedAt,
    totalCredit = timeTable.lectures.sumOf { it.credit ?: 0L }
)
