package com.wafflestudio.snu4t.timetables.dto

import com.wafflestudio.snu4t.lectures.dto.ClassPlaceAndTimeCamelDto
import com.wafflestudio.snu4t.lectures.utils.ClassTimeUtils
import com.wafflestudio.snu4t.timetables.data.ColorSet
import com.wafflestudio.snu4t.timetables.data.TimetableLecture

data class TimetableLectureDto(
    var id: String? = null,
    var academicYear: String?,
    var category: String?,
    var classPlaceAndTimes: List<ClassPlaceAndTimeCamelDto>,
    var classification: String?,
    var credit: Long?,
    var department: String?,
    var instructor: String?,
    var lectureNumber: String?,
    var quota: Int?,
    var freshmanQuota: Int?,
    var remark: String?,
    var courseNumber: String?,
    var courseTitle: String,
    var color: ColorSet = ColorSet(),
    var colorIndex: Int = 0,
    var lectureId: String? = null,

    // FIXME: 안드로이드 구버전 대응용 필드 1년 후 2024년에 삭제 (2023/06/26)
    val classTimeMask: List<Int> = emptyList(),
)

fun TimetableLectureDto(timetableLecture: TimetableLecture) = TimetableLectureDto(
    id = timetableLecture.id,
    academicYear = timetableLecture.academicYear,
    category = timetableLecture.category,
    classPlaceAndTimes = timetableLecture.classPlaceAndTimes.map { ClassPlaceAndTimeCamelDto(it) },
    classification = timetableLecture.classification,
    credit = timetableLecture.credit,
    department = timetableLecture.department,
    instructor = timetableLecture.instructor,
    lectureNumber = timetableLecture.lectureNumber,
    quota = timetableLecture.quota,
    freshmanQuota = timetableLecture.freshmanQuota,
    remark = timetableLecture.remark,
    courseNumber = timetableLecture.courseNumber,
    courseTitle = timetableLecture.courseTitle,
    color = timetableLecture.color,
    colorIndex = timetableLecture.colorIndex,
    lectureId = timetableLecture.lectureId,
    classTimeMask = ClassTimeUtils.classTimeToBitmask(timetableLecture.classPlaceAndTimes),
)
