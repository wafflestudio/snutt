package com.wafflestudio.snu4t.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class PhaseConfig(
    private val environment: Environment
) {
    @Bean
    fun phase(): Phase {
        val profiles = environment.activeProfiles.toList()
        return when {
            profiles.contains("dev") -> Phase.DEV
            profiles.contains("prod") -> Phase.PROD
            profiles.contains("local") -> Phase.LOCAL
            profiles.contains("test") -> Phase.TEST
            else -> throw IllegalArgumentException("Invalid profile")
        }
    }
}

enum class Phase {
    DEV,
    PROD,
    LOCAL,
    TEST
    ;
}
