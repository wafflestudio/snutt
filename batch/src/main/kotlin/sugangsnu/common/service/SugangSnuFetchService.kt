package com.wafflestudio.snutt.sugangsnu.common.service

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.pre2025category.service.CategoryPre2025FetchService
import com.wafflestudio.snutt.sugangsnu.common.SugangSnuRepository
import com.wafflestudio.snutt.sugangsnu.common.data.LectureIdentifier
import com.wafflestudio.snutt.sugangsnu.common.utils.SugangSnuClassTimeUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.apache.poi.ss.usermodel.Cell
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface SugangSnuFetchService {
    suspend fun getSugangSnuLectures(
        year: Int,
        semester: Semester,
    ): List<Lecture>

    suspend fun getSugangSnuSearchContent(
        year: Int,
        semester: Semester,
        pageNo: Int,
    ): Element

    suspend fun getPageCount(
        year: Int,
        semester: Semester,
    ): Int
}

@Service
class SugangSnuFetchServiceImpl(
    private val sugangSnuRepository: SugangSnuRepository,
    private val categoryPre2025FetchService: CategoryPre2025FetchService,
) : SugangSnuFetchService {
    private val log = LoggerFactory.getLogger(javaClass)
    private val quotaRegex = """(?<quota>\d+)(\s*\((?<quotaForCurrentStudent>\d+)\))?""".toRegex()
    private val courseNumberRegex = """(?<courseNumber>.*)\((?<lectureNumber>.+)\)""".toRegex()

    companion object {
        private const val COUNT_PER_PAGE = 10
    }

    override suspend fun getSugangSnuLectures(
        year: Int,
        semester: Semester,
    ): List<Lecture> {
        val courseNumberCategoryPre2025Map = categoryPre2025FetchService.getCategoriesPre2025()
        val pageCount = getPageCount(year, semester)

        return (1..pageCount).chunked(pageCount / 20).flatMap { chunkedPages ->
            val lectureIdentifiers = getLectureIdentifiers(year, semester, chunkedPages)
            lectureIdentifiers.map { (courseNumber, lectureNumber) ->
                val lectureInfo = sugangSnuRepository.getLectureInfo(year, semester, courseNumber, lectureNumber)
                val classPlaceAndTimes =
                    SugangSnuClassTimeUtils.convertTextToClassTimeObject(
                        lectureInfo.ltTime,
                        lectureInfo.ltRoom.map { it.replace("(무선랜제공)", "") },
                    )
                val subInfo = lectureInfo.subInfo

                Lecture(
                    academicYear =
                        subInfo.academicCourse.takeIf { it != "학사" }
                            ?: subInfo.academicYear?.let { "${it}학년" },
                    category = subInfo.category,
                    categoryPre2025 = courseNumberCategoryPre2025Map[courseNumber],
                    classification = subInfo.classification,
                    classPlaceAndTimes = classPlaceAndTimes,
                    courseNumber = courseNumber,
                    courseTitle =
                        if (subInfo.courseSubName.isNullOrEmpty()) {
                            subInfo.courseName!!
                        } else {
                            "${subInfo.courseName} (${subInfo.courseSubName})"
                        },
                    credit = subInfo.credit?.toLong() ?: 0,
                    department =
                        if (subInfo.departmentKorNm != null) {
                            if (subInfo.majorKorNm != null) {
                                "${subInfo.departmentKorNm}(${subInfo.majorKorNm})"
                            } else {
                                subInfo.departmentKorNm
                            }
                        } else {
                            subInfo.college
                        },
                    instructor = subInfo.professorName?.substringBeforeLast(" ("),
                    lectureNumber = lectureNumber,
                    quota = subInfo.quota ?: 0,
                    remark = subInfo.remark,
                    semester = semester,
                    year = year,
                )
            }
        }
    }

    override suspend fun getSugangSnuSearchContent(
        year: Int,
        semester: Semester,
        pageNo: Int,
    ): Element {
        val webPageDataBuffer = sugangSnuRepository.getSearchPageHtml(year, semester, pageNo)
        return try {
            Jsoup
                .parse(webPageDataBuffer.asInputStream(), Charsets.UTF_8.name(), "")
                .select("html > body > form#CC100 > div#wrapper > div#skip-con > div.content")
                .first()!!
        } finally {
            webPageDataBuffer.release()
        }
    }

    override suspend fun getPageCount(
        year: Int,
        semester: Semester,
    ): Int {
        val firstPageContent = getSugangSnuSearchContent(year, semester, 1)
        val totalCount =
            firstPageContent.select("div.content > div.search-result-con > small > em").text().toInt()
        return (totalCount + 9) / COUNT_PER_PAGE
    }

    private suspend fun getLectureIdentifiers(
        year: Int,
        semester: Semester,
        pages: List<Int>,
    ): List<LectureIdentifier> =
        supervisorScope {
            pages
                .map { page ->
                    async {
                        getSugangSnuSearchContent(year, semester, page).extractLectureIdentifier()
                    }
                }.awaitAll()
                .flatten()
        }

    /*
    엑셀 항목 (2023/01/26): 교과구분, 개설대학, 개설학과, 이수과정, 학년, 교과목번호, 강좌번호, 교과목명,
    부제명, 학점, 강의, 실습, 수업교시, 수업형태, 강의실(동-호)(#연건, *평창), 주담당교수,
    장바구니신청, 신입생장바구니신청, 재학생장바구니신청, 정원, 수강신청인원, 비고, 강의언어, 개설상태,
     */
    private fun convertSugangSnuRowToLecture(
        row: List<Cell>,
        columnNameIndex: Map<String, Int>,
        year: Int,
        semester: Semester,
    ): Lecture {
        fun List<Cell>.getCellByColumnName(key: String): String =
            this[
                columnNameIndex.getOrElse(key) {
                    log.error("$key 와 매칭되는 excel 컬럼이 존재하지 않습니다.")
                    this.size
                },
            ].stringCellValue

        val classification = row.getCellByColumnName("교과구분")
        val college = row.getCellByColumnName("개설대학")
        val department = row.getCellByColumnName("개설학과")
        val academicCourse = row.getCellByColumnName("이수과정")
        val academicYear = row.getCellByColumnName("학년")
        val courseNumber = row.getCellByColumnName("교과목번호")
        val lectureNumber = row.getCellByColumnName("강좌번호")
        val courseTitle = row.getCellByColumnName("교과목명")
        val courseSubtitle = row.getCellByColumnName("부제명")
        val credit = row.getCellByColumnName("학점").toLong()
        val classTimeText = row.getCellByColumnName("수업교시")
        val location = row.getCellByColumnName("강의실(동-호)(#연건, *평창)")
        val instructor = row.getCellByColumnName("주담당교수")
        val (quota, quotaForCurrentStudent) =
            row
                .getCellByColumnName("정원")
                .takeIf { quotaRegex.matches(it) }
                ?.let { quotaRegex.find(it)!!.groups }
                ?.let { it["quota"]!!.value.toInt() to (it["quotaForCurrentStudent"]?.value?.toInt() ?: 0) } ?: (0 to 0)
        val remark = row.getCellByColumnName("비고")
        val registrationCount = row.getCellByColumnName("수강신청인원").toIntOrNull() ?: 0

        val classTimes =
            SugangSnuClassTimeUtils.convertTextToClassTimeObject(classTimeText.split("/"), location.split("/"))

        val courseFullTitle = if (courseSubtitle.isEmpty()) courseTitle else "$courseTitle ($courseSubtitle)"

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
        )
    }

    private fun Element.extractLectureIdentifier() =
        this
            .select("div.content > div.course-list-wrap.pd-r > div.course-info-list > div.course-info-item")
            .map { course ->
                course
                    .select("div.course-info-item ul.course-info")
                    .first()!!
                    .let { info ->
                        val (courseNumber, lectureNumber) =
                            info
                                .select("li:nth-of-type(1) > span:nth-of-type(3)")
                                .text()
                                .takeIf { courseNumberRegex.matches(it) }!!
                                .let { courseNumberRegex.find(it)!!.groups }
                                .let { it["courseNumber"]!!.value to (it["lectureNumber"]!!.value) }
                        LectureIdentifier(
                            courseNumber = courseNumber,
                            lectureNumber = lectureNumber,
                        )
                    }
            }
}
