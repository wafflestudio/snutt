package com.wafflestudio.snu4t.sugangsnu.data

import com.wafflestudio.snu4t.lectures.data.Lecture

class SugangSnuLectureCompareResult(
    val created: List<Lecture>,
    val deleted: List<Lecture>,
    val updated: List<UpdatedLecture>,
)
