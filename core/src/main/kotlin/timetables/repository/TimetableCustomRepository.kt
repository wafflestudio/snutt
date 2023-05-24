package com.wafflestudio.snu4t.timetables.repository

import com.wafflestudio.snu4t.common.elemMatch
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.isEqualTo
import com.wafflestudio.snu4t.common.regex
import com.wafflestudio.snu4t.timetables.data.Timetable
import com.wafflestudio.snu4t.timetables.data.TimetableLecture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findModifyAndAwait
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.regex
import org.springframework.data.mongodb.core.update

interface TimetableCustomRepository {
    fun findAllContainsLectureId(year: Int, semester: Semester, lectureId: String): Flow<Timetable>
    fun findAllContainsLecture(
        year: Int,
        semester: Semester,
        courseNumber: String,
        lectureNumber: String
    ): Flow<Timetable>

    suspend fun pullLecture(timeTableId: String, lectureId: String)
    suspend fun findLatestChildTimetable(
        userId: String,
        year: Int,
        semester: Semester,
        title: String
    ): Timetable?
}

class TimetableCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : TimetableCustomRepository {
    override fun findAllContainsLectureId(year: Int, semester: Semester, lectureId: String): Flow<Timetable> {
        return reactiveMongoTemplate.find<Timetable>(
            Query.query(
                Timetable::year isEqualTo year and
                    Timetable::semester isEqualTo semester and
                    Timetable::lectures elemMatch (TimetableLecture::lectureId isEqualTo lectureId)
            )
        ).asFlow()
    }

    override fun findAllContainsLecture(
        year: Int,
        semester: Semester,
        courseNumber: String,
        lectureNumber: String
    ): Flow<Timetable> {
        return reactiveMongoTemplate.find<Timetable>(
            Query.query(
                Timetable::year isEqualTo year and
                    Timetable::semester isEqualTo semester and
                    Timetable::lectures elemMatch (
                    TimetableLecture::courseNumber isEqualTo courseNumber and
                        TimetableLecture::lectureNumber isEqualTo lectureNumber
                    )
            )
        ).asFlow()
    }

    override suspend fun pullLecture(timeTableId: String, lectureId: String) {
        reactiveMongoTemplate.update<Timetable>().matching(Timetable::id isEqualTo timeTableId).apply(
            Update().pull(
                Timetable::lectures.toDotPath(),
                Query.query(TimetableLecture::lectureId isEqualTo lectureId)
            ),
        ).findModifyAndAwait()
    }

    override suspend fun findLatestChildTimetable(
        userId: String,
        year: Int,
        semester: Semester,
        title: String
    ): Timetable? = reactiveMongoTemplate.findOne(
        Query.query(
            Timetable::userId isEqualTo userId and
                Timetable::year isEqualTo year and
                Timetable::semester isEqualTo semester and
                Timetable::title regex """$title(\s+\(\d+\))?"""
        ).with(Sort.by(Sort.Direction.DESC, "title")),
        Timetable::class.java
    ).awaitSingleOrNull()
}
