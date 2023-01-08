package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.LegacyServer
import com.wafflestudio.snu4t.Migration
import com.wafflestudio.snu4t.TimetableServer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.client.awaitBody

@Migration
class TimeTableMigrations @Autowired constructor(
    private val legacy: LegacyServer,
    private val timetable: TimetableServer,
) {

    @Test
    fun tables(): Unit = runBlocking {
        val expect = legacy.get()
            .uri("/v1/tables")
            .retrieve()
            .awaitBody<String>()

        println(expect)

        timetable.get()
            .uri("/v1/tables")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json(expect)
    }
}
