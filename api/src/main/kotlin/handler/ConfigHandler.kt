package com.wafflestudio.snu4t.handler

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snu4t.clientconfig.service.ClientConfigService
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class ConfigHandler(
    private val configService: ClientConfigService,
    private val objectMapper: ObjectMapper,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun getConfigs(req: ServerRequest) = handle<Map<String, JsonNode>>(req) {
        val clientInfo = req.clientInfo!!

        configService.getConfigs(clientInfo).associate {
            it.name to objectMapper.readTree(it.value)
        }
    }
}
