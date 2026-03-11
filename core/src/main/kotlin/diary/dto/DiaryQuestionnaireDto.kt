package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiaryQuestionnaire

data class DiaryQuestionnaireDto(
    val courseTitle: String,
    val questions: List<DiaryQuestionDto>,
    val nextLecture: DiaryTargetLectureDto?,
)

fun DiaryQuestionnaireDto(diaryQuestionnaire: DiaryQuestionnaire) =
    DiaryQuestionnaireDto(
        courseTitle = diaryQuestionnaire.courseTitle,
        questions =
            diaryQuestionnaire.questions.map {
                DiaryQuestionDto(it)
            },
        nextLecture = diaryQuestionnaire.nextLecture?.let { DiaryTargetLectureDto(it) },
    )
