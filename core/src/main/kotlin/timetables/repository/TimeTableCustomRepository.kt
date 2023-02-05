package com.wafflestudio.snu4t.timetables.repository

import com.wafflestudio.snu4t.common.elemMatch
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.isEqualTo
import com.wafflestudio.snu4t.timetables.data.TimeTable
import com.wafflestudio.snu4t.timetables.data.TimeTableLecture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.isEqualTo

interface TimeTableCustomRepository {
    fun findAllContainsLectureId(year: Int, semester: Semester, lectureId: String): Flow<TimeTable>
    fun findAllContainsLecture(
        year: Int,
        semester: Semester,
        courseNumber: String,
        lectureNumber: String
    ): Flow<TimeTable>
}

class TimeTableCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : TimeTableCustomRepository {
    override fun findAllContainsLectureId(year: Int, semester: Semester, lectureId: String): Flow<TimeTable> {
        return reactiveMongoTemplate.find<TimeTable>(
            Query.query(
                TimeTable::year isEqualTo year and
                    TimeTable::semester isEqualTo semester and
                    TimeTable::lectures elemMatch (TimeTableLecture::id isEqualTo lectureId)
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
                        TimeTableLecture::lectureNumber isEqualTo lectureNumber)
            )
        ).asFlow()
    }
}
