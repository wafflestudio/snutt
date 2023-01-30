package com.wafflestudio.snu4t.bookmark.repository

import com.wafflestudio.snu4t.bookmark.data.Bookmark
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.isEqualTo
import com.wafflestudio.snu4t.lectures.data.BookmarkLecture
import com.wafflestudio.snu4t.lectures.data.Lecture
import org.bson.types.ObjectId
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findModifyAndAwait
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.update

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
    ): Bookmark =
        reactiveMongoTemplate.update<Bookmark>().matching(
            Bookmark::userId isEqualTo ObjectId(userId) and
                    Bookmark::year isEqualTo year and
                    Bookmark::semester isEqualTo semester
        ).apply(
            Update().addToSet(Bookmark::lectures.toDotPath(), lecture),
        ).withOptions(
            FindAndModifyOptions.options().returnNew(true).upsert(true),
        ).findModifyAndAwait()

    override suspend fun findAndDeleteLectureByUserIdAndYearAndSemesterAndLectureId(
        userId: String,
        year: Int,
        semester: Semester,
        lectureId: String
    ): Bookmark =
        reactiveMongoTemplate.update<Bookmark>().matching(
            Bookmark::userId isEqualTo ObjectId(userId) and
                    Bookmark::year isEqualTo year and
                    Bookmark::semester isEqualTo semester
        ).apply(
            Update().pull(Bookmark::lectures.toDotPath(), Query.query(Lecture::id isEqualTo ObjectId(lectureId))),
        ).findModifyAndAwait()
}
