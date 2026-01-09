package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.clientconfig.service.ClientConfigService
import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.filter.SnuttNoAuthApiFilterTarget
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

@RestController
@SnuttNoAuthApiFilterTarget
@RequestMapping(
    "/v1/configs",
    "/configs",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
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
