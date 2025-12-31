package com.wafflestudio.snutt

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication
class SnuttBatchApplication

val log: Logger = LoggerFactory.getLogger(SnuttBatchApplication::class.java)

fun main(args: Array<String>) {
    val applicationContext = runApplication<SnuttBatchApplication>(*args)
    Thread.sleep(30000)
    exitProcess(SpringApplication.exit(applicationContext))
}
