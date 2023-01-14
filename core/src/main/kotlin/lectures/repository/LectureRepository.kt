package com.wafflestudio.snu4t.lectures.repository

import com.wafflestudio.snu4t.lectures.data.Lecture
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LectureRepository : CoroutineCrudRepository<Lecture, String>, LectureRepositoryCustom
