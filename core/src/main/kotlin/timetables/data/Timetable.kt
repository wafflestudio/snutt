package com.wafflestudio.snutt.timetables.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.enum.BasicThemeType
import com.wafflestudio.snutt.common.enum.Semester
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant

@Document("timetables")
data class Timetable(
    @Id
    @param:JsonProperty("_id")
    var id: String? = null,
    @Indexed
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    var userId: String,
    var year: Int,
    var semester: Semester,
    @Field("lecture_list")
    @param:JsonProperty("lecture_list")
    var lectures: List<TimetableLecture> = emptyList(),
    var title: String,
    var theme: BasicThemeType,
    var themeId: String?,
    @Field("is_primary")
    var isPrimary: Boolean? = null,
    @Field("updated_at")
    @param:JsonProperty("updated_at")
    var updatedAt: Instant = Instant.now(),
)
