package com.wafflestudio.snu4t

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@AutoConfigureWebTestClient
@SpringBootTest(classes = [Snu4tApplication::class])
@ActiveProfiles("local", "migration")
annotation class Migration
