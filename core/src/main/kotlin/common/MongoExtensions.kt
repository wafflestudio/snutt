package com.wafflestudio.snu4t.common

import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import kotlin.reflect.KProperty

infix fun Criteria.isEqualTo(o: Any?): Criteria = this.`is`(o)
infix fun <T> KProperty<T>.isEqualTo(value: T) = Criteria(this.toDotPath()).isEqualTo(value)
infix fun Criteria.elemMatch(c: Criteria): Criteria = this.elemMatch(c)
infix fun Criteria.all(c: Criteria): Criteria = this.all(c)

fun Query.addInWhereIfNotEmpty(field: String, values: List<*>?) {
    if (!values.isNullOrEmpty()) {
        addCriteria(Criteria.where(field).`in`(values))
    }
}
