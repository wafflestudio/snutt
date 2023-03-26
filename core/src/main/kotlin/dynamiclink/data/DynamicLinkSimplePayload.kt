package com.wafflestudio.snu4t.dynamiclink.data

import java.net.URLEncoder

data class DynamicLinkSimplePayload(
    val link: String,
    val mobileCtaLink: String,
) {
    val fullLink: String
        get() = "$link?mobileCtaLink=${URLEncoder.encode(this.mobileCtaLink, "UTF-8")}"
}
