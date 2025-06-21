package com.wafflestudio.snutt.diary.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class DiaryActivityType(
    @Id
    val id: String? = null,
    val name: String,
    val isActive: Boolean = true,
)
