package com.wafflestudio.snutt.config

import com.oracle.bmc.Region
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class OciConfig {
    companion object {
        val REGION: Region = Region.AP_CHUNCHEON_1
    }

    @Bean
    fun ociAuthProvider(): BasicAuthenticationDetailsProvider = ConfigFileAuthenticationDetailsProvider("DEFAULT")
}
