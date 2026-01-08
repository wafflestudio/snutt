package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.util.SugangSnuUrlUtils.REDIRECT_PREFIX_URL
import com.wafflestudio.snutt.common.util.SugangSnuUrlUtils.parseSyllabusUrl
import com.wafflestudio.snutt.coursebook.data.CoursebookOfficialResponse
import com.wafflestudio.snutt.coursebook.data.CoursebookResponse
import com.wafflestudio.snutt.coursebook.service.CoursebookService
import com.wafflestudio.snutt.filter.SnuttNoAuthApiFilterTarget
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttNoAuthApiFilterTarget
@RequestMapping(
    "/v1/course_books",
    "/course_books",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class CoursebookController(
    private val coursebookService: CoursebookService,
) {
    @GetMapping("")
    suspend fun getCoursebooks() = coursebookService.getCoursebooks().map { CoursebookResponse(it) }

    @GetMapping("/recent")
    suspend fun getLatestCoursebook(): CoursebookResponse {
        val latestCoursebook = coursebookService.getLatestCoursebook()
        return CoursebookResponse(latestCoursebook)
    }

    @GetMapping("/official")
    suspend fun getCoursebookOfficial(
        @RequestParam year: Int,
        @RequestParam semester: Semester,
        @RequestParam("course_number") courseNumber: String,
        @RequestParam("lecture_number") lectureNumber: String,
    ): CoursebookOfficialResponse {
        val url =
            parseSyllabusUrl(
                year = year,
                semester = semester,
                courseNumber = courseNumber,
                lectureNumber = lectureNumber,
            )
        return CoursebookOfficialResponse(
            noProxyUrl = url,
            proxyUrl = REDIRECT_PREFIX_URL + url,
        )
    }
}
