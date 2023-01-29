package com.wafflestudio.snu4t.timetables.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant

@Document("timetables")
@CompoundIndex(def = "{'user_id': 1}")
data class TimeTable(
    @Id
    @JsonProperty("_id")
    var id: String? = null,
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    var userId: String,
    var year: Int,
    var semester: Semester,
    @Field("lecture_list")
    var lectures: List<TimeTableLecture>,
    var title: String,
    var theme: Int?,
    @Field("updated_at")
    var updatedAt: Instant,
)
