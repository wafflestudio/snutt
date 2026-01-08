package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.evaluation.dto.EvErrorResponse
import com.wafflestudio.snutt.evaluation.dto.EvLectureInfoDto
import com.wafflestudio.snutt.evaluation.dto.EvUserDto
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureIdDto
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureSummaryDto
import com.wafflestudio.snutt.evaluation.service.EvService
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.users.data.User
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RegisterReflectionForBinding(
    EvLectureInfoDto::class,
    EvUserDto::class,
    EvErrorResponse::class,
    SnuttEvLectureIdDto::class,
    SnuttEvLectureSummaryDto::class,
    ListResponse::class,
)
@RequestMapping(
    "/v1/ev-service",
    "/ev-service",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class EvServiceController(
    private val evService: EvService,
) {
    @GetMapping("/v1/users/me/lectures/latest")
    suspend fun getMyLatestLectures(
        @CurrentUser user: User,
        @RequestParam queryParams: MultiValueMap<String, String>,
        @RequestParam(defaultValue = "100") limit: Int,
    ) = evService.getMyLatestLectures(user.id!!, queryParams, limit)

    @GetMapping("/{*requestPath}")
    suspend fun handleGet(
        @CurrentUser user: User,
        @PathVariable requestPath: String,
        @RequestParam queryParams: MultiValueMap<String, String>,
        @RequestBody(required = false) body: String?,
    ) = evService.handleRouting(user, requestPath, queryParams, body ?: "", HttpMethod.GET)

    @PostMapping("/{*requestPath}")
    suspend fun handlePost(
        @CurrentUser user: User,
        @PathVariable requestPath: String,
        @RequestParam queryParams: MultiValueMap<String, String>,
        @RequestBody(required = false) body: String?,
    ) = evService.handleRouting(user, requestPath, queryParams, body ?: "", HttpMethod.POST)

    @DeleteMapping("/{*requestPath}")
    suspend fun handleDelete(
        @CurrentUser user: User,
        @PathVariable requestPath: String,
        @RequestParam queryParams: MultiValueMap<String, String>,
        @RequestBody(required = false) body: String?,
    ) = evService.handleRouting(user, requestPath, queryParams, body ?: "", HttpMethod.DELETE)

    @PatchMapping("/{*requestPath}")
    suspend fun handlePatch(
        @CurrentUser user: User,
        @PathVariable requestPath: String,
        @RequestParam queryParams: MultiValueMap<String, String>,
        @RequestBody(required = false) body: String?,
    ) = evService.handleRouting(user, requestPath, queryParams, body ?: "", HttpMethod.PATCH)
}
