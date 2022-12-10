package com.wafflestudio.snu4t

import com.wafflestudio.snu4t.router.MainRouter
import io.kotest.core.spec.style.WordSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
class ExampleIntegTest(val mainRouter: MainRouter) : WordSpec({
    val restDocumentation = ManualRestDocumentation()
    val webTestClient = WebTestClient.bindToRouterFunction(mainRouter.route())
        .configureClient()
        .filter(documentationConfiguration(restDocumentation))
        .build()

    beforeEach {
        restDocumentation.beforeTest(javaClass, "ExampleTest")
    }

    afterEach {
        restDocumentation.afterTest()
    }

    "request ping" should {
        "response pong" {
            webTestClient.get().uri("/ping").exchange().expectStatus().isOk.expectBody(String::class.java)
                .isEqualTo<WebTestClient.BodySpec<String, *>>("pong")
                .consumeWith(
                    document(
                        "ping",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                    )
                )
        }
    }
})
