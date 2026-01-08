package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.filter.SnuttNoAuthApiFilterTarget
import com.wafflestudio.snutt.popup.dto.PopupResponse
import com.wafflestudio.snutt.popup.service.PopupService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttNoAuthApiFilterTarget
@RequestMapping(
    "/v1/popups",
    "/popups",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class PopupController(
    private val popupService: PopupService,
) {
    @GetMapping("")
    suspend fun getPopups(
        @RequestAttribute("clientInfo") clientInfo: ClientInfo,
    ): ListResponse<PopupResponse> {
        val popups = popupService.getPopups(clientInfo)
        return ListResponse(
            content = popups.map(::PopupResponse),
            totalCount = popups.size,
        )
    }
}
