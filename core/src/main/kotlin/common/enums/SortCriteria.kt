package com.wafflestudio.snutt.common.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.wafflestudio.snutt.common.extension.asc
import com.wafflestudio.snutt.common.extension.desc
import com.wafflestudio.snutt.lectures.data.EvInfo
import com.wafflestudio.snutt.lectures.data.Lecture
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.div
import kotlin.collections.get

enum class SortCriteria(
    val value: Int,
    @JsonValue
    val fullName: String,
    val fullNameEn: String,
) {
    ID(1, "기본값", "Default"),
    RATING_DESC(2, "평점 높은 순", "Highest rating"),
    COUNT_DESC(3, "강의평 많은 순", "Most reviews"),
    ;

    // RATING_ASC(4, "평점 낮은 순", "Lowest rating"),
    // COUNT_ASC(5, "강의평 적은 순", "Fewest reviews");

    companion object {
        // 클라이언트가 언어에 따라 한글/영문 중 무엇을 보내든 받을 수 있도록 둘 다 키로 등록한다.
        private val nameMap = entries.flatMap { listOf(it.fullName to it, it.fullNameEn to it) }.toMap()

        fun getOfName(sortCriteriaName: String?): SortCriteria? = nameMap[sortCriteriaName]

        fun getSort(sortCriteria: SortCriteria?): Sort =
            when (sortCriteria) {
                RATING_DESC -> (Lecture::evInfo / EvInfo::avgRating).desc().and(Lecture::id.asc())

                COUNT_DESC -> (Lecture::evInfo / EvInfo::count).desc().and(Lecture::id.asc())

                // RATING_ASC -> (Lecture::evInfo / EvInfo::avgRating).asc()
                // COUNT_ASC -> (Lecture::evInfo / EvInfo::count).asc()
                else -> Sort.unsorted()
            }
    }
}
