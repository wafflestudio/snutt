package com.wafflestudio.snu4t.bookmark.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.BookmarkLecture
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

@Document("bookmarks")
@CompoundIndex(def = "{'user_id': 1, 'year': 1, 'semester': 1}", unique = true)
class Bookmark(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    val userId: String,
    val year: Int,
    val semester: Semester,
    var lectures: List<BookmarkLecture> = listOf(),
)
