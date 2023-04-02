package com.wafflestudio.snu4t.common.dynamiclink.client

import com.wafflestudio.snu4t.common.dynamiclink.api.FirebaseDynamicLinkApi
import com.wafflestudio.snu4t.common.dynamiclink.dto.AndroidInfo
import com.wafflestudio.snu4t.common.dynamiclink.dto.DynamicLinkInfo
import com.wafflestudio.snu4t.common.dynamiclink.dto.DynamicLinkRequest
import com.wafflestudio.snu4t.common.dynamiclink.dto.DynamicLinkResponse
import com.wafflestudio.snu4t.common.dynamiclink.dto.IosInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.createExceptionAndAwait
import java.net.URLEncoder

interface DynamicLinkClient {
    suspend fun generateLink(dynamicLinkRequest: DynamicLinkRequest): DynamicLinkResponse
    suspend fun generateLink(link: String, mobileCtaLink: String): DynamicLinkResponse
}

@Service
@Profile("!test")
class FirebaseDynamicLinkClient(
    val firebaseDynamicLinkApi: FirebaseDynamicLinkApi,
    @Value("\${google.firebase.api-key}") val apiKey: String,
    @Value("\${google.firebase.dynamic-link.domain-uri-prefix}") val domainUriPrefix: String,
    @Value("\${google.firebase.dynamic-link.android.package-name}") val androidPackageName: String,
    @Value("\${google.firebase.dynamic-link.ios.bundle-id}") val iosBundleId: String,
    @Value("\${google.firebase.dynamic-link.ios.app-store-id:#{null}}") val iosAppStoreId: String?,
) : DynamicLinkClient {

    companion object {
        const val SHORT_LINK_PATH = "shortLinks"
    }

    override suspend fun generateLink(dynamicLinkRequest: DynamicLinkRequest): DynamicLinkResponse {
        val response = firebaseDynamicLinkApi
            .post()
            .uri { builder ->
                builder
                    .path(SHORT_LINK_PATH)
                    .queryParam("key", apiKey)
                    .build()
            }
            .bodyValue(dynamicLinkRequest)
            .awaitExchange {
                if (it.statusCode().is2xxSuccessful) {
                    it.awaitBody(DynamicLinkResponse::class)
                } else {
                    // FIXME: 로깅
                    throw it.createExceptionAndAwait()
                }
            }

        return response
    }

    override suspend fun generateLink(link: String, mobileCtaLink: String): DynamicLinkResponse {
        val dynamicLinkRequest = DynamicLinkRequest(
            DynamicLinkInfo(
                domainUriPrefix = domainUriPrefix,
                link = DynamicLinkSimplePayload(link, mobileCtaLink).fullLink,
                androidInfo = AndroidInfo(
                    androidPackageName = androidPackageName
                ),
                iosInfo = IosInfo(
                    iosBundleId = iosBundleId,
                    iosAppStoreId = iosAppStoreId
                ),
            )
        )
        return generateLink(dynamicLinkRequest)
    }
}

data class DynamicLinkSimplePayload(
    val link: String,
    val mobileCtaLink: String,
) {
    val fullLink: String
        get() = "$link?mobileCtaLink=${URLEncoder.encode(this.mobileCtaLink, "UTF-8")}"
}
