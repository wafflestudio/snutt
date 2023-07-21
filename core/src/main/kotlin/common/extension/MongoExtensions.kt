package com.wafflestudio.snu4t.common.extension

import org.springframework.data.domain.Sort
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import kotlin.reflect.KProperty

infix fun Criteria.isEqualTo(o: Any?): Criteria = this.`is`(o)
infix fun <T> KProperty<T>.isEqualTo(value: T) = Criteria(this.toDotPath()).isEqualTo(value)
infix fun Criteria.elemMatch(c: Criteria): Criteria = this.elemMatch(c)
infix fun Criteria.all(c: Criteria): Criteria = this.all(c)
infix fun <T : Any> Criteria.gt(value: T): Criteria = this.gt(value)
fun <T> KProperty<T>.desc() = Sort.by(Sort.Order.desc(this.toDotPath()))
fun <T> KProperty<T>.asc() = Sort.by(Sort.Order.asc(this.toDotPath()))
infix fun Criteria.regex(re: Regex): Criteria = this.regex(re)
infix fun Criteria.regex(re: String): Criteria = this.regex(re)

fun Query.addInWhereIfNotEmpty(field: String, values: List<*>?) {
    if (!values.isNullOrEmpty()) {
        addCriteria(Criteria.where(field).`in`(values))
    }
}
