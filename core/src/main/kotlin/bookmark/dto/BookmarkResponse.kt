package com.wafflestudio.snutt.bookmark.dto

import com.wafflestudio.snutt.lectures.dto.BookmarkLectureDto

class BookmarkResponse(
    val year: Int,
    val semester: Int,
    val lectures: List<BookmarkLectureDto>,
)
