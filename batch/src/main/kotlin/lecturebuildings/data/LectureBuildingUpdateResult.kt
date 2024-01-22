package com.wafflestudio.snu4t.lecturebuildings.data

import com.wafflestudio.snu4t.lectures.data.Lecture

class LectureBuildingUpdateResult(
    val lecture: Lecture,
    val buildingsAdded: List<LectureBuilding>
)
