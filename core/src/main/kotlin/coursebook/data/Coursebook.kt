package com.wafflestudio.snu4t.coursebook.data

import com.wafflestudio.snu4t.common.enum.Semester
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document("coursebooks")
class Coursebook(
    @Id
    val id: String? = null,
    var year: Int,
    var semester: Semester,
    @Field("updated_at")
    var updatedAt: Instant,
)
