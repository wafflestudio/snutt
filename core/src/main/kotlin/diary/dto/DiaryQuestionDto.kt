package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiaryQuestion

data class DiaryQuestionDto(
    val id: String,
    val question: String,
    val answers: List<String>,
)

fun DiaryQuestionDto(diaryQuestion: DiaryQuestion) =
    DiaryQuestionDto(
        id = diaryQuestion.id!!,
        question = diaryQuestion.question,
        answers = diaryQuestion.answers,
    )
