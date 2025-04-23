package com.wafflestudio.snutt.notification.dto

import com.wafflestudio.snutt.notification.data.PushPreference
import com.wafflestudio.snutt.notification.data.PushPreferenceItem

data class PushPreferenceDto(
    val pushPreferences: List<PushPreferenceItem>,
)

fun PushPreferenceDto(pushPreference: PushPreference) = PushPreferenceDto(pushPreference.pushPreferences.toList())
