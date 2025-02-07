package com.wafflestudio.snutt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnuttApplication

fun main(args: Array<String>) {
    runApplication<SnuttApplication>(*args)
}
