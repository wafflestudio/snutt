package com.wafflestudio.snutt.sugangsnu.job.sync.data

import com.wafflestudio.snutt.lectures.data.Lecture

class SugangSnuLectureCompareResult(
    val createdLectureList: List<Lecture>,
    val deletedLectureList: List<Lecture>,
    val updatedLectureList: List<UpdatedLecture>,
)
