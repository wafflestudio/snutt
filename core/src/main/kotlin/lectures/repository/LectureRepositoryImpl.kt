package com.wafflestudio.snu4t.lectures.repository

import com.wafflestudio.snu4t.lectures.data.LectureWithSemester
import org.springframework.data.domain.Page
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

class LectureRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : LectureRepositoryCustom {
    override suspend fun searchLectures(): Page<LectureWithSemester> {
        // TODO: 예시로 그냥 써둔거라 변경해야함
//        reactiveMongoTemplate.find<Lecture>(
//                Query.query(Criteria
//                        .where("key").`is`("value")
//                        .and("key2").`is`("value2")
//                ),
//        ).awaitFirstOrNull()
        return Page.empty()
    }
}
