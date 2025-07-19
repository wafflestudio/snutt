package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiaryQuestion

data class DiaryQuestionDto(
    val id: String,
    val question: String,
    val shortQuestion: String,
    val answers: List<String>,
    val shortAnswers: List<String>,
)

fun DiaryQuestionDto(diaryQuestion: DiaryQuestion) =
    DiaryQuestionDto(
        id = diaryQuestion.id!!,
        question = diaryQuestion.question,
        shortQuestion = diaryQuestion.shortQuestion,
        answers = diaryQuestion.answers,
        shortAnswers = diaryQuestion.shortAnswers,
    )
