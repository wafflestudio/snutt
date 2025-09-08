package com.wafflestudio.snutt.common.extension

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun LocalDateTime.toZonedDateTime(zonedId: ZoneId = ZoneId.of("UTC")): ZonedDateTime = ZonedDateTime.of(this, zonedId)
