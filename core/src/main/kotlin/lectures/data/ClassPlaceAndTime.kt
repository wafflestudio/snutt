package com.wafflestudio.snu4t.lectures.data

import com.wafflestudio.snu4t.common.enum.DayOfWeek
import com.wafflestudio.snu4t.lecturebuildings.data.LectureBuilding

data class ClassPlaceAndTime(
    val day: DayOfWeek,
    val place: String,
    val startMinute: Int,
    val endMinute: Int,
    var lectureBuildings: List<LectureBuilding>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return when (other) {
            is ClassPlaceAndTime -> {
                this.day == other.day &&
                    this.place == other.place &&
                    this.startMinute == other.startMinute &&
                    this.endMinute == other.endMinute
            }

            else -> false
        }
    }
}
