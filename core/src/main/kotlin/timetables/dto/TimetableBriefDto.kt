package timetables.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.timetables.data.Timetable
import java.time.Instant

@JsonNaming(SnakeCaseStrategy::class)
data class TimetableBriefDto(
    @JsonProperty("_id")
    val id: String,
    val year: Int,
    val semester: Int,
    val title: String,
    val isPrimary: Boolean,
    val updatedAt: Instant,
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
