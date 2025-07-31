package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiaryActivity

data class DiaryActivityTypeDto(
    val id: String,
    val name: String,
)

fun DiaryActivityTypeDto(diaryActivity: DiaryActivity) =
    DiaryActivityTypeDto(
        id = diaryActivity.id!!,
        name = diaryActivity.name,
    )
