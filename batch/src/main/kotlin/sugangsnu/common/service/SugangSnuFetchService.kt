package com.wafflestudio.snutt.sugangsnu.common.service

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.sugangsnu.common.SugangSnuRepository
import com.wafflestudio.snutt.sugangsnu.common.utils.SugangSnuClassTimeUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

interface SugangSnuFetchService {
    suspend fun getSugangSnuLectures(
        year: Int,
        semester: Semester,
    ): List<Lecture>
}

@Service
class SugangSnuFetchServiceImpl(
    private val sugangSnuRepository: SugangSnuRepository,
    private val resourceLoader: ResourceLoader,
) : SugangSnuFetchService {
    private val log = LoggerFactory.getLogger(javaClass)
    private val quotaRegex = """(?<quota>\d+)(\s*\((?<quotaForCurrentStudent>\d+)\))?""".toRegex()
    private val courseNumberCategoryPre2025Map: Map<String, String> by lazy {
        resourceLoader
            .getResource("classpath:categoryPre2025.txt")
            .inputStream
            .bufferedReader()
            .lineSequence()
            .filter { it.contains(":") }
            .associate { line ->
                val (courseNumber, category) = line.split(":", limit = 2)
                courseNumber to category
            }
    }

    override suspend fun getSugangSnuLectures(
        year: Int,
        semester: Semester,
    ): List<Lecture> {
        val koreanLectureXlsx = sugangSnuRepository.getSugangSnuLectures(year, semester, "ko")
        val englishLectureXlsx = sugangSnuRepository.getSugangSnuLectures(year, semester, "en")
        val koreanRows = HSSFWorkbook(koreanLectureXlsx.asInputStream()).getSheetAt(0).map { it.toList() }
        val englishRows = HSSFWorkbook(englishLectureXlsx.asInputStream()).getSheetAt(0).map { it.toList() }
        val koreanColumnIndex = koreanRows[2].associate { it.stringCellValue to it.columnIndex }
        val englishColumnIndex = englishRows[2].associate { it.stringCellValue to it.columnIndex }

        // 한/영 엑셀은 행 순서가 어긋날 수 있어 인덱스(zip) 결합 대신 (교과목번호, 강좌번호) 키로 조인한다.
        // 키가 없는 강의는 englishRow == null 이 되어 _en 필드가 null → 읽기 시점 KO 폴백으로 자연 수렴한다.
        val englishRowByKey =
            englishRows.drop(3).associateBy {
                it.getCellByColumnName(englishColumnIndex, "Course Number") to
                    it.getCellByColumnName(englishColumnIndex, "Lecture Number")
            }
        return koreanRows
            .drop(3)
            .map { koreanRow ->
                val key =
                    koreanRow.getCellByColumnName(koreanColumnIndex, "교과목번호") to
                        koreanRow.getCellByColumnName(koreanColumnIndex, "강좌번호")
                convertSugangSnuRowToLecture(
                    koreanRow,
                    koreanColumnIndex,
                    englishRowByKey[key],
                    englishColumnIndex,
                    year,
                    semester,
                )
            }.also {
                koreanLectureXlsx.release()
                englishLectureXlsx.release()
            }.map { lecture ->
                val extraLectureInfo =
                    sugangSnuRepository.getLectureInfo(year, semester, lecture.courseNumber, lecture.lectureNumber)

                val extraCourseTitle =
                    if (extraLectureInfo.subInfo.courseSubName.isNullOrEmpty()) {
                        extraLectureInfo.subInfo.courseName
                    } else {
                        "${extraLectureInfo.subInfo.courseName} (${extraLectureInfo.subInfo.courseSubName})"
                    }
                val extraDepartment =
                    if (extraLectureInfo.subInfo.departmentKorNm != null && extraLectureInfo.subInfo.majorKorNm != null) {
                        "${extraLectureInfo.subInfo.departmentKorNm}(${extraLectureInfo.subInfo.majorKorNm})"
                    } else {
                        null
                    }

                val extraCourseTitleEn =
                    if (extraLectureInfo.subInfo.courseSubNameEng.isNullOrEmpty()) {
                        extraLectureInfo.subInfo.courseNameEng
                    } else {
                        "${extraLectureInfo.subInfo.courseNameEng} (${extraLectureInfo.subInfo.courseSubNameEng})"
                    }
                val extraDepartmentEn =
                    if (extraLectureInfo.subInfo.departmentEngNm != null && extraLectureInfo.subInfo.majorEngNm != null) {
                        "${extraLectureInfo.subInfo.departmentEngNm}(${extraLectureInfo.subInfo.majorEngNm})"
                    } else {
                        null
                    }

                lecture.apply {
                    classPlaceAndTimes =
                        SugangSnuClassTimeUtils.convertTextToClassTimeObject(
                            extraLectureInfo.ltTime,
                            extraLectureInfo.ltRoom.map { it.replace("(무선랜제공)", "") },
                        )
                    academicYear = extraLectureInfo.subInfo.academicCourse.takeIf { it != "학사" }
                        ?: extraLectureInfo.subInfo.academicYear?.let { "${it}학년" } ?: academicYear
                    courseTitle = extraCourseTitle ?: courseTitle
                    instructor = (extraLectureInfo.subInfo.professorName ?: instructor)?.substringBeforeLast(" (")
                    category = extraLectureInfo.subInfo.category ?: category
                    department = extraDepartment ?: department
                    quota = extraLectureInfo.subInfo.quota ?: quota
                    remark = extraLectureInfo.subInfo.remark ?: remark
                    categoryPre2025 = courseNumberCategoryPre2025Map[lecture.courseNumber]
                    academicYearEn = extraLectureInfo.subInfo.academicCourseEng.takeIf { it != "Bachelor" }
                        ?: extraLectureInfo.subInfo.academicYear?.takeIf { it.isNotBlank() } ?: academicYearEn
                    courseTitleEn = extraCourseTitleEn ?: courseTitleEn
                    instructorEn = extraLectureInfo.subInfo.professorNameEng
                        ?.substringBeforeLast(" (")
                        ?.takeIf { it.isNotBlank() } ?: instructorEn
                    categoryEn = extraLectureInfo.subInfo.categoryEng ?: categoryEn
                    departmentEn = extraDepartmentEn ?: departmentEn
                    remarkEn = extraLectureInfo.subInfo.remarkEng ?: remarkEn
                }
            }
    }

    /*
    엑셀 항목 (2023/01/26): 교과구분, 개설대학, 개설학과, 이수과정, 학년, 교과목번호, 강좌번호, 교과목명,
    부제명, 학점, 강의, 실습, 수업교시, 수업형태, 강의실(동-호)(#연건, *평창), 주담당교수,
    장바구니신청, 신입생장바구니신청, 재학생장바구니신청, 정원, 수강신청인원, 비고, 강의언어, 개설상태,
     */
    private fun List<Cell>.getCellByColumnName(
        columnNameIndex: Map<String, Int>,
        key: String,
    ): String =
        this[
            columnNameIndex.getOrElse(key) {
                log.error("$key 와 매칭되는 excel 컬럼이 존재하지 않습니다.")
                this.size
            },
        ].stringCellValue

    private fun convertSugangSnuRowToLecture(
        koreanRow: List<Cell>,
        koreanColumnIndex: Map<String, Int>,
        englishRow: List<Cell>?,
        englishColumnIndex: Map<String, Int>,
        year: Int,
        semester: Semester,
    ): Lecture {
        fun List<Cell>.ko(key: String) = getCellByColumnName(koreanColumnIndex, key)

        fun List<Cell>.en(key: String) = getCellByColumnName(englishColumnIndex, key)

        val classification = koreanRow.ko("교과구분")
        val college = koreanRow.ko("개설대학")
        val department = koreanRow.ko("개설학과")
        val academicCourse = koreanRow.ko("이수과정")
        val academicYear = koreanRow.ko("학년")
        val courseNumber = koreanRow.ko("교과목번호")
        val lectureNumber = koreanRow.ko("강좌번호")
        val courseTitle = koreanRow.ko("교과목명")
        val courseSubtitle = koreanRow.ko("부제명")
        val credit = koreanRow.ko("학점").toLong()
        val classTimeText = koreanRow.ko("수업교시")
        val location = koreanRow.ko("강의실(동-호)(#연건, *평창)")
        val instructor = koreanRow.ko("주담당교수")
        val (quota, quotaForCurrentStudent) =
            koreanRow
                .ko("정원")
                .takeIf { quotaRegex.matches(it) }
                ?.let { quotaRegex.find(it)!!.groups }
                ?.let { it["quota"]!!.value.toInt() to (it["quotaForCurrentStudent"]?.value?.toInt() ?: 0) } ?: (0 to 0)
        val remark = koreanRow.ko("비고")
        val registrationCount = koreanRow.ko("수강신청인원").toIntOrNull() ?: 0

        val classTimes =
            SugangSnuClassTimeUtils.convertTextToClassTimeObject(classTimeText.split("/"), location.split("/"))

        val courseFullTitle = if (courseSubtitle.isEmpty()) courseTitle else "$courseTitle ($courseSubtitle)"

        // 영문 엑셀 행은 (교과목번호, 강좌번호) 키로 조인된 것. 없으면 englishRow == null → _en 전부 null.
        // KO 파생 로직을 1:1 미러. 분야(category)는 엑셀 컬럼이 없어 KO와 동일하게 비움.
        val courseFullTitleEn =
            englishRow?.let {
                val courseTitleEn = it.en("Course Title")
                val courseSubtitleEn = it.en("Course Subtitle")
                if (courseSubtitleEn.isEmpty()) courseTitleEn else "$courseTitleEn ($courseSubtitleEn)"
            }

        return Lecture(
            classification = classification,
            // null(과학교육계) 존재한다고 함 (old snutt에서 참고)
            department = department.replace("null", "").ifEmpty { college },
            academicYear = academicCourse.takeIf { academicCourse != "학사" } ?: academicYear,
            courseNumber = courseNumber,
            lectureNumber = lectureNumber,
            courseTitle = courseFullTitle,
            credit = credit,
            instructor = instructor,
            remark = remark,
            quota = quota,
            freshmanQuota = (quota - quotaForCurrentStudent).takeIf { it > 0 },
            year = year,
            semester = semester,
            category = "",
            classPlaceAndTimes = classTimes,
            registrationCount = registrationCount,
            categoryPre2025 = null,
            classificationEn = englishRow?.en("Course Classification"),
            departmentEn = englishRow?.let { it.en("Department").replace("null", "").ifEmpty { it.en("College") } },
            academicYearEn = englishRow?.let { it.en("Degree Program").takeIf { dp -> dp != "Bachelor" } ?: it.en("Academic Year") },
            courseTitleEn = courseFullTitleEn,
            instructorEn = englishRow?.en("Instructor"),
            categoryEn = "",
            remarkEn = englishRow?.en("Remark"),
        )
    }
}
