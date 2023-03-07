package timetables.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.timetables.data.TimeTable
import java.time.Instant

@JsonNaming(SnakeCaseStrategy::class)
data class TimeTableBriefDto(
    @JsonProperty("_id")
    val id: String,
    val year: Int,
    val semester: Int,
    val title: String,
    val updatedAt: Instant,
    val totalCredit: Long,
)

fun TimeTableBriefDto(timeTable: TimeTable): TimeTableBriefDto = TimeTableBriefDto(
    id = timeTable.id.let(::requireNotNull),
    year = timeTable.year,
    semester = timeTable.semester.value,
    title = timeTable.title,
    updatedAt = timeTable.updatedAt,
    totalCredit = timeTable.lectures.sumOf { it.credit ?: 0L }
)
