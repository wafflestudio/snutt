package com.wafflestudio.snu4t.lectures.data

import com.wafflestudio.snu4t.common.enum.DayOfWeek
import com.wafflestudio.snu4t.lecturebuildings.data.LectureBuilding

data class ClassPlaceAndTime(
    val day: DayOfWeek,
    val place: String,
    val startMinute: Int,
    val endMinute: Int,
    var lectureBuilding: LectureBuilding?
)
