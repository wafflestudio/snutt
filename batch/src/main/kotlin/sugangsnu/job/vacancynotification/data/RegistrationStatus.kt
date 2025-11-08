package com.wafflestudio.snutt.sugangsnu.job.vacancynotification.data

data class RegistrationStatus(
    val courseNumber: String,
    val lectureNumber: String,
    val registrationCount: Int,
    val remainingPlace: Boolean,
)
