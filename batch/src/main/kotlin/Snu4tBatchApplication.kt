package com.wafflestudio.snu4t

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableBatchProcessing
class Snu4tBatchApplication

fun main(args: Array<String>) {
    runApplication<Snu4tBatchApplication>(*args)
}
