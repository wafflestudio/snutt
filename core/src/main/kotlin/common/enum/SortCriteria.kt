package com.wafflestudio.snutt.common.enum

import com.fasterxml.jackson.annotation.JsonValue
import com.wafflestudio.snutt.common.extension.desc
import com.wafflestudio.snutt.lectures.data.EvInfo
import com.wafflestudio.snutt.lectures.data.Lecture
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.div

enum class SortCriteria(
    val value: Int,
    @JsonValue
    val fullName: String,
) {
    ID(1, "기본값"),
    RATING_DESC(2, "평점 높은 순"),
    COUNT_DESC(3, "강의평 많은 순"),
    ;

    // RATING_ASC(4, "평점 낮은 순"),
    // COUNT_ASC(5, "강의평 적은 순");

    companion object {
        private val nameMap = entries.associateBy { it.fullName }

        fun getOfName(sortCriteriaName: String?): SortCriteria? = nameMap[sortCriteriaName]

        fun getSort(sortCriteria: SortCriteria?): Sort =
            when (sortCriteria) {
                RATING_DESC -> (Lecture::evInfo / EvInfo::avgRating).desc()
                COUNT_DESC -> (Lecture::evInfo / EvInfo::count).desc()
                // RATING_ASC -> (Lecture::evInfo / EvInfo::avgRating).asc()
                // COUNT_ASC -> (Lecture::evInfo / EvInfo::count).asc()
                else -> Sort.unsorted()
            }
    }
}
