package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class FacebookLoginRequest(
    @param:JsonProperty("fb_id")
    val fbId: String?,
    @param:JsonProperty("fb_token")
    val fbToken: String,
)
