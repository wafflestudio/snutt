package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.semester.dto.GetSemesterStatusResponse
import com.wafflestudio.snutt.semester.service.SemesterService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping("/v1/semesters", "/semesters")
class SemesterController(
    private val semesterService: SemesterService,
) {
    @GetMapping("/status")
    suspend fun getSemesterStatus(): GetSemesterStatusResponse {
        val currentTime = Instant.now()
        val currentYearAndSemester = semesterService.getCurrentYearAndSemester(currentTime)
        val nextYearAndSemester = semesterService.getNextYearAndSemester(currentTime)
        return GetSemesterStatusResponse(
            current = currentYearAndSemester,
            next = nextYearAndSemester,
        )
    }
}
