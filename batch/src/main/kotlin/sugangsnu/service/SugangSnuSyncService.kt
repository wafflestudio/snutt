package com.wafflestudio.snu4t.sugangsnu.service

import com.wafflestudio.snu4t.bookmark.repository.BookmarkRepository
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
import com.wafflestudio.snu4t.sugangsnu.utils.toSugangSnuSearchString
import com.wafflestudio.snu4t.timetables.data.TimeTableLecture
import com.wafflestudio.snu4t.timetables.repository.TimeTableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.reflect.full.memberProperties

interface SugangSnuSyncService {
    suspend fun getLatestCoursebook(): Coursebook
    suspend fun saveCoursebook(coursebook: Coursebook): Coursebook
    suspend fun isSyncWithSugangSnu(latestCoursebook: Coursebook): Boolean
    fun compareLectures(newLectures: Iterable<Lecture>, oldLectures: Iterable<Lecture>): SugangSnuLectureCompareResult

    suspend fun syncLectures(compareResult: SugangSnuLectureCompareResult)
    suspend fun saveLectures(lectures: Iterable<Lecture>)
    suspend fun syncSavedUserLectures(compareResult: SugangSnuLectureCompareResult): List<UserLectureSyncResult>
}

@Service
class SugangSnuSyncServiceImpl(
    private val lectureService: LectureService,
    private val timeTableRepository: TimeTableRepository,
    private val sugangSnuRepository: SugangSnuRepository,
    private val coursebookRepository: CoursebookRepository,
    private val bookmarkRepository: BookmarkRepository,
) : SugangSnuSyncService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val quotaRegex = """(?<quota>\d+)(\s*\((?<quotaForCurrentStudent>\d+)\))?""".toRegex()

    override suspend fun getLatestCoursebook(): Coursebook =
        coursebookRepository.findFirstByOrderByYearDescSemesterDesc()

    override suspend fun saveCoursebook(coursebook: Coursebook): Coursebook = coursebookRepository.save(coursebook)

    override suspend fun isSyncWithSugangSnu(latestCoursebook: Coursebook): Boolean {
        val sugangSnuLatestCoursebook = sugangSnuRepository.getCoursebookCondition()
        return latestCoursebook.isSyncedToSugangSnu(sugangSnuLatestCoursebook)
    }

    override fun compareLectures(
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
                    old,
                    new,
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

    override suspend fun saveLectures(lectures: Iterable<Lecture>) = lectureService.upsertLectures(lectures)

    override suspend fun syncLectures(compareResult: SugangSnuLectureCompareResult) {
        val updatedLectures = compareResult.updatedLectureList.map { diff ->
            diff.newData.apply { id = diff.oldData.id }
        }

        lectureService.upsertLectures(compareResult.createdLectureList)
        lectureService.upsertLectures(updatedLectures)
        lectureService.deleteLectures(compareResult.deletedLectureList)
    }

    override suspend fun syncSavedUserLectures(compareResult: SugangSnuLectureCompareResult): List<UserLectureSyncResult> =
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
                lectures = findAndUpdateBookmarkLectures(bookmark.lectures, updatedLecture.newData)
            }
        }.let {
            bookmarkRepository.saveAll(it)
        }.map { bookmark ->
            BookmarkLectureUpdateResult(
                bookmark.year,
                bookmark.semester,
                updatedLecture.oldData.courseTitle,
                updatedLecture.oldData.id!!,
                bookmark.userId,
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
                lectures = findAndUpdateTimetableLecture(timetable.lectures, updatedLecture.newData)
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
                updatedField = updatedLecture.updatedField
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

    private fun findAndUpdateBookmarkLectures(lectures: List<BookmarkLecture>, newLecture: Lecture) =
        lectures.map { lecture ->
            lecture.apply {
                if (lecture.id == newLecture.id) {
                    academicYear = newLecture.academicYear
                    category = newLecture.category
                    classTime = newLecture.classTime
                    classTimeMask = newLecture.classTimeMask
                    classification = newLecture.classification
                    credit = newLecture.credit
                    department = newLecture.department
                    instructor = newLecture.instructor
                    lectureNumber = newLecture.lectureNumber
                    quota = newLecture.quota
                    remark = newLecture.remark
                    courseNumber = newLecture.courseNumber
                    courseTitle = newLecture.courseTitle
                }
            }
        }

    private fun findAndUpdateTimetableLecture(lectures: List<TimeTableLecture>, newLecture: Lecture) =
        lectures.map { lecture ->
            lecture.apply {
                if (lecture.lectureId == newLecture.id) {
                    academicYear = newLecture.academicYear
                    category = newLecture.category
                    periodText = newLecture.periodText
                    classTimeText = newLecture.classTimeText
                    classTime = newLecture.classTime
                    classTimeMask = newLecture.classTimeMask
                    classification = newLecture.classification
                    credit = newLecture.credit
                    department = newLecture.department
                    instructor = newLecture.instructor
                    lectureNumber = newLecture.lectureNumber
                    quota = newLecture.quota
                    remark = newLecture.remark
                    courseNumber = newLecture.courseNumber
                    courseTitle = newLecture.courseTitle
                }
            }
        }

    private fun Coursebook.isSyncedToSugangSnu(sugangSnuCoursebookCondition: SugangSnuCoursebookCondition): Boolean =
        this.year == sugangSnuCoursebookCondition.latestYear &&
            this.semester.toSugangSnuSearchString() == sugangSnuCoursebookCondition.latestSugangSnuSemester
}
