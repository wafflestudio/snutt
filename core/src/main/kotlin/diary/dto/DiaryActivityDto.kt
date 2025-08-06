package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiaryActivity

data class DiaryActivityDto(
    val id: String,
    val name: String,
)

fun DiaryActivityDto(diaryActivity: DiaryActivity) =
    DiaryActivityDto(
        id = diaryActivity.id!!,
        name = diaryActivity.name,
    )
