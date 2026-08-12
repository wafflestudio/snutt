package com.wafflestudio.snutt.sugangsnu.common.utils

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.coursebook.data.Coursebook
import com.wafflestudio.snutt.lectures.data.Lecture
import kotlin.reflect.KProperty1

// 영문(_en) 필드는 한글 짝과 같은 항목명으로 표기한다.
// 한/영이 함께 바뀌어도 호출부의 distinct()로 합쳐져 '비고, 기타'가 아닌 '비고'로 나간다.
fun KProperty1<Lecture, *>.toKoreanFieldName(): String =
    when (this) {
        Lecture::classification, Lecture::classificationEn -> "교과 구분"
        Lecture::department, Lecture::departmentEn -> "학부"
        Lecture::academicYear, Lecture::academicYearEn -> "학년"
        Lecture::courseTitle, Lecture::courseTitleEn -> "강의명"
        Lecture::credit -> "학점"
        Lecture::instructor, Lecture::instructorEn -> "교수"
        Lecture::quota -> "정원"
        Lecture::remark, Lecture::remarkEn -> "비고"
        Lecture::category, Lecture::categoryEn -> "교양영역"
        Lecture::classPlaceAndTimes -> "강의 시간/장소"
        Lecture::categoryPre2025 -> "구) 교양영역"
        else -> "기타"
    }

fun Coursebook.nextCoursebook(): Coursebook =
    when (this.semester) {
        Semester.SPRING -> Coursebook(year = this.year, semester = Semester.SUMMER)
        Semester.SUMMER -> Coursebook(year = this.year, semester = Semester.AUTUMN)
        Semester.AUTUMN -> Coursebook(year = this.year, semester = Semester.WINTER)
        Semester.WINTER -> Coursebook(year = this.year + 1, semester = Semester.SPRING)
    }
