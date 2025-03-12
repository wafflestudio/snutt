package com.wafflestudio.snutt.timetable

import BaseIntegTest
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.snutt.evaluation.service.EvService
import com.wafflestudio.snutt.fixture.TimetableFixture
import com.wafflestudio.snutt.fixture.UserFixture
import com.wafflestudio.snutt.handler.RequestContext
import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.router.MainRouter
import com.wafflestudio.snutt.timetables.dto.TimetableLegacyDto
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import timetables.dto.TimetableBriefDto

@SpringBootTest
class TimetableIntegTest(
    @MockkBean private val mockMiddleware: SnuttRestApiDefaultMiddleware,
    @MockkBean private val mockSnuttEvService: EvService,
    val mainRouter: MainRouter,
    val timetableFixture: TimetableFixture,
    val userFixture: UserFixture,
    val timetableRepository: TimetableRepository,
    val repositories: List<CoroutineCrudRepository<*, *>>,
) : BaseIntegTest({
        val timetableServer =
            WebTestClient.bindToRouterFunction(mainRouter.tableRoute()).configureClient().defaultHeaders { header ->
                header.contentType = MediaType.APPLICATION_JSON
            }.build()

        coEvery { mockSnuttEvService.getSummariesByIds(any()) } returns emptyList()
        coEvery { mockSnuttEvService.getEvIdsBySnuttIds(any()) } returns emptyList()
        coEvery { mockMiddleware.invoke(any(), any()) } returns RequestContext(user = userFixture.testUser)
        afterContainer { repositories.forEach { it.deleteAll() } }

        "POST /v1/tables" should {
            "success" {
                timetableServer.post().uri("/v1/tables")
                    .bodyValue("""{"year":2016, "semester":3, "title":"MyTimeTable"}""".trimIndent()).exchange()
                    .expectStatus().isOk.expectBody<List<TimetableBriefDto>>().returnResult().responseBody.should { body ->
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
                timetableServer.get().uri("/v1/tables").exchange()
                    .expectStatus().isOk.expectBody<List<TimetableBriefDto>>()
                    .returnResult().responseBody.should { body ->
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
                timetableServer.get().uri("/v1/tables").exchange().expectStatus().isOk.expectBody()
                    .jsonPath("$.[0]._id").exists()
                    .jsonPath("$.[0].title").exists()
                    .jsonPath("$.[0].year").isNumber
                    .jsonPath("$.[0].semester").isNumber
                    .jsonPath("$.[0].isPrimary").isBoolean
                    .jsonPath("$.[0].updated_at").exists()
                    .jsonPath("$.[0].total_credit").exists()
            }
        }
        "GET /v1/tables/{tableId} 요청 시" should {
            val table = timetableFixture.getTimetable("test").let { timetableRepository.save(it) }
            "정상 반환" {
                timetableServer.get().uri("/v1/tables/${table.id}").exchange()
                    .expectStatus().isOk.expectBody<TimetableLegacyDto>()
                    .returnResult().responseBody.should { body ->
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
                timetableServer.get().uri("/v1/tables/${table.id}").exchange().expectStatus().isOk.expectBody()
                    .jsonPath("$._id").exists()
                    .jsonPath("$.user_id").exists()
                    .jsonPath("$.year").isNumber
                    .jsonPath("$.semester").isNumber
                    .jsonPath("$.lecture_list").isArray
                    .jsonPath("$.title").exists()
                    .jsonPath("$.theme").isNumber
                    .jsonPath("$.isPrimary").isBoolean
                    .jsonPath("$.updated_at").exists()
            }
        }
    })
