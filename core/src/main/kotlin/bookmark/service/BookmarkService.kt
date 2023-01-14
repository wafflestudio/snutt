package com.wafflestudio.snu4t.bookmark.service

import com.wafflestudio.snu4t.bookmark.data.Bookmark
import com.wafflestudio.snu4t.bookmark.repository.BookmarkRepository
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.exception.LectureNotFoundException
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.service.LectureWithSemesterService
import org.springframework.stereotype.Service

interface BookmarkService {
    suspend fun getBookmark(userId: String, year: Int, semester: Semester): Bookmark
    suspend fun addLecture(userId: String, lectureId: String): Bookmark
}

@Service
class BookmarkServiceImpl(
    private val bookmarkRepository: BookmarkRepository,
    private val lectureWithSemesterService: LectureWithSemesterService,
) : BookmarkService {
    override suspend fun getBookmark(
        userId: String,
        year: Int,
        semester: Semester
    ): Bookmark =
        bookmarkRepository.findFirstByUserIdAndYearAndSemester(userId, year, semester)
            ?: Bookmark(userId = userId, year = year, semester = semester)

    override suspend fun addLecture(userId: String, lectureId: String): Bookmark {
        val lectureWithSemester = lectureWithSemesterService.getByIdOrNull(lectureId) ?: throw LectureNotFoundException
        return bookmarkRepository.findAndAddLectureByUserIdAndYearAndSemester(
            userId,
            lectureWithSemester.year,
            lectureWithSemester.semester,
            Lecture(lectureWithSemester)
        )
    }
}
