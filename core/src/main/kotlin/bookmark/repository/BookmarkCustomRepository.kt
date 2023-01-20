package com.wafflestudio.snu4t.bookmark.repository

import com.wafflestudio.snu4t.bookmark.data.Bookmark
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.BookmarkLecture
import kotlinx.coroutines.reactor.awaitSingle
import org.bson.types.ObjectId
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

    suspend fun findAndDeleteLectureByUserIdAndYearAndSemesterAndLectureId(
        userId: String,
        year: Int,
        semester: Semester,
        lectureId: String
    ): Bookmark
}

class BookmarkCustomRepositoryImpl(private val reactiveMongoTemplate: ReactiveMongoTemplate) :
    BookmarkCustomRepository {
    override suspend fun findAndAddLectureByUserIdAndYearAndSemester(
        userId: String,
        year: Int,
        semester: Semester,
        lecture: BookmarkLecture
    ): Bookmark = reactiveMongoTemplate.findAndModify<Bookmark>(
        Query.query(
            Criteria.where("user_id").isEqualTo(ObjectId(userId))
                .and("year").isEqualTo(year)
                .and("semester").isEqualTo(semester)
        ),
        Update().addToSet("lectures", lecture),
        FindAndModifyOptions.options().returnNew(true).upsert(true),
    ).awaitSingle()

    override suspend fun findAndDeleteLectureByUserIdAndYearAndSemesterAndLectureId(
        userId: String,
        year: Int,
        semester: Semester,
        lectureId: String
    ): Bookmark = reactiveMongoTemplate.findAndModify<Bookmark>(
        Query.query(
            Criteria.where("user_id").isEqualTo(ObjectId(userId))
                .and("year").isEqualTo(year)
                .and("semester").isEqualTo(semester)
        ),
        Update().pull("lectures", Query.query(Criteria.where("_id").isEqualTo(ObjectId(lectureId)))),
        FindAndModifyOptions.options().returnNew(true),
    ).awaitSingle()
}
