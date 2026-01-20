package com.wafflestudio.snutt.registrationperiod.dto

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.registrationperiod.data.RegistrationPeriod

data class SemesterRegistrationPeriodRequest(
    val year: Int,
    val semester: Semester,
    val registrationPeriods: List<RegistrationPeriod>,
)
