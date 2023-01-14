package com.wafflestudio.snu4t.timetables.data

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.Lecture
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant

@Document("timetables")
data class TimeTable(
    @Id
    var id: String? = null,
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    var userId: String,
    var year: Int,
    var semester: Semester,
    @Field("lecture_list")
    var lectures: List<Lecture>,
    var title: String,
    var theme: Int,
    @Field("updated_at")
    var updatedAt: Instant,
)
