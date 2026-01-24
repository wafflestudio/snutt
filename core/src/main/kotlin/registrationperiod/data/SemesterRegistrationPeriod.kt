package com.wafflestudio.snutt.registrationperiod.data

import com.wafflestudio.snutt.common.enums.Semester
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDate

@Document
@CompoundIndex(def = "{ 'year': 1, 'semester': 1 }")
data class SemesterRegistrationPeriod(
    @Id
    val id: String?,
    val year: Int,
    val semester: Semester,
    val registrationPeriods: List<RegistrationDate>,
)

data class RegistrationDate(
    val date: LocalDate,
    val vacantSeatRegistrationTimes: List<RegistrationTimeSlot>,
    @Field(targetType = FieldType.STRING)
    val phase: RegistrationPhase,
)

data class RegistrationTimeSlot(
    val startMinute: Int,
    val endMinute: Int,
)
