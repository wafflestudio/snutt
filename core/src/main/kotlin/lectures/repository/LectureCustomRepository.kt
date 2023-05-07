package com.wafflestudio.snu4t.lectures.repository

import com.wafflestudio.snu4t.common.all
import com.wafflestudio.snu4t.common.isEqualTo
import com.wafflestudio.snu4t.lectures.data.ClassPlaceAndTime
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.dto.SearchDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.gte
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.ne
import org.springframework.data.mongodb.core.query.nin
import org.springframework.data.mongodb.core.query.regex

interface LectureCustomRepository {
    fun searchLectures(query: SearchDto): Flow<Lecture>
}

class LectureCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : LectureCustomRepository {

    override fun searchLectures(search: SearchDto): Flow<Lecture> = reactiveMongoTemplate.find<Lecture>(
        Query.query(
            Criteria().andOperator(
                listOfNotNull(
                    Lecture::year isEqualTo search.year and Lecture::semester isEqualTo search.semester,
                    search.credit?.let { Lecture::credit inValues it },
                    search.academicYear?.let { Lecture::academicYear inValues it },
                    search.courseNumber?.let { Lecture::courseNumber inValues it },
                    search.classification?.let { Lecture::classification inValues it },
                    search.category?.let { Lecture::category inValues it },
                    search.department?.let { Lecture::department inValues it },
                    search.query?.let { makeSearchCriteriaFromQuery(it) },
                    search.times?.takeIf { it.isNotEmpty() }?.let {
                        // 시간이 존재하지 않는 경우 제외
                        Lecture::classPlaceAndTimes ne listOf() and Lecture::classPlaceAndTimes all (Criteria().orOperator(
                            it.map { time ->
                                ClassPlaceAndTime::startMinute.gte(time.startMinute)
                                    .and(ClassPlaceAndTime::endMinute).lte(time.endMinute)
                            }))
                    },
                    *search.etcTags.orEmpty().map { etcTag ->
                        when (etcTag) {
                            "E" -> Lecture::remark regex ".*ⓔ.*"
                            "MO" -> Lecture::remark regex ".*ⓜⓞ.*"
                            else -> null
                        }
                    }.toTypedArray()
                ),
            )
        )
    ).asFlow()

    private fun makeSearchCriteriaFromQuery(query: String): Criteria =
        Criteria().andOperator(
            query.split(' ').map { keyword ->
                val fuzzyKeyword = keyword.toCharArray().joinToString(".*")
                when {
                    keyword == "전공" -> Lecture::classification.inValues("전선", "전필")
                    keyword in listOf("석박", "대학원") -> Lecture::academicYear.inValues("석사", "박사", "석박사통합")
                    keyword in listOf("학부", "학사") -> Lecture::academicYear.nin("석사", "박사", "석박사통합")
                    keyword == "체육" -> Lecture::category isEqualTo "체육"
                    keyword in listOf("영강", "영어강의") -> Lecture::remark regex ".*ⓔ.*"
                    keyword in listOf("군휴학", "군휴학원격") -> Lecture::remark regex ".*ⓜⓞ.*"
                    keyword.hasKorean() -> Criteria().orOperator(
                        listOfNotNull(
                            Lecture::courseTitle.regex(fuzzyKeyword, "i"),
                            Lecture::category.regex(fuzzyKeyword, "i"),
                            Lecture::instructor isEqualTo keyword,
                            Lecture::academicYear isEqualTo keyword,
                            Lecture::classification isEqualTo keyword,
                            when (keyword.last()) {
                                /*
                                '컴공과', '전기과' 등으로 검색할 때, 실제 학과명은 '컴퓨터공학부', '전기공학부'이므로 검색이 안됨.
                                만약 '과' 혹은 '부'로 끝나는 단어라면 regex의 마지막 단어를 빼버린다.
                                */
                                '과', '부' -> Lecture::department.regex("^${fuzzyKeyword.dropLast(1)}", "i")
                                // 마지막 글자가 '학'이라면 해당 학과의 수업이 모두 포함될 확률이 높다. 수학, 물리학, 경제학 etc
                                '학' -> null
                                else -> Lecture::department.regex("^$fuzzyKeyword", "i")
                            }
                        )
                    )

                    else -> Criteria().orOperator(
                        Lecture::courseTitle.regex(keyword, "i"),
                        Lecture::instructor.regex(keyword, "i"),
                        Lecture::courseNumber isEqualTo keyword,
                        Lecture::lectureNumber isEqualTo keyword,
                    )
                }
            }
        )

    private fun Char.isHangul(): Boolean {
        return this in '가'..'힣'
    }

    private fun String.hasKorean(): Boolean {
        return this.map { it.isHangul() }.reduce { acc, c -> acc || c }
    }
}
