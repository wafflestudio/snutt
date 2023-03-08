package com.wafflestudio.snu4t.timetables.repository

import com.wafflestudio.snu4t.common.elemMatch
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.isEqualTo
import com.wafflestudio.snu4t.timetables.data.TimeTable
import com.wafflestudio.snu4t.timetables.data.TimeTableLecture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findModifyAndAwait
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.update

interface TimeTableCustomRepository {
    fun findAllContainsLectureId(year: Int, semester: Semester, lectureId: String): Flow<TimeTable>
    fun findAllContainsLecture(
        year: Int,
        semester: Semester,
        courseNumber: String,
        lectureNumber: String
    ): Flow<TimeTable>

    suspend fun pullLecture(timeTableId: String, lectureId: String)
}

class TimeTableCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : TimeTableCustomRepository {
    override fun findAllContainsLectureId(year: Int, semester: Semester, lectureId: String): Flow<TimeTable> {
        return reactiveMongoTemplate.find<TimeTable>(
            Query.query(
                TimeTable::year isEqualTo year and
                    TimeTable::semester isEqualTo semester and
                    TimeTable::lectures elemMatch (TimeTableLecture::lectureId isEqualTo lectureId)
            )
        ).asFlow()
    }

    override fun findAllContainsLecture(
        year: Int,
        semester: Semester,
        courseNumber: String,
        lectureNumber: String
    ): Flow<TimeTable> {
        return reactiveMongoTemplate.find<TimeTable>(
            Query.query(
                TimeTable::year isEqualTo year and
                    TimeTable::semester isEqualTo semester and
                    TimeTable::lectures elemMatch (
                    TimeTableLecture::courseNumber isEqualTo courseNumber and
                        TimeTableLecture::lectureNumber isEqualTo lectureNumber
                    )
            )
        ).asFlow()
    }

    override suspend fun pullLecture(timeTableId: String, lectureId: String) {
        reactiveMongoTemplate.update<TimeTable>().matching(TimeTable::id isEqualTo timeTableId).apply(
            Update().pull(
                TimeTable::lectures.toDotPath(),
                Query.query(TimeTableLecture::lectureId isEqualTo lectureId)
            ),
        ).findModifyAndAwait()
    }
}
