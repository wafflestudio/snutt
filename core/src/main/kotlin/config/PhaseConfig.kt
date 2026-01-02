package com.wafflestudio.snutt.config

import jakarta.annotation.PostConstruct
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class PhaseUtils(
    private val environment: Environment,
) {
    companion object {
        private lateinit var env: Environment

        fun getPhase(): Phase =
            when {
                env.matchesProfiles("dev") -> Phase.DEV
                env.matchesProfiles("prod") -> Phase.PROD
                env.matchesProfiles("local") -> Phase.LOCAL
                env.matchesProfiles("test") -> Phase.TEST
                else -> throw IllegalArgumentException("Invalid profile")
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
