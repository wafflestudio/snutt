package com.wafflestudio.snu4t.lectures.data

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.common.enum.DayOfWeek
import org.springframework.data.mongodb.core.mapping.Field

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ClassTime(
    val day: DayOfWeek,
    val place: String?,
    @Field("startMinute")
    val startMinute: Int,
    @Field("endMinute")
    val endMinute: Int,
)
