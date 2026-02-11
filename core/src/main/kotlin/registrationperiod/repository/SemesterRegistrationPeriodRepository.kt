package com.wafflestudio.snutt.registrationperiod.repository

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.registrationperiod.data.SemesterRegistrationPeriod
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SemesterRegistrationPeriodRepository : CoroutineCrudRepository<SemesterRegistrationPeriod, String> {
    suspend fun findByYearAndSemester(
        year: Int,
        semester: Semester,
    ): SemesterRegistrationPeriod?

    suspend fun deleteByYearAndSemester(
        year: Int,
        semester: Semester,
    )
}
