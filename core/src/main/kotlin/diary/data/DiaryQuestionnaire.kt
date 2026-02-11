package com.wafflestudio.snutt.diary.data

import com.wafflestudio.snutt.timetables.data.TimetableLecture

data class DiaryQuestionnaire(
    val courseTitle: String,
    val questions: List<DiaryQuestion>,
    val nextLecture: TimetableLecture?,
)
