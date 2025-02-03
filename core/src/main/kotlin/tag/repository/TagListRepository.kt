package com.wafflestudio.snutt.tag.repository

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.tag.data.TagList
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TagListRepository : CoroutineCrudRepository<TagList, String> {
    suspend fun findByYearAndSemester(
        year: Int,
        semester: Semester,
    ): TagList?
}
