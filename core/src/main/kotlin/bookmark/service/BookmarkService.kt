package com.wafflestudio.snu4t.bookmark.service

import com.wafflestudio.snu4t.bookmark.data.Bookmark
import com.wafflestudio.snu4t.bookmark.dto.BookmarkResponse
import com.wafflestudio.snu4t.bookmark.repository.BookmarkRepository
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.exception.LectureNotFoundException
import com.wafflestudio.snu4t.evaluation.repository.SnuttEvRepository
import com.wafflestudio.snu4t.lectures.data.BookmarkLecture
import com.wafflestudio.snu4t.lectures.service.LectureService
import org.springframework.stereotype.Service

interface BookmarkService {
    suspend fun getBookmark(userId: String, year: Int, semester: Semester): BookmarkResponse
    suspend fun existsBookmarkLecture(userId: String, lectureId: String): Boolean
    suspend fun addLecture(userId: String, lectureId: String): Bookmark
    suspend fun deleteLecture(userId: String, lectureId: String): Bookmark
}

@Service
class BookmarkServiceImpl(
    private val bookmarkRepository: BookmarkRepository,
    private val lectureService: LectureService,
    private val snuttEvRepository: SnuttEvRepository,
) : BookmarkService {
    override suspend fun getBookmark(
        userId: String,
        year: Int,
        semester: Semester
    ): BookmarkResponse {
        val bookmark = bookmarkRepository.findFirstByUserIdAndYearAndSemester(userId, year, semester)
            ?: Bookmark(userId = userId, year = year, semester = semester)
        val snuttIdToEvLectureMap =
            snuttEvRepository.getSummariesByIds(bookmark.lectures.map { it.id!! }).associateBy { it.snuttId }
        return BookmarkResponse(bookmark, snuttIdToEvLectureMap)
    }

    override suspend fun existsBookmarkLecture(userId: String, lectureId: String): Boolean {
        val lecture = lectureService.getByIdOrNull(lectureId) ?: throw LectureNotFoundException
        return bookmarkRepository.findFirstByUserIdAndYearAndSemester(userId, lecture.year, lecture.semester)
            ?.lectures?.any { it.id == lectureId } ?: false
    }

    override suspend fun addLecture(userId: String, lectureId: String): Bookmark {
        val lecture = lectureService.getByIdOrNull(lectureId) ?: throw LectureNotFoundException
        return bookmarkRepository.findAndAddLectureByUserIdAndYearAndSemester(
            userId,
            lecture.year,
            lecture.semester,
            BookmarkLecture(lecture)
        )
    }

    override suspend fun deleteLecture(userId: String, lectureId: String): Bookmark {
        val lecture = lectureService.getByIdOrNull(lectureId) ?: throw LectureNotFoundException
        return bookmarkRepository.findAndDeleteLectureByUserIdAndYearAndSemesterAndLectureId(
            userId,
            lecture.year,
            lecture.semester,
            lectureId
        )
    }
}
