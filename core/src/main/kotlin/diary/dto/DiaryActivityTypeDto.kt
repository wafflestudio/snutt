package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiaryActivityType

data class DiaryActivityTypeDto(
    val id: String,
    val name: String,
)

fun DiaryActivityTypeDto(diaryActivityType: DiaryActivityType) =
    DiaryActivityTypeDto(
        id = diaryActivityType.id!!,
        name = diaryActivityType.name,
    )
