package com.wafflestudio.snu4t.popup.dto

import com.wafflestudio.snu4t.common.storage.toGetUri
import com.wafflestudio.snu4t.popup.data.Popup

data class PopupResponse(
    val id: String,
    val key: String,
    val imageUri: String,
    @Deprecated("Use imageUri instead", replaceWith = ReplaceWith("imageUri"))
    val image_url: String,
    val hiddenDays: Int?, // null 이면 '당분간 보지 않기' 눌러도 매번 노출
    @Deprecated("Use hiddenDays instead", replaceWith = ReplaceWith("hiddenDays"))
    val hidden_days: Int?,
)

fun PopupResponse(popup: Popup): PopupResponse = PopupResponse(
    id = popup.id!!,
    key = popup.key,
    imageUri = popup.imageOriginUri.toGetUri(),
    image_url = popup.imageOriginUri.toGetUri(),
    hiddenDays = popup.hiddenDays,
    hidden_days = popup.hiddenDays,
)
