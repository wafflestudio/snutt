package com.wafflestudio.snu4t.sugangsnu.enum

enum class LectureCategory(val parentCategory: String, val queryValue: Long, val koreanName: String) {
        NONE("", 0, ""),

        // 학문의 기초
        FOUNDATION_WRITING("04", 40, "사고와 표현"),
        FOUNDATION_LANGUAGE("04", 41, "외국어"),
        FOUNDATION_MATH("04", 42, "수량적 분석과 추론"),
        FOUNDATION_SCIENCE("04", 43, "과학적 사고와 실험"),
        FOUNDATION_COMPUTER("04", 44, "컴퓨터와 정보 활용"),

        // 학문의 세계
        KNOWLEDGE_LITERATURE("05", 45, "언어와 문학"),
        KNOWLEDGE_ART("05", 46, "문화와 예술"),
        KNOWLEDGE_HISTORY("05", 47, "역사와 철학"),
        KNOWLEDGE_POLITICS("05", 48, "정치와 경제"),
        KNOWLEDGE_HUMAN("05", 49, "인간과 사회"),
        KNOWLEDGE_NATURE("05", 50, "자연과 기술"),
        KNOWLEDGE_LIFE("05", 51, "생명과 환경"),

        // 선택 교양
        GENERAL_PHYSICAL("06", 52, "체육"),
        GENERAL_ART("06", 53, "예술실기"),
        GENERAL_COLLEGE("06", 54, "대학과 리더쉽"),
        GENERAL_CREATIVITY("06", 55, "창의와 융합"),
        GENERAL_KOREAN("06", 56, "한국의 이해");
}
