package com.wafflestudio.snutt.theme.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.mongodb.core.mapping.Field

data class ColorSet(
    @Field("bg")
    @param:JsonProperty("bg")
    var backgroundColor: String? = null,
    @Field("fg")
    @param:JsonProperty("fg")
    var foregroundColor: String? = null,
)
