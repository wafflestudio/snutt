package com.wafflestudio.snutt.common.enums

import com.wafflestudio.snutt.common.client.Language
import com.wafflestudio.snutt.common.client.select

/**
 * 2025 교양 개편 이전 교양영역(구 교양영역).
 *
 * 수강신청 엑셀이 아니라 batch의 categoryPre2025.txt(과목번호 → 한글)에서 오는 값이라
 * Lecture에 영문 필드가 따로 없다. 그래서 DB에는 한글만 저장하고 응답/요청 경계에서 양방향으로 변환한다.
 * 한글 표기는 categoryPre2025.txt, 영문 표기는 수강신청 공통코드(SBJT_FLD)를 따른다.
 */
enum class LectureCategoryPre2025(
    val fullName: String,
    val fullNameEn: String,
) {
    // 학문의 기초
    FOUNDATION_WRITING("사고와 표현", "Critical Thinking and Writing"),
    FOUNDATION_LANGUAGE("외국어", "Foreign Languages"),
    FOUNDATION_MATH("수량적 분석과 추론", "Mathematical Sciences"),
    FOUNDATION_SCIENCE("과학적 사고와 실험", "Natural Sciences"),
    FOUNDATION_COMPUTER("컴퓨터와 정보 활용", "Computer and Information Science"),

    // 학문의 세계
    KNOWLEDGE_LITERATURE("언어와 문학", "Language and Literature"),
    KNOWLEDGE_ART("문화와 예술", "Culture and Art"),
    KNOWLEDGE_HISTORY("역사와 철학", "History and Philosophy"),
    KNOWLEDGE_POLITICS("정치와 경제", "Politics and Economy"),
    KNOWLEDGE_HUMAN("인간과 사회", "Humans and Society"),
    KNOWLEDGE_NATURE("자연과 기술", "Nature and Technology"),
    KNOWLEDGE_LIFE("생명과 환경", "Life and Environment"),

    // 선택교양
    GENERAL_PHYSICAL("체육", "Physical Education"),
    GENERAL_ART("예술 실기", "Art Practice"),
    GENERAL_COLLEGE("대학과 리더십", "College Life and Leadership"),
    GENERAL_CREATIVITY("창의와 융합", "Creativity and Convergence"),
    GENERAL_KOREAN("한국의 이해", "Korea in the World (Courses in English)"),
    ;

    companion object {
        // 클라이언트가 언어에 따라 한글/영문 중 무엇을 보내든 받을 수 있도록 둘 다 키로 등록한다.
        private val nameMap = entries.flatMap { listOf(it.fullName to it, it.fullNameEn to it) }.toMap()

        fun getOfName(categoryName: String?): LectureCategoryPre2025? = nameMap[categoryName]

        /** 응답용. DB의 한글 값을 요청 언어에 맞춰 바꾼다. 매핑에 없는 값은 원본을 그대로 둔다. */
        fun localize(
            categoryName: String,
            language: Language,
        ): String = language.select(categoryName, getOfName(categoryName)?.fullNameEn)

        /** 요청용. 한글/영문 어느 쪽으로 들어와도 DB에 저장·조회할 한글 값으로 되돌린다. */
        fun toKorean(categoryName: String): String = getOfName(categoryName)?.fullName ?: categoryName
    }
}
