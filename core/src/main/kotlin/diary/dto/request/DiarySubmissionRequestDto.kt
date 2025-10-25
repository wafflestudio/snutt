package com.wafflestudio.snutt.diary.dto.request

import com.wafflestudio.snutt.diary.data.QuestionAnswer

data class DiarySubmissionRequestDto(
    val lectureId: String,
    val dailyClassTypes: List<String>,
    val questionAnswers: List<QuestionAnswer>,
    val comment: String,
)
