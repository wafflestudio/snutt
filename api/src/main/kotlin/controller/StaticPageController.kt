package com.wafflestudio.snutt.controller

import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@RequestMapping("")
class StaticPageController(
    private val resourceLoader: ResourceLoader,
) {
    companion object {
        private const val RESOURCE_PATH = "classpath:views"
        private val staticResponse =
            ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                .header("Content-Type", "text/html; charset=utf-8")
    }

    @GetMapping("/member", produces = ["text/html; charset=utf-8"])
    suspend fun member(): ResponseEntity<Resource> =
        staticResponse.body(
            resourceLoader.getResource("$RESOURCE_PATH/member.html"),
        )

    @GetMapping("/privacy_policy", produces = ["text/html; charset=utf-8"])
    suspend fun privacyPolicy(): ResponseEntity<Resource> =
        staticResponse.body(
            resourceLoader.getResource("$RESOURCE_PATH/privacy_policy.html"),
        )

    @GetMapping("/terms_of_service", produces = ["text/html; charset=utf-8"])
    suspend fun termsOfService(): ResponseEntity<Resource> =
        staticResponse.body(
            resourceLoader.getResource("$RESOURCE_PATH/terms_of_service.html"),
        )
}
