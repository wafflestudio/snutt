package com.wafflestudio.snutt.tag.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.client.Language
import com.wafflestudio.snutt.common.client.select
import com.wafflestudio.snutt.common.enums.LectureCategoryPre2025
import com.wafflestudio.snutt.common.enums.SortCriteria

data class TagListResponse(
    val classification: List<String>,
    val department: List<String>,
    @param:JsonProperty("academic_year")
    val academicYear: List<String>,
    val credit: List<String>,
    val instructor: List<String>,
    val category: List<String>,
    val sortCriteria: List<String>,
    @param:JsonProperty("updated_at")
    val updatedAt: Long,
    val categoryPre2025: List<String>,
)

fun TagListResponse(
    tagList: TagList,
    language: Language = Language.KO,
) = run {
    // EN이면 영문 태그 우선. 영문 태그가 비어있으면(과거 학기 미백필) 한글로 폴백.
    fun localize(
        ko: List<String>,
        en: List<String>,
    ): List<String> = if (language == Language.EN) en.ifEmpty { ko } else ko
    TagListResponse(
        classification = localize(tagList.tagCollection.classification, tagList.tagCollection.classificationEn),
        department = localize(tagList.tagCollection.department, tagList.tagCollection.departmentEn),
        academicYear = localize(tagList.tagCollection.academicYear, tagList.tagCollection.academicYearEn),
        credit = tagList.tagCollection.credit,
        instructor = localize(tagList.tagCollection.instructor, tagList.tagCollection.instructorEn),
        category = localize(tagList.tagCollection.category, tagList.tagCollection.categoryEn),
        sortCriteria =
            SortCriteria.entries
                .sortedBy { it.value }
                .filterNot { it == SortCriteria.ID }
                .map { language.select(it.fullName, it.fullNameEn) },
        updatedAt = tagList.updatedAt.toEpochMilli(),
        categoryPre2025 =
            tagList.tagCollection.categoryPre2025.map {
                LectureCategoryPre2025.localize(it, language)
            },
    )
}
