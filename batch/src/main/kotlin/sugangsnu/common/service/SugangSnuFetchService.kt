package com.wafflestudio.snutt.sugangsnu.common.service

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.sugangsnu.common.SugangSnuRepository
import com.wafflestudio.snutt.sugangsnu.common.data.RegistrationStatus
import com.wafflestudio.snutt.sugangsnu.common.utils.SugangSnuClassTimeUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
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

    suspend fun getRegistrationStatus(
        year: Int,
        semester: Semester,
        pages: List<Int>,
    ): List<RegistrationStatus>
}

@Service
class SugangSnuFetchServiceImpl(
    private val sugangSnuRepository: SugangSnuRepository,
    private val resourceLoader: ResourceLoader,
) : SugangSnuFetchService {
    private val log = LoggerFactory.getLogger(javaClass)
    private val courseNumberRegex = """(?<courseNumber>.*)\((?<lectureNumber>.+)\)""".toRegex()
    private val enrollmentRegex = """(?<registrationCount>\d+)\s*/\s*(?<quota>\d+)(\s*\((?<quotaForCurrentStudent>\d+)\))?""".toRegex()
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

    companion object {
        private const val COUNT_PER_PAGE = 10
    }

    override suspend fun getSugangSnuLectures(
        year: Int,
        semester: Semester,
    ): List<Lecture> {
        val pageCount = getPageCount(year, semester)
        log.info("total: $pageCount pages")

        return (1..pageCount).chunked(pageCount / 20).flatMap { chunkedPages ->
            val registrationStatus = getRegistrationStatus(year, semester, chunkedPages)
            registrationStatus.map { it ->
                val lectureInfo = sugangSnuRepository.getLectureInfo(year, semester, it.courseNumber, it.lectureNumber)
                val classPlaceAndTimes =
                    SugangSnuClassTimeUtils.convertTextToClassTimeObject(
                        lectureInfo.ltTime,
                        lectureInfo.ltRoom.map { it.replace("(무선랜제공)", "") },
                    )
                val subInfo = lectureInfo.subInfo

                Lecture(
                    academicYear =
                        subInfo.academicCourse.takeIf { it != "학사" }
                            ?: subInfo.academicYear?.let { "${it}학년" } ?: "",
                    category = subInfo.category ?: "",
                    categoryPre2025 = courseNumberCategoryPre2025Map[it.courseNumber],
                    classification = subInfo.classification,
                    classPlaceAndTimes = classPlaceAndTimes,
                    courseNumber = it.courseNumber,
                    courseTitle =
                        if (subInfo.courseSubName.isNullOrEmpty()) {
                            subInfo.courseName!!
                        } else {
                            "${subInfo.courseName} (${subInfo.courseSubName})"
                        },
                    credit = subInfo.credit?.toLong() ?: 0,
                    department =
                        if (!subInfo.departmentKorNm.isNullOrEmpty()) {
                            if (!subInfo.majorKorNm.isNullOrEmpty()) {
                                "${subInfo.departmentKorNm}(${subInfo.majorKorNm})"
                            } else {
                                subInfo.departmentKorNm
                            }
                        } else {
                            subInfo.college
                        } ?: "",
                    instructor = subInfo.professorName?.substringBeforeLast(" ("),
                    lectureNumber = it.lectureNumber,
                    quota = subInfo.quota ?: it.quota,
                    freshmanQuota = it.freshmanQuota,
                    remark = subInfo.remark ?: "",
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

    override suspend fun getRegistrationStatus(
        year: Int,
        semester: Semester,
        pages: List<Int>,
    ): List<RegistrationStatus> =
        supervisorScope {
            pages
                .map { page ->
                    async {
                        getSugangSnuSearchContent(year, semester, page).extractRegistrationStatus()
                    }
                }.awaitAll()
                .flatten()
        }

    private fun Element.extractRegistrationStatus() =
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
                        val regMatchResult =
                            enrollmentRegex.find(
                                info.select("ul.course-info > li:nth-of-type(2) > span:nth-of-type(1) > em").text(),
                            )
                        val registrationCount = regMatchResult?.groups["registrationCount"]?.value?.toInt() ?: 0
                        val quota = regMatchResult?.groups["quota"]?.value?.toInt() ?: 0
                        val quotaForCurrentStudent = regMatchResult?.groups["quotaForCurrentStudent"]?.value?.toInt() ?: 0
                        val freshmanQuota = (quota - quotaForCurrentStudent).takeIf { it > 0 }
                        val wasFull =
                            info
                                .select("li.state > span[data-dialog-target='remaining-place-dialog']")
                                .isNotEmpty()

                        RegistrationStatus(
                            courseNumber = courseNumber,
                            lectureNumber = lectureNumber,
                            registrationCount = registrationCount,
                            quota = quota,
                            freshmanQuota = freshmanQuota,
                            wasFull = wasFull,
                        )
                    }
            }
}
