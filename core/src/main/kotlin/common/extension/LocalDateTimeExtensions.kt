package com.wafflestudio.snu4t.common.extension

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun LocalDateTime.toZonedDateTime(zonedId: ZoneId = ZoneId.of("UTC")): ZonedDateTime {
    return ZonedDateTime.of(this, zonedId)
}
