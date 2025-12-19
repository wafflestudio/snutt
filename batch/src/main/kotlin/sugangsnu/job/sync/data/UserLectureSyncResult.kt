package com.wafflestudio.snutt.sugangsnu.job.sync.data

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.lectures.data.Lecture
import kotlin.reflect.KProperty1

sealed class UserLectureSyncResult(
    open val userId: String,
    open val lectureId: String,
)

sealed class BookmarkLectureSyncResult(
    override val userId: String,
    override val lectureId: String,
) : UserLectureSyncResult(userId, lectureId)

sealed class TimetableLectureSyncResult(
    override val userId: String,
    override val lectureId: String,
) : UserLectureSyncResult(userId, lectureId)

data class BookmarkLectureUpdateResult(
    val year: Int,
    val semester: Semester,
    val courseTitle: String,
    override val userId: String,
    override val lectureId: String,
    val updatedFields: List<KProperty1<Lecture, *>>,
) : BookmarkLectureSyncResult(userId, lectureId)

data class BookmarkLectureDeleteResult(
    val year: Int,
    val semester: Semester,
    val courseTitle: String,
    override val userId: String,
    override val lectureId: String,
) : BookmarkLectureSyncResult(userId, lectureId)

data class TimetableLectureUpdateResult(
    val year: Int,
    val semester: Semester,
    val timetableTitle: String,
    val timetableId: String,
    val courseTitle: String,
    override val userId: String,
    override val lectureId: String,
    val updatedFields: List<KProperty1<Lecture, *>>,
) : TimetableLectureSyncResult(userId, lectureId)

data class TimetableLectureDeleteResult(
    val year: Int,
    val semester: Semester,
    val timetableTitle: String,
    val courseTitle: String,
    override val userId: String,
    override val lectureId: String,
) : TimetableLectureSyncResult(userId, lectureId)

data class TimetableLectureDeleteByOverlapResult(
    val year: Int,
    val semester: Semester,
    val timetableTitle: String,
    val courseTitle: String,
    override val userId: String,
    override val lectureId: String,
) : TimetableLectureSyncResult(userId, lectureId)
