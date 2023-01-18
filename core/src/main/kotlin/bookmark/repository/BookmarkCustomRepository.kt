package com.wafflestudio.snu4t.bookmark.repository

import com.wafflestudio.snu4t.bookmark.data.Bookmark
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.BookmarkLecture
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo

interface BookmarkCustomRepository {
    suspend fun findAndAddLectureByUserIdAndYearAndSemester(
        userId: String,
        year: Int,
        semester: Semester,
        lecture: BookmarkLecture
    ): Bookmark
}

class BookmarkCustomRepositoryImpl(private val reactiveMongoTemplate: ReactiveMongoTemplate) :
    BookmarkCustomRepository {
    override suspend fun findAndAddLectureByUserIdAndYearAndSemester(
        userId: String,
        year: Int,
        semester: Semester,
        lecture: BookmarkLecture
    ): Bookmark {
        return reactiveMongoTemplate.findAndModify<Bookmark>(
            Query.query(
                Criteria.where("user_id").isEqualTo(userId)
                    .and("year").isEqualTo(year)
                    .and("semester").isEqualTo(semester)
            ),
            Update().addToSet("lectures", lecture),
            FindAndModifyOptions.options().returnNew(true).upsert(true),
        ).awaitSingle()
    }
}
