package com.wafflestudio.snutt.fixture

import com.wafflestudio.snutt.common.enums.BasicThemeType
import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.timetables.data.Timetable
import org.springframework.stereotype.Component

@Component
class TimetableFixture(
    val userFixture: UserFixture,
) {
    fun getTimetable(title: String): Timetable {
        val userId = userFixture.testUser.id!!
        return Timetable(
            userId = userId,
            year = 2023,
            semester = Semester.AUTUMN,
            title = title,
            theme = BasicThemeType.SNUTT,
            themeId = null,
        )
    }
}
