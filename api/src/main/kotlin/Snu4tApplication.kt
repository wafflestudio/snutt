package com.wafflestudio.snu4t

import com.wafflestudio.snu4t.filter.AuthenticationFilter
import com.wafflestudio.snu4t.filter.DeviceFilter
import com.wafflestudio.snu4t.handler.TimeTableHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

fun main(args: Array<String>) {
    runApplication<Snu4tApplication>(*args)
}

@SpringBootApplication
class Snu4tApplication(
    private val timeTableHandler: TimeTableHandler,
    private val authFilter: AuthenticationFilter,
    private val deviceFilter: DeviceFilter,
) {
    @Bean
    fun pingpong(): RouterFunction<ServerResponse> = coRouter {
        GET("/ping") { ServerResponse.ok().bodyValueAndAwait("pong") }
    }

    @Bean
    fun timetables(): RouterFunction<ServerResponse> = coRouter {
        GET("/v1/timetables", timeTableHandler::getTimeTables)
    }
        .filter(authFilter)
}
