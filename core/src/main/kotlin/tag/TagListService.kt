package com.wafflestudio.snutt.tag

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.exception.TagListNotFoundException
import com.wafflestudio.snutt.tag.data.TagList
import com.wafflestudio.snutt.tag.repository.TagListRepository
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
