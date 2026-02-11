package com.wafflestudio.snutt.config

import com.oracle.bmc.Region
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.auth.okeworkloadidentity.OkeWorkloadIdentityAuthenticationDetailsProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class OciConfig {
    companion object {
        val REGION: Region = Region.AP_CHUNCHEON_1
    }

    @Bean
    @Profile("local", "test")
    fun localAuthProvider(): BasicAuthenticationDetailsProvider = ConfigFileAuthenticationDetailsProvider("DEFAULT")

    @Bean
    @Profile("dev", "prod")
    fun workloadIdentityAuthProvider(): BasicAuthenticationDetailsProvider =
        OkeWorkloadIdentityAuthenticationDetailsProvider.builder().build()
}
