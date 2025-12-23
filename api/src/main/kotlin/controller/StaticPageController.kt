package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping("")
class StaticPageController {
    companion object {
        private const val RESOURCE_PATH = "/views"
    }

    @GetMapping("/member", produces = ["text/html; charset=utf-8"])
    suspend fun member(): Resource = ClassPathResource("$RESOURCE_PATH/member.html")

    @GetMapping("/privacy_policy", produces = ["text/html; charset=utf-8"])
    suspend fun privacyPolicy(): Resource = ClassPathResource("$RESOURCE_PATH/privacy_policy.html")

    @GetMapping("/terms_of_service", produces = ["text/html; charset=utf-8"])
    suspend fun termsOfService(): Resource = ClassPathResource("$RESOURCE_PATH/terms_of_service.html")
}
