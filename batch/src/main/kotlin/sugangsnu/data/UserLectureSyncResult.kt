package com.wafflestudio.snu4t.sugangsnu.data

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.Lecture
import kotlin.reflect.KProperty1

sealed class UserLectureSyncResult(open val userId: String, open val lectureId: String)

data class BookmarkLectureUpdateResult(
    val year: Int,
    val semester: Semester,
    val courseTitle: String,
    override val userId: String,
    override val lectureId: String,
    val updatedFields: List<KProperty1<Lecture, *>>,
) : UserLectureSyncResult(userId, lectureId)

data class BookmarkLectureDeleteResult(
    val year: Int,
    val semester: Semester,
    val courseTitle: String,
    override val userId: String,
    override val lectureId: String,
) : UserLectureSyncResult(userId, lectureId)

data class TimetableLectureUpdateResult(
    val year: Int,
    val semester: Semester,
    val timetableTitle: String,
    val courseTitle: String,
    override val userId: String,
    override val lectureId: String,
    val updatedFields: List<KProperty1<Lecture, *>>,
) : UserLectureSyncResult(userId, lectureId)

data class TimetableLectureDeleteResult(
    val year: Int,
    val semester: Semester,
    val timetableTitle: String,
    val courseTitle: String,
    override val userId: String,
    override val lectureId: String,
) : UserLectureSyncResult(userId, lectureId)
