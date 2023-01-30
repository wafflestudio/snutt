package com.wafflestudio.snu4t.lectures.repository

import com.wafflestudio.snu4t.common.isEqualTo
import com.wafflestudio.snu4t.lectures.data.Lecture
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.data.domain.Page
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findReplaceAndAwait
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.update

interface LectureCustomRepository {
    suspend fun searchLectures(): Page<Lecture>
    suspend fun upsertLectures(lectures: Iterable<Lecture>): List<Lecture>
}

class LectureCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : LectureCustomRepository {
    override suspend fun searchLectures(): Page<Lecture> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertLectures(lectures: Iterable<Lecture>): List<Lecture> =
        coroutineScope {
            lectures.map { lecture ->
                async {
                    reactiveMongoTemplate.update<Lecture>().matching(
                        Lecture::year isEqualTo lecture.year and
                                Lecture::semester isEqualTo lecture.semester and
                                Lecture::courseNumber isEqualTo lecture.courseNumber and
                                Lecture::lectureNumber isEqualTo lecture.lectureNumber
                    ).replaceWith(lecture)
                        .withOptions(FindAndReplaceOptions().upsert())
                        .findReplaceAndAwait()
                }
            }.awaitAll()
        }
}
