package com.wafflestudio.snutt.diary.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class DiaryActivityType(
    @Id
    val id: String? = null,
    @Indexed(unique = true)
    val name: String,
    var active: Boolean = true,
)
