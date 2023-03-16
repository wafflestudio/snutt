package com.wafflestudio.snu4t.lectures.repository

import com.wafflestudio.snu4t.lectures.data.Lecture
import org.springframework.data.domain.Page
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

interface LectureCustomRepository {
    suspend fun searchLectures(): Page<Lecture>
}

class LectureCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : LectureCustomRepository {
    override suspend fun searchLectures(): Page<Lecture> {
        TODO("Not yet implemented")
    }
}
