package com.wafflestudio.snu4t.bookmark.dto

import com.wafflestudio.snu4t.bookmark.data.Bookmark
import com.wafflestudio.snu4t.lectures.dto.BookmarkLectureDto

class BookmarkResponse(
    val year: Int,
    val semester: Int,
    val lectures: List<BookmarkLectureDto>,
)

fun BookmarkResponse(bookmark: Bookmark): BookmarkResponse = BookmarkResponse(
    year = bookmark.year,
    semester = bookmark.semester.value,
    lectures = bookmark.lectures.map { BookmarkLectureDto(it) },
)
