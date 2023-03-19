package com.wafflestudio.snu4t.lectures.repository

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.Lecture
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LectureRepository : CoroutineCrudRepository<Lecture, String>, LectureCustomRepository {
    fun findAllByYearAndSemester(year: Int, semester: Semester): Flow<Lecture>
}
