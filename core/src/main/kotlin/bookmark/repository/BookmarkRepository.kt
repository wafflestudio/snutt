package com.wafflestudio.snu4t.bookmark.repository

import com.wafflestudio.snu4t.bookmark.data.Bookmark
import com.wafflestudio.snu4t.common.enum.Semester
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookmarkRepository : CoroutineCrudRepository<Bookmark, String>, BookmarkCustomRepository {
    suspend fun findFirstByUserIdAndYearAndSemester(
        userId: String,
        year: Int,
        semester: Semester,
    ): Bookmark?

    suspend fun existsByUserIdAndYearAndSemester(
        userId: String,
        year: Int,
        semester: Semester,
    ): Boolean
}
