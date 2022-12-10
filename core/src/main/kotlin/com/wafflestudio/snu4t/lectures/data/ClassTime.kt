package com.wafflestudio.snu4t.lectures.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field

class ClassTime(
        @Id
        var id: String,
        var day: Double,
        var place: String,
        @Field("start_time")
        var startTime: String,
        @Field("end_time")
        var endTime: String,
        @Field("len")
        var length: Double,
        @Field("start")
        var startPeriod: Double,
)
