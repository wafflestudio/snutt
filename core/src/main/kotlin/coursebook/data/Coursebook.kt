package com.wafflestudio.snu4t.coursebook.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document("coursebooks")
@CompoundIndex(def = "{ 'year': 1, 'semester': 1 }", unique = true)
class Coursebook(
    @Id
    @JsonProperty("_id")
    var id: String? = null,
    val year: Int,
    val semester: Semester,
    @Field("updated_at")
    var updatedAt: Instant = Instant.now(),
)
