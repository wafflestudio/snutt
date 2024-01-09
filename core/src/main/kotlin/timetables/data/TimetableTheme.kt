package com.wafflestudio.snu4t.timetables.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document
@CompoundIndex(def = "{ 'userId': 1, 'name': 1 }", unique = true)
data class TimetableTheme(
    @Id
    var id: String? = null,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    var name: String,
    var colors: List<ColorSet>?, // basic 테마는 null (클라이언트 처리)
    val isCustom: Boolean, // basic 테마는 false
    var isDefault: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
