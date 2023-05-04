package com.wafflestudio.snu4t

import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Snu4tBatchApplication

fun main(args: Array<String>) {
    val application = SpringApplication(Snu4tBatchApplication::class.java)
    application.webApplicationType = WebApplicationType.NONE
    application.run(*args)
}
