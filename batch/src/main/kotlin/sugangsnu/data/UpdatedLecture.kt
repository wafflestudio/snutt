package com.wafflestudio.snu4t.sugangsnu.data

import com.wafflestudio.snu4t.lectures.data.Lecture
import kotlin.reflect.KProperty1

// Map<DifferenceCandidate, Pair<Any, Any>> 로 만들까..
class UpdatedLecture(
    val oldData: Lecture,
    val newData: Lecture,
    val updatedField: List<KProperty1<Lecture, *>>
)
