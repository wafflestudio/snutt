package com.wafflestudio.snu4t.timetables.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant

@Document("timetables")
data class Timetable(
    @Id
    @JsonProperty("_id")
    var id: String? = null,
    @Indexed
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    var userId: String,
    var year: Int,
    var semester: Semester,
    @Field("lecture_list")
    @JsonProperty("lecture_list")
    var lectures: List<TimetableLecture>,
    var title: String,
    @Field("updated_at")
    @JsonProperty("updated_at")
    var updatedAt: Instant,
    var theme: Int = 0,
)
