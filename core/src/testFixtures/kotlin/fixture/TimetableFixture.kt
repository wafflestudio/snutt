package com.wafflestudio.snu4t.fixture

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.enum.TimetableTheme
import com.wafflestudio.snu4t.timetables.data.Timetable
import org.springframework.stereotype.Component

@Component
class TimetableFixture(val userFixture: UserFixture) {
    fun getTimetable(title: String): Timetable {
        val userId = userFixture.testUser.id!!
        return Timetable(
            userId = userId,
            year = 2023,
            semester = Semester.AUTUMN,
            title = title,
            theme = TimetableTheme.SNUTT,
        )
    }
}
