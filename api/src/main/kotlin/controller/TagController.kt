package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.tag.TagListService
import com.wafflestudio.snutt.tag.data.TagListResponse
import com.wafflestudio.snutt.tag.data.TagListUpdateTimeResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping("/v1/tags", "/tags")
class TagController(
    private val tagService: TagListService,
) {
    @GetMapping("/{year}/{semester}")
    suspend fun getTagList(
        @PathVariable year: Int,
        @PathVariable semester: Semester,
    ): TagListResponse {
        val tagList = tagService.getTagList(year, semester)
        return TagListResponse(tagList)
    }

    @GetMapping("/{year}/{semester}/update_time")
    suspend fun getTagListUpdateTime(
        @PathVariable year: Int,
        @PathVariable semester: Semester,
    ): TagListUpdateTimeResponse {
        val tagList = tagService.getTagList(year, semester)
        return TagListUpdateTimeResponse(tagList.updatedAt.toEpochMilli())
    }
}
