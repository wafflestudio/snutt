package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.bookmark.dto.BookmarkLectureModifyRequest
import com.wafflestudio.snutt.bookmark.dto.BookmarkResponse
import com.wafflestudio.snutt.bookmark.service.BookmarkService
import com.wafflestudio.snutt.common.dto.ExistenceResponse
import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.users.data.User
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/bookmarks", "/bookmarks")
class BookmarkController(
    private val bookmarkService: BookmarkService,
) {
    @GetMapping("")
    suspend fun getBookmarks(
        @CurrentUser user: User,
        @RequestParam year: Int,
        @RequestParam semester: Semester,
    ): BookmarkResponse {
        val userId = user.id!!
        val bookmark = bookmarkService.getBookmark(userId, year, semester)
        val bookmarkLectureDtos = bookmarkService.convertBookmarkLecturesToBookmarkLectureDtos(bookmark.lectures)

        return BookmarkResponse(
            year = bookmark.year,
            semester = bookmark.semester.value,
            lectures = bookmarkLectureDtos,
        )
    }

    @GetMapping("/lectures/{lectureId}/state")
    suspend fun existsBookmarkLecture(
        @CurrentUser user: User,
        @PathVariable lectureId: String,
    ) = ExistenceResponse(bookmarkService.existsBookmarkLecture(user.id!!, lectureId))

    @PostMapping("/lecture")
    suspend fun addLecture(
        @CurrentUser user: User,
        @RequestBody body: BookmarkLectureModifyRequest,
    ) {
        bookmarkService.addLecture(user.id!!, body.lectureId)
    }

    @DeleteMapping("/lecture")
    suspend fun deleteBookmark(
        @CurrentUser user: User,
        @RequestBody body: BookmarkLectureModifyRequest,
    ) {
        bookmarkService.deleteLecture(user.id!!, body.lectureId)
    }
}
