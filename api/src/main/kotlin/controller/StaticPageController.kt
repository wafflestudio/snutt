package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import org.springframework.core.io.ResourceLoader
import org.springframework.http.CacheControl
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.time.Duration

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping("")
class StaticPageController(
    private val resourceLoader: ResourceLoader,
) {
    companion object {
        private const val RESOURCE_PATH = "/views"
        private val staticResponse =
            ServerResponse
                .ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                .header("Content-Type", "text/html; charset=utf-8")
    }

    @GetMapping("/member", produces = ["text/html; charset=utf-8"])
    suspend fun member(): ServerResponse =
        staticResponse.bodyValueAndAwait(
            resourceLoader.getResource("$RESOURCE_PATH/member.html"),
        )

    @GetMapping("/privacy_policy", produces = ["text/html; charset=utf-8"])
    suspend fun privacyPolicy(): ServerResponse =
        staticResponse.bodyValueAndAwait(
            resourceLoader.getResource("$RESOURCE_PATH/privacy_policy.html"),
        )

    @GetMapping("/terms_of_service", produces = ["text/html; charset=utf-8"])
    suspend fun termsOfService(): ServerResponse =
        staticResponse.bodyValueAndAwait(
            resourceLoader.getResource("$RESOURCE_PATH/terms_of_service.html"),
        )
}
