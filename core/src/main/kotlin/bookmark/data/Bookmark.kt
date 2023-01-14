package com.wafflestudio.snu4t.bookmark.data

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.Lecture
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

@Document("bookmarks")
@CompoundIndex(def = "{'userId': 1, 'year': 1, 'semester': 1}", unique = true)
class Bookmark(
    @Id
    val id: String? = null,
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    val year: Int,
    val semester: Semester,
    val lectures: List<Lecture> = listOf(),
)
