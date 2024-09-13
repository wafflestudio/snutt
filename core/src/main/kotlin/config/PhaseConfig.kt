package com.wafflestudio.snu4t.config

import jakarta.annotation.PostConstruct
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class PhaseUtils(
    private val environment: Environment,
) {
    companion object {
        private lateinit var env: Environment

        fun getPhase(): Phase {
            val profiles = env.activeProfiles.toList()
            return when {
                profiles.contains("dev") -> Phase.DEV
                profiles.contains("prod") -> Phase.PROD
                profiles.contains("local") -> Phase.LOCAL
                profiles.contains("test") -> Phase.TEST
                else -> throw IllegalArgumentException("Invalid profile")
            }
        }
    }

    @PostConstruct
    fun init() {
        env = environment
    }
}

enum class Phase {
    DEV,
    PROD,
    LOCAL,
    TEST,
    ;

    val isProd: Boolean
        get() = this == PROD
}
