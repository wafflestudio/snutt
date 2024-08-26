package com.wafflestudio.snu4t.tag.data

import com.fasterxml.jackson.annotation.JsonProperty

data class TagListResponse(
    val classification: List<String>,
    val department: List<String>,
    @JsonProperty("academic_year")
    val academicYear: List<String>,
    val credit: List<String>,
    val instructor: List<String>,
    val category: List<String>,
    val sortCriteria: List<String>,
    @JsonProperty("updated_at")
    val updatedAt: Long,
)
fun TagListResponse(tagList: TagList) = TagListResponse(
    classification = tagList.tagCollection.classification,
    department = tagList.tagCollection.department,
    academicYear = tagList.tagCollection.academicYear,
    credit = tagList.tagCollection.credit,
    instructor = tagList.tagCollection.instructor,
    category = tagList.tagCollection.category,
    sortCriteria = tagList.tagCollection.sortCriteria,
    updatedAt = tagList.updatedAt.toEpochMilli()
)
