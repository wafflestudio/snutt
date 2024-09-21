package com.wafflestudio.snu4t.mock.dynamiclink

import com.wafflestudio.snu4t.common.dynamiclink.client.DynamicLinkClient
import com.wafflestudio.snu4t.common.dynamiclink.dto.DynamicLinkRequest
import com.wafflestudio.snu4t.common.dynamiclink.dto.DynamicLinkResponse
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class MockDynamicLinkClient : DynamicLinkClient {
    override suspend fun generateLink(dynamicLinkRequest: DynamicLinkRequest): DynamicLinkResponse {
        return DynamicLinkResponse(
            "https://snuttdev.page.link/oySD",
            "https://snuttdev.page.link/oySD?d=1",
        )
    }

    override suspend fun generateLink(
        link: String,
        mobileCtaLink: String,
    ): DynamicLinkResponse {
        return DynamicLinkResponse(
            "https://snuttdev.page.link/oySD",
            "https://snuttdev.page.link/oySD?d=1",
        )
    }
}
