package com.wafflestudio.snutt.sugangsnu.job.sync.data

import com.wafflestudio.snutt.lectures.data.Lecture
import kotlin.reflect.KProperty1

class UpdatedLecture(
    val oldData: Lecture,
    val newData: Lecture,
    val updatedField: List<KProperty1<Lecture, *>>,
)
