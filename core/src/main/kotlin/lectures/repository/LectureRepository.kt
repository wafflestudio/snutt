package com.wafflestudio.snu4t.lectures.repository

import com.wafflestudio.snu4t.lectures.data.LectureWithSemester
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LectureRepository : CoroutineCrudRepository<LectureWithSemester, String>, LectureRepositoryCustom
