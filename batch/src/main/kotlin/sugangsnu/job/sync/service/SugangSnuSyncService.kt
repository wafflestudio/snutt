package com.wafflestudio.snutt.sugangsnu.job.sync.service

import com.wafflestudio.snutt.bookmark.repository.BookmarkRepository
import com.wafflestudio.snutt.coursebook.data.Coursebook
import com.wafflestudio.snutt.coursebook.repository.CoursebookRepository
import com.wafflestudio.snutt.lecturebuildings.data.Campus
import com.wafflestudio.snutt.lecturebuildings.data.PlaceInfo
import com.wafflestudio.snutt.lecturebuildings.service.LectureBuildingService
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.lectures.service.LectureService
import com.wafflestudio.snutt.lectures.utils.ClassTimeUtils
import com.wafflestudio.snutt.sugangsnu.common.SugangSnuRepository
import com.wafflestudio.snutt.sugangsnu.common.data.SugangSnuCoursebookCondition
import com.wafflestudio.snutt.sugangsnu.common.service.SugangSnuFetchService
import com.wafflestudio.snutt.sugangsnu.common.utils.nextCoursebook
import com.wafflestudio.snutt.sugangsnu.common.utils.toKoreanFieldName
import com.wafflestudio.snutt.sugangsnu.job.sync.data.BookmarkLectureDeleteResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.BookmarkLectureUpdateResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.SugangSnuLectureCompareResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.TimetableLectureDeleteByOverlapResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.TimetableLectureDeleteResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.TimetableLectureUpdateResult
import com.wafflestudio.snutt.sugangsnu.job.sync.data.UpdatedLecture
import com.wafflestudio.snutt.sugangsnu.job.sync.data.UserLectureSyncResult
import com.wafflestudio.snutt.tag.data.TagCollection
import com.wafflestudio.snutt.tag.data.TagList
import com.wafflestudio.snutt.tag.repository.TagListRepository
import com.wafflestudio.snutt.timetables.data.Timetable
import com.wafflestudio.snutt.timetables.event.data.TimetableLectureModifiedEvent
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.reflect.full.memberProperties

interface SugangSnuSyncService {
    suspend fun updateCoursebook(coursebook: Coursebook): List<UserLectureSyncResult>

    suspend fun addCoursebook(coursebook: Coursebook)

    suspend fun isSyncWithSugangSnu(latestCoursebook: Coursebook): Boolean
}

