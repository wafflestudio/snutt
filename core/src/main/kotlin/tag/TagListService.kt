package com.wafflestudio.snu4t.tag

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.exception.TagListNotFoundException
import com.wafflestudio.snu4t.tag.data.TagList
import com.wafflestudio.snu4t.tag.repository.TagListRepository
import org.springframework.stereotype.Service

interface TagListService {
    suspend fun getTagList(
        year: Int,
        semester: Semester,
    ): TagList
}

@Service
class TagServiceImpl(
    private val tagListRepository: TagListRepository,
) : TagListService {
    override suspend fun getTagList(
        year: Int,
        semester: Semester,
    ): TagList = tagListRepository.findByYearAndSemester(year, semester) ?: throw TagListNotFoundException
}
