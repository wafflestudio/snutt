package com.wafflestudio.snu4t.lecturebuildings.data

import com.wafflestudio.snu4t.lectures.data.Lecture

class LectureBuildingUpdateResult(
    val lecturesWithBuildingInfos: List<Lecture>,
    val lecturesWithOutBuildingInfos: List<Lecture>,
    val lecturesFailed: List<Lecture>
)
