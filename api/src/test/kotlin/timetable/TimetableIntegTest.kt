package com.wafflestudio.snutt.timetable

import BaseIntegTest
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.snutt.config.USER_ATTRIBUTE_KEY
import com.wafflestudio.snutt.evaluation.service.EvService
import com.wafflestudio.snutt.filter.ApiKeyWebFilter
import com.wafflestudio.snutt.filter.UserAuthenticationWebFilter
import com.wafflestudio.snutt.fixture.TimetableFixture
import com.wafflestudio.snutt.fixture.UserFixture
import com.wafflestudio.snutt.timetables.dto.TimetableLegacyDto
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.reactor.mono
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import timetables.dto.TimetableBriefDto

@SpringBootTest
@AutoConfigureWebTestClient
class TimetableIntegTest(
    @MockkBean private val mockSnuttEvService: EvService,
    @MockkBean private val apiKeyWebFilter: ApiKeyWebFilter,
    @MockkBean private val userAuthenticationWebFilter: UserAuthenticationWebFilter,
    val webTestClient: WebTestClient,
    val timetableFixture: TimetableFixture,
    val userFixture: UserFixture,
    val timetableRepository: TimetableRepository,
    val repositories: List<CoroutineCrudRepository<*, *>>,
) : BaseIntegTest({
        coEvery { mockSnuttEvService.getSummariesByIds(any()) } returns emptyList()
        coEvery { mockSnuttEvService.getEvIdsBySnuttIds(any()) } returns emptyList()
        coEvery {
            apiKeyWebFilter.filter(any(), any())
        } answers {
            val exchange = firstArg<ServerWebExchange>()
            val chain = secondArg<WebFilterChain>()
            chain.filter(exchange)
        }
        coEvery {
            userAuthenticationWebFilter.filter(any(), any())
        } answers {
            val exchange = firstArg<ServerWebExchange>()
            val chain = secondArg<WebFilterChain>()
            mono {
                exchange.attributes[USER_ATTRIBUTE_KEY] = userFixture.testUser
            }.then(chain.filter(exchange))
        }

        afterContainer {
            repositories.forEach { it.deleteAll() }
        }

        "POST /v1/tables" should {
            "success" {
                webTestClient
                    .post()
                    .uri("/v1/tables")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .bodyValue("""{"year":2016, "semester":3, "title":"MyTimeTable"}""".trimIndent())
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<List<TimetableBriefDto>>()
                    .returnResult()
                    .responseBody
                    .should { body ->
                        body.shouldNotBeNull()
                        body.size shouldBe 1
                        body[0].title shouldBe "MyTimeTable"
                        body[0].year shouldBe 2016
                        body[0].semester shouldBe 3
                    }
            }
        }

        "GET /v1/tables 요청시" should {
            val table = timetableFixture.getTimetable("test").let { timetableRepository.save(it) }
            "정상 반환" {
                webTestClient
                    .get()
                    .uri("/v1/tables")
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<List<TimetableBriefDto>>()
                    .returnResult()
                    .responseBody
                    .should { body ->
                        body.shouldNotBeNull()
                        body.size shouldBe 1
                        body[0].id shouldBe table.id
                        body[0].title shouldBe table.title
                        body[0].year shouldBe table.year
                        body[0].semester shouldBe table.semester.value
                        body[0].isPrimary shouldBe false
                        body[0].totalCredit shouldBe 0
                    }
            }
            "json 형태 확인" {
                webTestClient
                    .get()
                    .uri("/v1/tables")
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody()
                    .jsonPath("$.[0]._id")
                    .exists()
                    .jsonPath("$.[0].title")
                    .exists()
                    .jsonPath("$.[0].year")
                    .isNumber
                    .jsonPath("$.[0].semester")
                    .isNumber
                    .jsonPath("$.[0].isPrimary")
                    .isBoolean
                    .jsonPath("$.[0].updated_at")
                    .exists()
                    .jsonPath("$.[0].total_credit")
                    .exists()
            }
        }
        "GET /v1/tables/{tableId} 요청 시" should {
            val table = timetableFixture.getTimetable("test").let { timetableRepository.save(it) }
            "정상 반환" {
                webTestClient
                    .get()
                    .uri("/v1/tables/${table.id}")
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<TimetableLegacyDto>()
                    .returnResult()
                    .responseBody
                    .should { body ->
                        body.shouldNotBeNull()
                        body.id shouldBe table.id
                        body.title shouldBe table.title
                        body.year shouldBe table.year
                        body.semester shouldBe table.semester
                        body.isPrimary shouldBe false
                        body.theme shouldBe table.theme
                        body.userId shouldBe table.userId
                        body.lectures shouldBe emptyList()
                    }
            }
            "json 형태 확인" {
                webTestClient
                    .get()
                    .uri("/v1/tables/${table.id}")
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody()
                    .jsonPath("$._id")
                    .exists()
                    .jsonPath("$.user_id")
                    .exists()
                    .jsonPath("$.year")
                    .isNumber
                    .jsonPath("$.semester")
                    .isNumber
                    .jsonPath("$.lecture_list")
                    .isArray
                    .jsonPath("$.title")
                    .exists()
                    .jsonPath("$.theme")
                    .isNumber
                    .jsonPath("$.isPrimary")
                    .isBoolean
                    .jsonPath("$.updated_at")
                    .exists()
            }
        }
    })
