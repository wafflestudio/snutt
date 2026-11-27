package com.wafflestudio.snutt.diary.data

data class DiaryQuestionnaire(
    val courseTitle: String,
    val questions: List<DiaryQuestion>,
    val nextLecture: DiaryTargetCandidate?,
)