@Service
class SugangSnuSyncServiceImpl(
    private val sugangSnuFetchService: SugangSnuFetchService,
    private val lectureService: LectureService,
    private val timeTableRepository: TimetableRepository,
    private val sugangSnuRepository: SugangSnuRepository,
    private val coursebookRepository: CoursebookRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val tagListRepository: TagListRepository,
    private val lectureBuildingService: LectureBuildingService,
    private val eventPublisher: ApplicationEventPublisher,
) : SugangSnuSyncService {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val THRESHOLD_SEARCH_PAGES = 5
    }

    override suspend fun updateCoursebook(coursebook: Coursebook): List<UserLectureSyncResult> {
        log.info("${coursebook.year}년도 ${coursebook.semester.fullName} 강의 업데이트 시작")
        val newLectures =
            sugangSnuFetchService.getSugangSnuLectures(coursebook.year, coursebook.semester)
        val oldLectures =
            lectureService.getLecturesByYearAndSemesterAsFlow(coursebook.year, coursebook.semester).toList()
        val compareResult = compareLectures(newLectures, oldLectures)

        compareResult.updatedLectureList.forEach {
            log.info("강의 업데이트: ${it.newData.courseNumber} ${it.newData.courseTitle} (${it.newData.lectureNumber})")
            log.info("항목: ${it.updatedField.map { it2 -> it2.toKoreanFieldName() }}")
        }
        log.info("추가된 강의: ${compareResult.createdLectureList.size}개, 삭제된 강의: ${compareResult.deletedLectureList.size}개")

        syncLectures(compareResult)
        val syncUserLecturesResults = syncSavedUserLectures(compareResult)
        syncTagList(coursebook, newLectures)
        coursebookRepository.save(coursebook.apply { updatedAt = Instant.now() })
        runCatching { updateLectureBuildings(compareResult) }.onFailure { log.error("Failed to update lecture buildings", it) }

        return syncUserLecturesResults
    }

    override suspend fun addCoursebook(coursebook: Coursebook) {
        val newLectures =
            sugangSnuFetchService.getSugangSnuLectures(coursebook.year, coursebook.semester)
        lectureService.upsertLectures(newLectures)
        syncTagList(coursebook, newLectures)

        coursebookRepository.save(coursebook)
    }

    override suspend fun isSyncWithSugangSnu(latestCoursebook: Coursebook): Boolean {
        val sugangSnuLatestCoursebook = sugangSnuRepository.getCoursebookCondition()
        return latestCoursebook.isSyncedToSugangSnu(sugangSnuLatestCoursebook)
    }

    private fun compareLectures(
        newLectures: Iterable<Lecture>,
        oldLectures: Iterable<Lecture>,
    ): SugangSnuLectureCompareResult {
        val newMap = newLectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }
        val oldMap = oldLectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }

        val created = (newMap.keys - oldMap.keys).map(newMap::getValue)
        val updated =
            (newMap.keys intersect oldMap.keys)
                .map { oldMap[it]!! to newMap[it]!! }
                .filter { (old, new) -> !(old equalsMetadata new) }
                .map { (old, new) ->
                    UpdatedLecture(
                        old,
                        new,
                        Lecture::class.memberProperties.filter {
                            it != Lecture::id && it != Lecture::evInfo && it.get(old) != it.get(new)
                        },
                    )
                }
        val deleted =
            (oldMap.keys - newMap.keys).map(
                oldMap::getValue,
            )

        return SugangSnuLectureCompareResult(created, deleted, updated)
    }

    private suspend fun syncTagList(
        coursebook: Coursebook,
        lectures: Iterable<Lecture>,
    ) {
        val tagCollection =
            lectures
                .fold(ParsedTags()) { acc, lecture ->
                    ParsedTags(
                        academicYear = acc.academicYear + lecture.academicYear,
                        classification = acc.classification + lecture.classification,
                        department = acc.department + lecture.department,
                        credit = acc.credit + lecture.credit,
                        instructor = acc.instructor + lecture.instructor,
                        category = acc.category + lecture.category,
                        categoryPre2025 = acc.categoryPre2025 + lecture.categoryPre2025,
                    )
                }.let { parsedTag ->
                    TagCollection(
                        // 엑셀 academicYear 필드 '학년' 안 붙어 나오는 경우 제외
                        academicYear =
                            parsedTag.academicYear
                                .filterNotNull()
                                .filter { it.length > 1 }
                                .sorted(),
                        classification =
                            parsedTag.classification
                                .filterNotNull()
                                .filter { it.isNotBlank() }
                                .sorted(),
                        department =
                            parsedTag.department
                                .filterNotNull()
                                .filter { it.isNotBlank() }
                                .sorted(),
                        credit = parsedTag.credit.sorted().map { "${it}학점" },
                        instructor =
                            parsedTag.instructor
                                .filterNotNull()
                                .filter { it.isNotBlank() }
                                .sorted(),
                        category =
                            parsedTag.category
                                .filterNotNull()
                                .filter { it.isNotBlank() }
                                .sorted(),
                        categoryPre2025 =
                            parsedTag.categoryPre2025
                                .filterNotNull()
                                .filter { it.isNotBlank() }
                                .sorted(),
                    )
                }
        val tagList =
            tagListRepository
                .findByYearAndSemester(coursebook.year, coursebook.semester)
                ?.copy(tagCollection = tagCollection, updatedAt = Instant.now()) ?: TagList(
                year = coursebook.year,
                semester = coursebook.semester,
                tagCollection = tagCollection,
            )
        tagListRepository.save(tagList)
    }

    private suspend fun syncLectures(compareResult: SugangSnuLectureCompareResult) {
        val updatedLectures =
            compareResult.updatedLectureList.map { diff ->
                diff.newData.apply {
                    id = diff.oldData.id
                    evInfo = diff.oldData.evInfo
                }
            }

        lectureService.upsertLectures(compareResult.createdLectureList)
        lectureService.upsertLectures(updatedLectures)
        lectureService.deleteLectures(compareResult.deletedLectureList)
    }

    private suspend fun syncSavedUserLectures(compareResult: SugangSnuLectureCompareResult): List<UserLectureSyncResult> =
        merge(
            syncTimetableLectures(compareResult),
            syncBookmarks(compareResult),
        ).toList()

    private fun syncTimetableLectures(compareResult: SugangSnuLectureCompareResult) =
        merge(
            compareResult.updatedLectureList.map { checkOverlapAndUpdateTimetableLectures(it) }.merge(),
            compareResult.deletedLectureList.map { deleteTimetableLectures(it) }.merge(),
        )

    private fun syncBookmarks(compareResult: SugangSnuLectureCompareResult) =
        merge(
            compareResult.updatedLectureList.map { updateBookmarkLectures(it) }.merge(),
            compareResult.deletedLectureList.map { deleteBookmarkLectures(it) }.merge(),
        )

    private fun updateBookmarkLectures(updatedLecture: UpdatedLecture) =
        bookmarkRepository
            .findAllContainsLectureId(
                updatedLecture.oldData.year,
                updatedLecture.oldData.semester,
                updatedLecture.oldData.id!!,
            ).map { bookmark ->
                val updatedBookmarkLecture =
                    bookmark.lectures.find { it.id == updatedLecture.oldData.id }?.apply {
                        academicYear = updatedLecture.newData.academicYear
                        category = updatedLecture.newData.category
                        classPlaceAndTimes = updatedLecture.newData.classPlaceAndTimes
                        classification = updatedLecture.newData.classification
                        credit = updatedLecture.newData.credit
                        department = updatedLecture.newData.department
                        instructor = updatedLecture.newData.instructor
                        quota = updatedLecture.newData.quota
                        freshmanQuota = updatedLecture.newData.freshmanQuota
                        remark = updatedLecture.newData.remark
                        lectureNumber = updatedLecture.newData.lectureNumber
                        courseNumber = updatedLecture.newData.courseNumber
                        courseTitle = updatedLecture.newData.courseTitle
                        categoryPre2025 = updatedLecture.newData.categoryPre2025
                    }!!
                bookmarkRepository.updateLecture(bookmark.id!!, updatedBookmarkLecture)
            }.map { bookmark ->
                BookmarkLectureUpdateResult(
                    bookmark.year,
                    bookmark.semester,
                    updatedLecture.oldData.courseTitle,
                    bookmark.userId,
                    updatedLecture.oldData.id!!,
                    updatedLecture.updatedField,
                )
            }

    private fun checkOverlapAndUpdateTimetableLectures(updatedLecture: UpdatedLecture): Flow<UserLectureSyncResult> =
        timeTableRepository
            .findAllContainsLectureId(
                updatedLecture.oldData.year,
                updatedLecture.oldData.semester,
                updatedLecture.oldData.id!!,
            ).let { timetables ->
                merge(
                    updateTimetableLectures(
                        timetables.filter { !isUpdatedTimetableLectureOverlapped(it, updatedLecture) },
                        updatedLecture,
                    ),
                    dropOverlappingLectures(
                        timetables.filter { isUpdatedTimetableLectureOverlapped(it, updatedLecture) },
                        updatedLecture,
                    ),
                )
            }

    private fun updateTimetableLectures(
        timetables: Flow<Timetable>,
        updatedLecture: UpdatedLecture,
    ): Flow<TimetableLectureUpdateResult> =
        timetables
            .map { timetable ->
                val updatedTimetableLecture =
                    timetable.lectures.find { it.lectureId == updatedLecture.oldData.id }?.apply {
                        academicYear = updatedLecture.newData.academicYear
                        category = updatedLecture.newData.category
                        classPlaceAndTimes = updatedLecture.newData.classPlaceAndTimes
                        classification = updatedLecture.newData.classification
                        credit = updatedLecture.newData.credit
                        department = updatedLecture.newData.department
                        instructor = updatedLecture.newData.instructor
                        lectureNumber = updatedLecture.newData.lectureNumber
                        quota = updatedLecture.newData.quota
                        freshmanQuota = updatedLecture.newData.freshmanQuota
                        remark = updatedLecture.newData.remark
                        courseNumber = updatedLecture.newData.courseNumber
                        courseTitle = updatedLecture.newData.courseTitle
                        categoryPre2025 = updatedLecture.newData.categoryPre2025
                    }
                timeTableRepository.updateTimetableLecture(timetable.id!!, updatedTimetableLecture!!).also {
                    eventPublisher.publishEvent(TimetableLectureModifiedEvent(updatedTimetableLecture))
                }
            }.map { timetable ->
                TimetableLectureUpdateResult(
                    year = timetable.year,
                    semester = timetable.semester,
                    lectureId = updatedLecture.oldData.id!!,
                    userId = timetable.userId,
                    timetableTitle = timetable.title,
                    timetableId = timetable.id!!,
                    courseTitle = updatedLecture.oldData.courseTitle,
                    updatedFields = updatedLecture.updatedField,
                )
            }

    fun dropOverlappingLectures(
        timetables: Flow<Timetable>,
        updatedLecture: UpdatedLecture,
    ) = timetables.map { timetable ->
        timeTableRepository.pullTimetableLectureByLectureId(timetable.id!!, updatedLecture.oldData.id!!)
        TimetableLectureDeleteByOverlapResult(
            year = timetable.year,
            semester = timetable.semester,
            lectureId = updatedLecture.oldData.id!!,
            userId = timetable.userId,
            timetableTitle = timetable.title,
            courseTitle = updatedLecture.oldData.courseTitle,
        )
    }

    fun isUpdatedTimetableLectureOverlapped(
        timetable: Timetable,
        updatedLecture: UpdatedLecture,
    ) = updatedLecture.updatedField.contains(Lecture::classPlaceAndTimes) &&
        timetable.lectures.any {
            it.lectureId != updatedLecture.oldData.id &&
                ClassTimeUtils.timesOverlap(it.classPlaceAndTimes, updatedLecture.newData.classPlaceAndTimes)
        }

    private fun deleteBookmarkLectures(deletedLecture: Lecture): Flow<BookmarkLectureDeleteResult> =
        bookmarkRepository
            .findAllContainsLectureId(
                deletedLecture.year,
                deletedLecture.semester,
                deletedLecture.id!!,
            ).map { bookmark ->
                bookmarkRepository.pullLecture(bookmark.id!!, deletedLecture.id!!)
                BookmarkLectureDeleteResult(
                    year = bookmark.year,
                    semester = bookmark.semester,
                    courseTitle = deletedLecture.courseTitle,
                    userId = bookmark.userId,
                    lectureId = deletedLecture.id!!,
                )
            }

    private fun deleteTimetableLectures(deletedLecture: Lecture): Flow<TimetableLectureDeleteResult> =
        timeTableRepository
            .findAllContainsLectureId(
                deletedLecture.year,
                deletedLecture.semester,
                deletedLecture.id!!,
            ).map { timetable ->
                timeTableRepository.pullTimetableLectureByLectureId(timetable.id!!, deletedLecture.id!!)
                TimetableLectureDeleteResult(
                    timetable.year,
                    timetable.semester,
                    timetable.title,
                    deletedLecture.courseTitle,
                    timetable.userId,
                    deletedLecture.id!!,
                )
            }

    private suspend fun updateLectureBuildings(compareResult: SugangSnuLectureCompareResult) {
        val updatedPlaceInfos =
            (compareResult.updatedLectureList.map { it.newData } + compareResult.createdLectureList)
                .flatMap { it.classPlaceAndTimes }
                .flatMap { PlaceInfo.getValuesOf(it.place) }
                .filter { it.campus == Campus.GWANAK }
                .distinct()
        lectureBuildingService.updateLectureBuildings(updatedPlaceInfos)
    }

    private suspend fun Coursebook.isSyncedToSugangSnu(sugangSnuCoursebookCondition: SugangSnuCoursebookCondition): Boolean {
        if (this < Coursebook(year = sugangSnuCoursebookCondition.latestYear, semester = sugangSnuCoursebookCondition.latestSemester)) {
            return false
        }
        val nextCoursebook = this.nextCoursebook()
        return sugangSnuFetchService.getPageCount(nextCoursebook.year, nextCoursebook.semester) < THRESHOLD_SEARCH_PAGES
    }
}

data class ParsedTags(
    val classification: Set<String?> = setOf(),
    val department: Set<String?> = setOf(),
    val academicYear: Set<String?> = setOf(),
    val credit: Set<Long> = setOf(),
    val instructor: Set<String?> = setOf(),
    val category: Set<String?> = setOf(),
    val etc: Set<String?> = setOf(),
    val categoryPre2025: Set<String?> = setOf(),
)
