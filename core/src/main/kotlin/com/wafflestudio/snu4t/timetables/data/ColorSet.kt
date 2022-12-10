package com.wafflestudio.snu4t.timetables.data

import org.springframework.data.mongodb.core.mapping.Field

class ColorSet(
        @Field("bg")
        var backgroundColor: String,
        @Field("fg")
        var foregroundColor: String,
)