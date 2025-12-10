package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiaryDailyClassType

data class DiaryDailyClassTypeDto(
    val id: String,
    val name: String,
)

fun DiaryDailyClassTypeDto(diaryDailyClassType: DiaryDailyClassType) =
    DiaryDailyClassTypeDto(
        id = diaryDailyClassType.id!!,
        name = diaryDailyClassType.name,
    )
