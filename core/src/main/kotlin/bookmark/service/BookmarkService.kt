package com.wafflestudio.snutt.bookmark.service

import com.wafflestudio.snutt.bookmark.data.Bookmark
import com.wafflestudio.snutt.bookmark.repository.BookmarkRepository
import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.exception.LectureNotFoundException
import com.wafflestudio.snutt.evaluation.service.EvService
import com.wafflestudio.snutt.lectures.data.BookmarkLecture
import com.wafflestudio.snutt.lectures.dto.BookmarkLectureDto
import com.wafflestudio.snutt.lectures.service.LectureService
import org.springframework.stereotype.Service

interface BookmarkService {
    suspend fun getBookmark(
        userId: String,
        year: Int,
        semester: Semester,
    ): Bookmark

    suspend fun existsBookmarkLecture(
        userId: String,
        lectureId: String,
    ): Boolean

    suspend fun addLecture(
        userId: String,
        lectureId: String,
    ): Bookmark

    suspend fun deleteLecture(
        userId: String,
        lectureId: String,
    ): Bookmark

    suspend fun convertBookmarkLecturesToBookmarkLectureDtos(bookmarkLectures: List<BookmarkLecture>): List<BookmarkLectureDto>
}

@Service
class BookmarkServiceImpl(
    private val bookmarkRepository: BookmarkRepository,
    private val lectureService: LectureService,
    private val snuttEvService: EvService,
) : BookmarkService {
    override suspend fun getBookmark(
        userId: String,
        year: Int,
        semester: Semester,
    ): Bookmark =
        bookmarkRepository.findFirstByUserIdAndYearAndSemester(userId, year, semester)
            ?: Bookmark(userId = userId, year = year, semester = semester)

    override suspend fun existsBookmarkLecture(
        userId: String,
        lectureId: String,
    ): Boolean {
        val lecture = lectureService.getByIdOrNull(lectureId) ?: throw LectureNotFoundException
        return bookmarkRepository
            .findFirstByUserIdAndYearAndSemester(userId, lecture.year, lecture.semester)
            ?.lectures
            ?.any { it.id == lectureId } ?: false
    }

    override suspend fun addLecture(
        userId: String,
        lectureId: String,
    ): Bookmark {
        val lecture = lectureService.getByIdOrNull(lectureId) ?: throw LectureNotFoundException
        return bookmarkRepository.findAndAddLectureByUserIdAndYearAndSemester(
            userId,
            lecture.year,
            lecture.semester,
            BookmarkLecture(lecture),
        )
    }

    override suspend fun deleteLecture(
        userId: String,
        lectureId: String,
    ): Bookmark {
        val lecture = lectureService.getByIdOrNull(lectureId) ?: throw LectureNotFoundException
        return bookmarkRepository.findAndDeleteLectureByUserIdAndYearAndSemesterAndLectureId(
            userId,
            lecture.year,
            lecture.semester,
            lectureId,
        )
    }

    override suspend fun convertBookmarkLecturesToBookmarkLectureDtos(bookmarkLectures: List<BookmarkLecture>): List<BookmarkLectureDto> {
        val snuttIdtoLectureMap =
            snuttEvService.getSummariesByIds(bookmarkLectures.map { it.id!! }).associateBy { it.snuttId }
        return bookmarkLectures.map { bookmarkLecture ->
            val snuttEvLecture = snuttIdtoLectureMap[bookmarkLecture.id]
            BookmarkLectureDto(bookmarkLecture, snuttEvLecture)
        }
    }
}
