package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.semester.dto.GetSemesterStatusResponse
import com.wafflestudio.snutt.semester.service.SemesterService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import java.time.Instant

@Component
class SemesterHandler(
    private val semesterService: SemesterService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiDefaultMiddleware,
    ) {
    suspend fun getSemesterStatus(req: ServerRequest): ServerResponse =
        handle(req) {
            val currentTime = Instant.now()
            val currentYearAndSemester = semesterService.getCurrentYearAndSemester(currentTime)
            val nextYearAndSemester = semesterService.getNextYearAndSemester(currentTime)
            GetSemesterStatusResponse(
                current = currentYearAndSemester,
                next = nextYearAndSemester,
            )
        }
}
