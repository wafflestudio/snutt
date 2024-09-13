package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.exception.InvalidPathParameterException
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.tag.TagListService
import com.wafflestudio.snu4t.tag.data.TagListResponse
import com.wafflestudio.snu4t.tag.data.TagListUpdateTimeResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class TagHandler(
    private val tagService: TagListService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun getTagList(req: ServerRequest): ServerResponse =
        handle(req) {
            val year = req.pathVariable("year").toInt()
            val semester =
                Semester.getOfValue(req.pathVariable("semester").toInt()) ?: throw InvalidPathParameterException("semester")
            val tagList = tagService.getTagList(year, semester)
            TagListResponse(tagList)
        }

    suspend fun getTagListUpdateTime(req: ServerRequest): ServerResponse =
        handle(req) {
            val year = req.pathVariable("year").toInt()
            val semester =
                Semester.getOfValue(req.pathVariable("semester").toInt()) ?: throw InvalidPathParameterException("semester")
            val tagList = tagService.getTagList(year, semester)
            TagListUpdateTimeResponse(tagList.updatedAt.toEpochMilli())
        }
}
