package com.wafflestudio.snu4t.tag.repository

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.tag.data.TagList
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TagListRepository : CoroutineCrudRepository<TagList, String> {
    suspend fun findByYearAndSemester(year: Int, semester: Semester): TagList?
}
