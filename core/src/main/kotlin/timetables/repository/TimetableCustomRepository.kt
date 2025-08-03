package com.wafflestudio.snutt.timetables.repository

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.common.extension.desc
import com.wafflestudio.snutt.common.extension.elemMatch
import com.wafflestudio.snutt.common.extension.isEqualTo
import com.wafflestudio.snutt.common.extension.regex
import com.wafflestudio.snutt.timetables.data.Timetable
import com.wafflestudio.snutt.timetables.data.TimetableLecture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.types.ObjectId
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findModifyAndAwait
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.update

interface TimetableCustomRepository {
    fun findAllContainsLectureId(
        year: Int,
        semester: Semester,
        lectureId: String,
    ): Flow<Timetable>

    fun findAllContainsLecture(
        year: Int,
        semester: Semester,
        courseNumber: String,
        lectureNumber: String,
    ): Flow<Timetable>

    suspend fun pushTimetableLecture(
        timeTableId: String,
        timetableLecture: TimetableLecture,
    ): Timetable

    suspend fun pullTimetableLecture(
        timeTableId: String,
        timetableLectureId: String,
    ): Timetable

    suspend fun pullTimetableLectureByLectureId(
        timeTableId: String,
        lectureId: String,
    ): Timetable

    suspend fun pullTimetableLectures(
        timeTableId: String,
        timetableLectureIds: List<String>,
    ): Timetable

    suspend fun updateTimetableLecture(
        timeTableId: String,
        timetableLecture: TimetableLecture,
    ): Timetable

    suspend fun findLatestChildTimetable(
        userId: String,
        year: Int,
        semester: Semester,
        title: String,
    ): Timetable?
}

class TimetableCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : TimetableCustomRepository {
    override fun findAllContainsLectureId(
        year: Int,
        semester: Semester,
        lectureId: String,
    ): Flow<Timetable> {
        return reactiveMongoTemplate.find<Timetable>(
            Query.query(
                Timetable::year isEqualTo year and
                    Timetable::semester isEqualTo semester and
                    Timetable::lectures elemMatch (TimetableLecture::lectureId isEqualTo lectureId),
            ),
        ).asFlow()
    }

    override fun findAllContainsLecture(
        year: Int,
        semester: Semester,
        courseNumber: String,
        lectureNumber: String,
    ): Flow<Timetable> {
        return reactiveMongoTemplate.find<Timetable>(
            Query.query(
                Timetable::year isEqualTo year and
                    Timetable::semester isEqualTo semester and
                    Timetable::lectures elemMatch (
                        TimetableLecture::courseNumber isEqualTo courseNumber and
                            TimetableLecture::lectureNumber isEqualTo lectureNumber
                    ),
            ),
        ).asFlow()
    }

    override suspend fun pushTimetableLecture(
        timeTableId: String,
        timetableLecture: TimetableLecture,
    ): Timetable =
        reactiveMongoTemplate.update<Timetable>().matching(Timetable::id isEqualTo timeTableId).apply(
            Update().push(Timetable::lectures.toDotPath(), timetableLecture)
                .currentDate(Timetable::updatedAt.toDotPath()),
        ).withOptions(FindAndModifyOptions.options().returnNew(true)).findModifyAndAwait()

    override suspend fun pullTimetableLecture(
        timeTableId: String,
        timetableLectureId: String,
    ): Timetable =
        reactiveMongoTemplate.update<Timetable>().matching(Timetable::id isEqualTo timeTableId).apply(
            Update().pull(
                Timetable::lectures.toDotPath(),
                Query.query(TimetableLecture::id isEqualTo timetableLectureId),
            ).currentDate(Timetable::updatedAt.toDotPath()),
        ).withOptions(FindAndModifyOptions.options().returnNew(true)).findModifyAndAwait()

    override suspend fun pullTimetableLectureByLectureId(
        timeTableId: String,
        lectureId: String,
    ): Timetable =
        reactiveMongoTemplate.update<Timetable>().matching(Timetable::id isEqualTo timeTableId).apply(
            Update().pull(
                Timetable::lectures.toDotPath(),
                Query.query(TimetableLecture::lectureId isEqualTo lectureId),
            ).currentDate(Timetable::updatedAt.toDotPath()),
        ).withOptions(FindAndModifyOptions.options().returnNew(true)).findModifyAndAwait()

    override suspend fun pullTimetableLectures(
        timeTableId: String,
        timetableLectureIds: List<String>,
    ): Timetable =
        reactiveMongoTemplate.update<Timetable>().matching(Timetable::id isEqualTo timeTableId).apply(
            Update().pull(
                Timetable::lectures.toDotPath(),
                Query.query(TimetableLecture::id.inValues(timetableLectureIds.map { ObjectId(it) })),
            ).currentDate(Timetable::updatedAt.toDotPath()),
        ).withOptions(FindAndModifyOptions.options().returnNew(true)).findModifyAndAwait()

    override suspend fun updateTimetableLecture(
        timeTableId: String,
        timetableLecture: TimetableLecture,
    ): Timetable =
        reactiveMongoTemplate.update<Timetable>().matching(
            Timetable::id.isEqualTo(timeTableId).and("lecture_list._id").isEqualTo(ObjectId(timetableLecture.id)),
        ).apply(Update().apply { set("""lecture_list.$""", timetableLecture) }.currentDate(Timetable::updatedAt.toDotPath()))
            .withOptions(FindAndModifyOptions.options().returnNew(true)).findModifyAndAwait()

    override suspend fun findLatestChildTimetable(
        userId: String,
        year: Int,
        semester: Semester,
        title: String,
    ): Timetable? =
        reactiveMongoTemplate.findOne(
            Query.query(
                Timetable::userId isEqualTo userId and
                    Timetable::year isEqualTo year and
                    Timetable::semester isEqualTo semester and
                    Timetable::title regex """${Regex.escape(title)}(\s+\(\d+\))?""",
            ).with(Timetable::title.desc()),
            Timetable::class.java,
        ).awaitSingleOrNull()
}
