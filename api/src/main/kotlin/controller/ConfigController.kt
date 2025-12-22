package com.wafflestudio.snutt.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snutt.clientconfig.service.ClientConfigService
import com.wafflestudio.snutt.common.client.ClientInfo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/configs", "/configs")
class ConfigController(
    private val configService: ClientConfigService,
    private val objectMapper: ObjectMapper,
) {
    @GetMapping("")
    suspend fun getConfigs(
        @RequestAttribute("clientInfo") clientInfo: ClientInfo,
    ): Map<String, JsonNode> =
        configService.getConfigs(clientInfo).associate {
            it.name to objectMapper.readTree(it.value)
        }
}
