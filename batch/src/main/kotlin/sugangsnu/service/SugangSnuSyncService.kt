package com.wafflestudio.snu4t.sugangsnu.service

import com.wafflestudio.snu4t.bookmark.repository.BookmarkRepository
import com.wafflestudio.snu4t.common.cache.CacheRepository
import com.wafflestudio.snu4t.coursebook.data.Coursebook
import com.wafflestudio.snu4t.coursebook.repository.CoursebookRepository
import com.wafflestudio.snu4t.lectures.data.BookmarkLecture
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.service.LectureService
import com.wafflestudio.snu4t.sugangsnu.SugangSnuRepository
import com.wafflestudio.snu4t.sugangsnu.data.BookmarkLectureDeleteResult
import com.wafflestudio.snu4t.sugangsnu.data.BookmarkLectureUpdateResult
import com.wafflestudio.snu4t.sugangsnu.data.SugangSnuCoursebookCondition
import com.wafflestudio.snu4t.sugangsnu.data.SugangSnuLectureCompareResult
import com.wafflestudio.snu4t.sugangsnu.data.TimetableLectureDeleteResult
import com.wafflestudio.snu4t.sugangsnu.data.TimetableLectureUpdateResult
import com.wafflestudio.snu4t.sugangsnu.data.UpdatedLecture
import com.wafflestudio.snu4t.sugangsnu.data.UserLectureSyncResult
import com.wafflestudio.snu4t.tag.data.TagCollection
import com.wafflestudio.snu4t.tag.data.TagList
import com.wafflestudio.snu4t.tag.repository.TagListRepository
import com.wafflestudio.snu4t.timetables.data.TimetableLecture
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.reflect.full.memberProperties

interface SugangSnuSyncService {
    suspend fun updateCoursebook(coursebook: Coursebook)
    suspend fun addCoursebook(coursebook: Coursebook)
    suspend fun isSyncWithSugangSnu(latestCoursebook: Coursebook): Boolean
    suspend fun flushCache()
}

