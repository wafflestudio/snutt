package com.wafflestudio.snutt.sugangsnu.common.data

data class RegistrationStatus(
    val courseNumber: String,
    val lectureNumber: String,
    val registrationCount: Int,
    val quota: Int,
    val freshmanQuota: Int?,
    val wasFull: Boolean,
)
