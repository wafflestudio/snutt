package com.wafflestudio.snutt.lectures.repository

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.lectures.data.Lecture
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LectureRepository :
    CoroutineCrudRepository<Lecture, String>,
    LectureCustomRepository {
    fun findAllByYearAndSemester(
        year: Int,
        semester: Semester,
    ): Flow<Lecture>
}
