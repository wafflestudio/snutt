package com.wafflestudio.snutt.diary.dto.request

import com.wafflestudio.snutt.diary.data.QuestionAnswer
import java.time.LocalDate

data class DiarySubmissionRequestDto(
    val lectureId: String,
    val dailyClassTypes: List<String>,
    val questionAnswers: List<QuestionAnswer>,
    val comment: String,
    // 일기를 쓰는 수업이 열린 날(KST). 없으면 서버가 그 강의의 가장 최근 수업일로 정한다.
    val classDate: LocalDate? = null,
)
