package com.wafflestudio.snu4t

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication
class Snu4tBatchApplication

val log: Logger = LoggerFactory.getLogger(Snu4tBatchApplication::class.java)
fun main(args: Array<String>) {
    runCatching {
        val applicationContext = runApplication<Snu4tBatchApplication>(*args)
        Thread.sleep(30000)
        exitProcess(SpringApplication.exit(applicationContext))
    }.onFailure { exception -> log.error("배치 실패", exception) }
}
