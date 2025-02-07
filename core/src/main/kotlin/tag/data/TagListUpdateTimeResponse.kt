package com.wafflestudio.snutt.tag.data

import com.fasterxml.jackson.annotation.JsonProperty

data class TagListUpdateTimeResponse(
    @JsonProperty("updated_at")
    val updatedAt: Long,
)
