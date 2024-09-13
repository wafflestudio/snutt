package com.wafflestudio.snu4t.theme.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.mongodb.core.mapping.Field

data class ColorSet(
    @Field("bg")
    @JsonProperty("bg")
    var backgroundColor: String? = null,
    @Field("fg")
    @JsonProperty("fg")
    var foregroundColor: String? = null,
)
