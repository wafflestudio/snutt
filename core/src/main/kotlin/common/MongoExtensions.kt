package com.wafflestudio.snu4t.common

import org.springframework.data.mongodb.core.query.Criteria

infix fun Criteria.isEqualTo(o: Any?): Criteria = this.`is`(o)
infix fun Criteria.elemMatch(c: Criteria): Criteria = this.elemMatch(c)
