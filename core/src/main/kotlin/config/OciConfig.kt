package com.wafflestudio.snutt.config

import com.oracle.bmc.Region
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class OciConfig(
    @param:Value("\${oci.auth.type:auto}")
    private val authType: String,
    @param:Value("\${oci.config.profile:DEFAULT}")
    private val configProfile: String,
) {
    companion object {
        val REGION: Region = Region.AP_CHUNCHEON_1
    }

    @Bean
    fun ociAuthProvider(): BasicAuthenticationDetailsProvider =
        when (authType.trim().lowercase()) {
            "auto" ->
                try {
                    instancePrincipalAuth()
                } catch (e: Exception) {
                    log.info(
                        "OCI instance principal auth failed; falling back to config file auth (oci.auth.type=auto): {}",
                        e.message,
                    )
                    configFileAuth()
                }

            "config" -> configFileAuth()
            "instance_principal" -> instancePrincipalAuth()
            else ->
                throw IllegalArgumentException(
                    "Unsupported oci.auth.type='${authType.trim()}'. Supported: auto, config, instance_principal",
                )
        }

    private val log = LoggerFactory.getLogger(javaClass)

    private fun instancePrincipalAuth(): BasicAuthenticationDetailsProvider =
        InstancePrincipalsAuthenticationDetailsProvider.builder().build()

    private fun configFileAuth(): BasicAuthenticationDetailsProvider {
        val profile = configProfile.trim().ifEmpty { "DEFAULT" }
        return ConfigFileAuthenticationDetailsProvider(profile)
    }
}
