package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.diary.service.DiaryService
import org.springframework.stereotype.Component

@Component
class DiaryHandler(
    private val diaryService: DiaryService,
)
