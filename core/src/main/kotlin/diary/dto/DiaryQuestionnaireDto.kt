package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiaryQuestionnaire

data class DiaryQuestionnaireDto(
    val lectureTitle: String,
    val questions: List<DiaryQuestionDto>,
    val nextLectureId: String,
)

fun DiaryQuestionnaireDto(diaryQuestionnaire: DiaryQuestionnaire) =
    DiaryQuestionnaireDto(
        lectureTitle = diaryQuestionnaire.lectureTitle,
        questions =
            diaryQuestionnaire.questions.map {
                DiaryQuestionDto(it)
            },
        nextLectureId = diaryQuestionnaire.nextLectureId,
    )
