package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.evaluation.service.EvService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/ev", "/ev")
class EvController(
    private val evService: EvService,
) {
    @GetMapping("/lectures/{lectureId}/summary")
    suspend fun getLectureEvaluationSummary(
        @PathVariable lectureId: String,
    ) = evService.getEvSummary(lectureId)
}
