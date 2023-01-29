package com.wafflestudio.snu4t.sugangsnu

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.coursebook.data.Coursebook
import com.wafflestudio.snu4t.coursebook.repository.CoursebookRepository
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.utils.ClassTimeUtils
import com.wafflestudio.snu4t.sugangsnu.data.SugangSnuCoursebookCondition
import com.wafflestudio.snu4t.sugangsnu.data.SugangSnuLectureCompareResult
import com.wafflestudio.snu4t.sugangsnu.enum.LectureCategory
import com.wafflestudio.snu4t.sugangsnu.utils.SugangSnuClassTimeUtils
import com.wafflestudio.snu4t.sugangsnu.utils.toSugangSnuSearchString
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface SugangSnuFetchService {
    suspend fun getOrCreateLatestCoursebook(): Coursebook
    suspend fun getLectures(year: Int, semester: Semester): List<Lecture>
    fun compareLectures(newLectures: List<Lecture>, oldLectures: List<Lecture>): SugangSnuLectureCompareResult
}

@Service
class SugangSnuFetchServiceImpl(
    private val sugangSnuRepository: SugangSnuRepository,
    private val coursebookRepository: CoursebookRepository
) : SugangSnuFetchService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun getOrCreateLatestCoursebook(): Coursebook {
        val existingLatestCoursebook = coursebookRepository.findFirstByOrderByYearDescSemesterDesc()
        val sugangSnuLatestCoursebook = sugangSnuRepository.getCoursebookCondition()
        return if (!existingLatestCoursebook.isSyncedToSugangSnu(sugangSnuLatestCoursebook)) {
            coursebookRepository.save(existingLatestCoursebook.nextCoursebook())
        } else existingLatestCoursebook
    }

    override suspend fun getLectures(year: Int, semester: Semester): List<Lecture> {
        return LectureCategory.values().flatMap { lectureCategory ->
            val lectureXlsx = sugangSnuRepository.getSugangSnuLectures(year, semester, lectureCategory)
            val sheet = HSSFWorkbook(lectureXlsx.asInputStream()).getSheetAt(0)
            val columnNameIndex = sheet.getRow(2).associate { it.stringCellValue to it.columnIndex }
            sheet.filterIndexed { index, _ -> index > 2 }
                .map { row -> convertSugangSnuRowToLecture(row, columnNameIndex, lectureCategory, year, semester) }
                .also { lectureXlsx.release() }
        }
    }

    override fun compareLectures(
        newLectures: List<Lecture>,
        oldLectures: List<Lecture>
    ): SugangSnuLectureCompareResult {
        val newMap = newLectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }
        val oldMap = oldLectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }

        val created = (newMap.keys - oldMap.keys).map(newMap::getValue)
        val updated = (newMap.keys intersect oldMap.keys)
            .map { oldMap[it]!! to newMap[it]!! }
            .filter { (old, new) -> old != new }
        val deleted = (oldMap.keys - newMap.keys).map(oldMap::getValue)

        return SugangSnuLectureCompareResult(created, deleted, updated)
    }

    /*
    엑셀 항목 (2023/01/26): 교과구분, 개설대학, 개설학과, 이수과정, 학년, 교과목번호, 강좌번호, 교과목명,
    부제명, 학점, 강의, 실습, 수업교시, 수업형태, 강의실(동-호)(#연건, *평창), 주담당교수,
    장바구니신청, 신입생장바구니신청, 재학생장바구니신청, 정원, 수강신청인원, 비고, 강의언어, 개설상태,
     */
    private fun convertSugangSnuRowToLecture(
        row: Row,
        columnNameIndex: Map<String, Int>,
        category: LectureCategory = LectureCategory.NONE,
        year: Int,
        semester: Semester,
    ): Lecture {
        fun Row.getCellByColumnName(key: String): String? =
            this.getCell(columnNameIndex.getOrElse(key) {
                // TODO: slack 메시지로 보내기
                logger.error("$key 와 매칭되는 excel 컬럼이 존재하지 않습니다.")
                this.lastCellNum.toInt()
            })?.stringCellValue

        val classification = row.getCellByColumnName("교과구분")!!
        val college = row.getCellByColumnName("개설대학")!!
        val department = row.getCellByColumnName("개설학과")!!
        val academicCourse = row.getCellByColumnName("이수과정")!!
        val academicYear = row.getCellByColumnName("학년")!!
        val courseNumber = row.getCellByColumnName("교과목번호")!!
        val lectureNumber = row.getCellByColumnName("강좌번호")!!
        val courseTitle = row.getCellByColumnName("교과목명")!!
        val courseSubtitle = row.getCellByColumnName("부제명")!!
        val credit = row.getCellByColumnName("학점")?.toInt()!!
        val classTimeText = row.getCellByColumnName("수업교시")!!
        val location = row.getCellByColumnName("강의실(동-호)(#연건, *평창)")!!
        val instructor = row.getCellByColumnName("주담당교수")!!
        val quota = row.getCellByColumnName("정원")!!
        val remark = row.getCellByColumnName("비고")!!

        val periodText = SugangSnuClassTimeUtils.convertClassTimeTextToPeriodText(classTimeText)
        val classTime = SugangSnuClassTimeUtils.convertTextToClassTimeObject(classTimeText, location)
        val classTimeMask = ClassTimeUtils.classTimeToBitmask(classTime)

        val courseFullTitle = if (courseSubtitle.isEmpty()) courseTitle else "$courseTitle (${courseSubtitle})"

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
            quota = quota.split(" ").first().toInt(),
            year = year,
            semester = semester,
            category = category.koreanName,
            classTimeText = classTimeText,
            periodText = periodText,
            classTime = classTime,
            classTimeMask = classTimeMask,
        )

    }

    private fun Coursebook.isSyncedToSugangSnu(sugangSnuCoursebookCondition: SugangSnuCoursebookCondition): Boolean {
        return this.year == sugangSnuCoursebookCondition.latestYear &&
                this.semester.toSugangSnuSearchString() == sugangSnuCoursebookCondition.latestSugangSnuSemester
    }

    private fun Coursebook.nextCoursebook(): Coursebook {
        return when (this.semester) {
            Semester.SPRING -> Coursebook(year = this.year, semester = Semester.SUMMER)
            Semester.SUMMER -> Coursebook(year = this.year, semester = Semester.AUTUMN)
            Semester.AUTUMN -> Coursebook(year = this.year, semester = Semester.WINTER)
            Semester.WINTER -> Coursebook(year = this.year + 1, semester = Semester.SPRING)
        }
    }
}
