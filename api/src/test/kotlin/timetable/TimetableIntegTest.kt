package com.wafflestudio.snutt.timetable

import BaseIntegTest
import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.snutt.common.enums.Semester
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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
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
        "POST /v1/tables 요청시 기존 시간표 일부에 order가 없으면" should {
            val table1 = timetableFixture.getTimetable("first").copy(order = 10).let { timetableRepository.save(it) }
            val table2 = timetableFixture.getTimetable("second").let { timetableRepository.save(it) }
            val table3 = timetableFixture.getTimetable("third").copy(order = 0).let { timetableRepository.save(it) }

            "현재 조회 순서대로 기존 시간표와 새 시간표에 order를 다시 저장" {
                var newTimetableId: String? = null

                webTestClient
                    .post()
                    .uri("/v1/tables")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .bodyValue("""{"year":2023, "semester":3, "title":"NewLastTable"}""".trimIndent())
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<List<TimetableBriefDto>>()
                    .returnResult()
                    .responseBody
                    .should { body ->
                        body.shouldNotBeNull()
                        body.map { it.id }.take(3) shouldBe listOf(table1.id, table2.id, table3.id)
                        body.last().title shouldBe "NewLastTable"
                        newTimetableId = body.last().id
                    }

                listOf(table1, table2, table3)
                    .map { timetableRepository.findById(it.id!!)!!.order }
                    .shouldBe(listOf(0, 1, 2))
                timetableRepository.findById(newTimetableId!!)!!.order shouldBe 3
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
        "PUT /v1/tables/{year}/{semester}/order 요청시" should {
            val otherSemesterTable =
                timetableFixture.getTimetable("spring").copy(year = 2024, semester = Semester.SPRING).let { timetableRepository.save(it) }
            val table1 = timetableFixture.getTimetable("first").let { timetableRepository.save(it) }
            val table2 = timetableFixture.getTimetable("second").let { timetableRepository.save(it) }
            val table3 = timetableFixture.getTimetable("third").let { timetableRepository.save(it) }

            "요청한 순서대로 변경" {
                webTestClient
                    .put()
                    .uri("/v1/tables/2023/3/order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .bodyValue(
                        """
                        {
                          "timetableIds": ["${table3.id}", "${table1.id}", "${table2.id}"]
                        }
                        """.trimIndent(),
                    ).exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<List<TimetableBriefDto>>()
                    .returnResult()
                    .responseBody
                    .should { body ->
                        body.shouldNotBeNull()
                        body.map { it.id } shouldBe listOf(table3.id, table1.id, table2.id)
                    }

                listOf(table3, table1, table2)
                    .map { timetableRepository.findById(it.id!!)!!.order }
                    .shouldBe(listOf(0, 1, 2))
                timetableRepository.findById(otherSemesterTable.id!!)!!.order shouldBe null
            }

            "일부 시간표가 누락되면 실패" {
                webTestClient
                    .put()
                    .uri("/v1/tables/2023/3/order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-access-token", userFixture.testUser.credentialHash)
                    .bodyValue(
                        """
                        {
                          "timetableIds": ["${table1.id}", "${table2.id}"]
                        }
                        """.trimIndent(),
                    ).exchange()
                    .expectStatus()
                    .isBadRequest
                    .expectBody()
                    .jsonPath("$.errcode")
                    .isEqualTo(40028)
            }
        }
    })