@Service
class SugangSnuSyncServiceImpl(
    private val sugangSnuFetchService: SugangSnuFetchService,
    private val sugangSnuNotificationService: SugangSnuNotificationService,
    private val lectureService: LectureService,
    private val timeTableRepository: TimetableRepository,
    private val sugangSnuRepository: SugangSnuRepository,
    private val coursebookRepository: CoursebookRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val tagListRepository: TagListRepository,
    private val cacheRepository: CacheRepository,
) : SugangSnuSyncService {
    override suspend fun updateCoursebook(coursebook: Coursebook) {
        val newLectures = sugangSnuFetchService.getSugangSnuLectures(coursebook.year, coursebook.semester)
        val oldLectures =
            lectureService.getLecturesByYearAndSemesterAsFlow(coursebook.year, coursebook.semester).toList()
        val compareResult = compareLectures(newLectures, oldLectures)

        syncLectures(compareResult)
        val syncUserLecturesResults = syncSavedUserLectures(compareResult)
        syncTagList(coursebook, newLectures)

        coursebookRepository.save(coursebook.apply { updatedAt = Instant.now() })
        sugangSnuNotificationService.notifyUserLectureChanges(syncUserLecturesResults)
    }

    override suspend fun addCoursebook(coursebook: Coursebook) {
        val newLectures = sugangSnuFetchService.getSugangSnuLectures(coursebook.year, coursebook.semester)
        lectureService.upsertLectures(newLectures)
        syncTagList(coursebook, newLectures)

        coursebookRepository.save(coursebook)
        sugangSnuNotificationService.notifyCoursebookUpdate(coursebook)
    }

    override suspend fun isSyncWithSugangSnu(latestCoursebook: Coursebook): Boolean {
        val sugangSnuLatestCoursebook = sugangSnuRepository.getCoursebookCondition()
        return latestCoursebook.isSyncedToSugangSnu(sugangSnuLatestCoursebook)
    }

    override suspend fun flushCache() = cacheRepository.flushDatabase()

    private fun compareLectures(
        newLectures: Iterable<Lecture>,
        oldLectures: Iterable<Lecture>
    ): SugangSnuLectureCompareResult {
        val newMap = newLectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }
        val oldMap = oldLectures.associateBy { lecture -> lecture.courseNumber + "##" + lecture.lectureNumber }

        val created = (newMap.keys - oldMap.keys).map(newMap::getValue)
        val updated = (newMap.keys intersect oldMap.keys)
            .map { oldMap[it]!! to newMap[it]!! }
            .filter { (old, new) -> old != new }
            .map { (old, new) ->
                UpdatedLecture(
                    old, new,
                    Lecture::class.memberProperties.filter {
                        it != Lecture::id && it.get(old) != it.get(new)
                    }
                )
            }
        val deleted = (oldMap.keys - newMap.keys).map(
            oldMap::getValue
        )

        return SugangSnuLectureCompareResult(created, deleted, updated)
    }

    private suspend fun syncTagList(coursebook: Coursebook, lectures: Iterable<Lecture>) {
        val tagCollection = lectures.fold(ParsedTags()) { acc, lecture ->
            ParsedTags(
                academicYear = acc.academicYear + lecture.academicYear,
                classification = acc.classification + lecture.classification,
                department = acc.department + lecture.department,
                credit = acc.credit + lecture.credit,
                instructor = acc.instructor + lecture.instructor,
                category = acc.category + lecture.category,
            )
        }.let { parsedTag ->
            TagCollection(
                // 엑셀 academicYear 필드 '학년' 안 붙어 나오는 경우 제외
                academicYear = parsedTag.academicYear.filterNotNull().filter { it.length > 1 }.sorted(),
                classification = parsedTag.classification.filterNotNull().filter { it.isNotBlank() }.sorted(),
                department = parsedTag.department.filterNotNull().filter { it.isNotBlank() }.sorted(),
                credit = parsedTag.credit.sorted().map { "${it}학점" },
                instructor = parsedTag.instructor.filterNotNull().filter { it.isNotBlank() }.sorted(),
                category = parsedTag.category.filterNotNull().filter { it.isNotBlank() }.sorted(),
            )
        }
        val tagList = tagListRepository.findByYearAndSemester(coursebook.year, coursebook.semester)
            ?.copy(tagCollection = tagCollection, updatedAt = Instant.now()) ?: TagList(
            year = coursebook.year,
            semester = coursebook.semester,
            tagCollection = tagCollection
        )
        tagListRepository.save(tagList)
    }

    private suspend fun syncLectures(compareResult: SugangSnuLectureCompareResult) {
        val updatedLectures = compareResult.updatedLectureList.map { diff ->
            diff.newData.apply { id = diff.oldData.id }
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
            compareResult.updatedLectureList.map { updateTimetableLectures(it) }.merge(),
            compareResult.deletedLectureList.map { deleteTimetableLectures(it) }.merge(),
        )

    private fun syncBookmarks(compareResult: SugangSnuLectureCompareResult) =
        merge(
            compareResult.updatedLectureList.map { updateBookmarkLectures(it) }.merge(),
            compareResult.deletedLectureList.map { deleteBookmarkLectures(it) }.merge(),
        )

    private fun updateBookmarkLectures(updatedLecture: UpdatedLecture) =
        bookmarkRepository.findAllContainsLectureId(
            updatedLecture.oldData.year,
            updatedLecture.oldData.semester,
            updatedLecture.oldData.id!!
        ).map { bookmark ->
            bookmark.apply {
                lectures = lectures.map { lecture ->
                    if (lecture.id == updatedLecture.newData.id) BookmarkLecture(updatedLecture.newData) else lecture
                }
            }
        }.let {
            bookmarkRepository.saveAll(it)
        }.map { bookmark ->
            BookmarkLectureUpdateResult(
                bookmark.year,
                bookmark.semester,
                updatedLecture.oldData.courseTitle,
                bookmark.userId,
                updatedLecture.oldData.id!!,
                updatedLecture.updatedField
            )
        }

    private fun updateTimetableLectures(updatedLecture: UpdatedLecture): Flow<TimetableLectureUpdateResult> =
        timeTableRepository.findAllContainsLectureId(
            updatedLecture.oldData.year,
            updatedLecture.oldData.semester,
            updatedLecture.oldData.id!!
        ).map { timetable ->
            timetable.apply {
                lectures = lectures.map { lecture ->
                    if (lecture.lectureId == updatedLecture.newData.id) lecture.apply {
                        academicYear = updatedLecture.newData.academicYear
                        category = updatedLecture.newData.category
                        periodText = updatedLecture.newData.periodText
                        classTimeText = updatedLecture.newData.classTimeText
                        classTime = updatedLecture.newData.classTime
                        classTimeMask = updatedLecture.newData.classTimeMask
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
                    } else lecture
                }
                updatedAt = Instant.now()
            }
        }.let {
            timeTableRepository.saveAll(it)
        }.map { timetable ->
            TimetableLectureUpdateResult(
                year = timetable.year,
                semester = timetable.semester,
                lectureId = updatedLecture.oldData.id!!,
                userId = timetable.userId,
                timetableTitle = timetable.title,
                courseTitle = updatedLecture.oldData.courseTitle,
                updatedFields = updatedLecture.updatedField
            )
        }

    private fun deleteBookmarkLectures(deletedLecture: Lecture): Flow<BookmarkLectureDeleteResult> =
        bookmarkRepository.findAllContainsLectureId(
            deletedLecture.year, deletedLecture.semester, deletedLecture.id!!
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
        timeTableRepository.findAllContainsLectureId(
            deletedLecture.year, deletedLecture.semester, deletedLecture.id!!
        ).map { timetable ->
            timeTableRepository.pullLecture(timetable.id!!, deletedLecture.id!!)
            TimetableLectureDeleteResult(
                timetable.year,
                timetable.semester,
                timetable.title,
                deletedLecture.courseTitle,
                timetable.userId,
                deletedLecture.id!!,
            )
        }

    private fun Coursebook.isSyncedToSugangSnu(sugangSnuCoursebookCondition: SugangSnuCoursebookCondition): Boolean =
        this.year == sugangSnuCoursebookCondition.latestYear && this.semester == sugangSnuCoursebookCondition.latestSemester
}

data class ParsedTags(
    val classification: Set<String?> = setOf(),
    val department: Set<String?> = setOf(),
    val academicYear: Set<String?> = setOf(),
    val credit: Set<Long> = setOf(),
    val instructor: Set<String?> = setOf(),
    val category: Set<String?> = setOf(),
    val etc: Set<String?> = setOf(),
)
