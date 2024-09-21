package com.wafflestudio.snu4t.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    servers = [Server(url = "/", description = "Local")],
    info =
        Info(
            title = "SNUTT timetable api spec",
            version = "1.0.0",
        ),
)
@Configuration
class SwaggerConfig {
    @Bean
    fun openApiCustomizer() =
        OpenApiCustomizer {
            val securitySchemes =
                mapOf(
                    "ApiKey" to
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.HEADER)
                            .name("x-access-apikey"),
                    "AccessToken" to
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.HEADER)
                            .name("x-access-token"),
                )

            securitySchemes.map { (k, v) ->
                it.components
                    .addSecuritySchemes(k, v)
            }

            it.security(
                securitySchemes
                    .map { (k, _) -> SecurityRequirement().addList(k) },
            )
        }
}
