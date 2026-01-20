package com.wafflestudio.snutt.registrationperiod.service

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.registrationperiod.data.SemesterRegistrationPeriod
import com.wafflestudio.snutt.registrationperiod.dto.SemesterRegistrationPeriodRequest
import com.wafflestudio.snutt.registrationperiod.repository.SemesterRegistrationPeriodRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

interface SemesterRegistrationPeriodService {
    suspend fun getAll(): List<SemesterRegistrationPeriod>

    suspend fun getByYearAndSemester(
        year: Int,
        semester: Semester,
    ): SemesterRegistrationPeriod?

    suspend fun upsert(request: SemesterRegistrationPeriodRequest): SemesterRegistrationPeriod

    suspend fun delete(
        year: Int,
        semester: Semester,
    )
}

@Service
class SemesterRegistrationPeriodServiceImpl(
    private val semesterRegistrationPeriodRepository: SemesterRegistrationPeriodRepository,
) : SemesterRegistrationPeriodService {
    override suspend fun getAll(): List<SemesterRegistrationPeriod> = semesterRegistrationPeriodRepository.findAll().toList()

    override suspend fun getByYearAndSemester(
        year: Int,
        semester: Semester,
    ): SemesterRegistrationPeriod? = semesterRegistrationPeriodRepository.findByYearAndSemester(year, semester)

    override suspend fun upsert(request: SemesterRegistrationPeriodRequest): SemesterRegistrationPeriod {
        val existing = semesterRegistrationPeriodRepository.findByYearAndSemester(request.year, request.semester)
        val semesterRegistrationPeriod =
            SemesterRegistrationPeriod(
                id = existing?.id,
                year = request.year,
                semester = request.semester,
                registrationPeriods = request.registrationPeriods,
            )
        return semesterRegistrationPeriodRepository.save(semesterRegistrationPeriod)
    }

    override suspend fun delete(
        year: Int,
        semester: Semester,
    ) {
        semesterRegistrationPeriodRepository.deleteByYearAndSemester(year, semester)
    }
}
