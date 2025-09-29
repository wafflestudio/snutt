package com.wafflestudio.snutt.diary.data

data class DiaryQuestionnaire(
    val lectureTitle: String,
    val questions: List<DiaryQuestion>,
    val nextLectureId: String?,
    val nextLectureTitle: String?,
)
