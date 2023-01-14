package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.bookmark.dto.BookmarkLectureAddingRequest
import com.wafflestudio.snu4t.bookmark.dto.BookmarkResponse
import com.wafflestudio.snu4t.bookmark.service.BookmarkService
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class BookmarkHandler(
    private val bookmarkService: BookmarkService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(handlerMiddleware = snuttRestApiDefaultMiddleware) {
    suspend fun addLecture(req: ServerRequest) = handle(req) {
        val userId: String = req.userId
        val body = req.awaitBody<BookmarkLectureAddingRequest>()
        val lectureId = body.lectureId
        bookmarkService.addLecture(userId, lectureId).let { BookmarkResponse(it) }
    }

    suspend fun getBookmark(req: ServerRequest) = handle(req) {
        val userId: String = req.userId
        val year: Int = req.parseQueryParam("year") { it.toInt() }
        val semester: Semester = req.parseQueryParam("semester") { Semester.getOfValue(it.toInt()) }
        bookmarkService.getBookmark(userId, year, semester).let { BookmarkResponse(it) }
    }
}

