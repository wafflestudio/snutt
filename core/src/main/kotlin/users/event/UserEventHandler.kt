package com.wafflestudio.snu4t.users.event

import com.wafflestudio.snu4t.timetables.service.TimetableService
import com.wafflestudio.snu4t.users.event.data.SignupEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UserEventHandler(
    private val timetableService: TimetableService,
) {
    @EventListener
    suspend fun createDefaultTimetable(event: SignupEvent) {
        timetableService.createDefaultTable(event.userId)
    }
}
