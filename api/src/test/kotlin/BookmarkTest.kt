package com.wafflestudio.snu4t

import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.snu4t.bookmark.dto.BookmarkLectureAddRequest
import com.wafflestudio.snu4t.bookmark.dto.BookmarkResponse
import com.wafflestudio.snu4t.handler.RequestContext
import com.wafflestudio.snu4t.lectures.service.LectureFixture
import com.wafflestudio.snu4t.middleware.ApiKeyMiddleware
import com.wafflestudio.snu4t.router.MainRouter
import com.wafflestudio.snu4t.users.dto.LocalRegisterRequest
import com.wafflestudio.snu4t.users.service.UserService
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.WordSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.coEvery
import io.mockk.slot
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
class BookmarkTest(private val mainRouter: MainRouter, private val userService: UserService, private val lectureFixture: LectureFixture) : WordSpec() {
    @MockkBean
    lateinit var apiKeyMiddleware: ApiKeyMiddleware

    init {
        extension(SpringExtension)
        val token: String by lazy {
            runBlocking {
                userService.registerLocal(LocalRegisterRequest("test", "testtest15", "test@wafflestudio.com"))
            }.token
        }

        runBlocking { lectureFixture.saveTestLecture() }

        val restDocumentation = ManualRestDocumentation()
        val webTestClient = WebTestClient.bindToRouterFunction(mainRouter.route())
            .configureClient()
            .filter(documentationConfiguration(restDocumentation))
            .defaultHeader("x-access-token", token)
            .build()

        beforeEach {
            restDocumentation.beforeTest(javaClass, "bookmarkTest")
            val contextSlot = slot<RequestContext>()

            coEvery { apiKeyMiddleware.invoke(any(), capture(contextSlot)) } answers { contextSlot.captured }
        }

        afterEach {
            restDocumentation.afterTest()
        }

        "post bookmark" should {
            "should response bookmark" {
                webTestClient.post().uri("/v1/bookmarks/lecture")
                    .bodyValue(BookmarkLectureAddRequest(lectureId = "63c152573ef71d00162f65cf"))
                    .exchange().expectStatus().isOk.expectBody(BookmarkResponse::class.java)
                    .consumeWith(
                        document(
                            "bookmark_add_lecture",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                fieldWithPath("lecture_id").type(JsonFieldType.STRING).description("lecture id")
                                    .optional(),
                            ),
                            responseFields(
                                fieldWithPath("year").type(JsonFieldType.NUMBER).description("year"),
                                fieldWithPath("semester").type(JsonFieldType.NUMBER)
                                    .description("semester - 1(봄),2(여름),3(가을),4(겨울)"),
                                fieldWithPath("lectures[].id").type(JsonFieldType.STRING).description("lecture id"),
                                fieldWithPath("lectures[].academic_year").type(JsonFieldType.STRING).optional()
                                    .description("수강학년"),
                                fieldWithPath("lectures[].category").type(JsonFieldType.STRING).optional()
                                    .description("카테고리 (전필, 전선, 교양 ...) "),
                                fieldWithPath("lectures[].class_time").type(JsonFieldType.STRING).optional()
                                    .description("수업 교시"),
                                fieldWithPath("lectures[].real_class_time").type(JsonFieldType.STRING).optional()
                                    .description("수업 시간"),
                                fieldWithPath("lectures[].class_time_json").type(JsonFieldType.ARRAY)
                                    .description("시간 객체 리스트"),
                                fieldWithPath("lectures[].class_time_json[].$").optional().type(JsonFieldType.OBJECT)
                                    .description("시간 객체"),
                                fieldWithPath("lectures[].class_time_json[].id").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].class_time_json[].day").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].class_time_json[].start_time").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].class_time_json[].end_time").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].class_time_json[].len").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].class_time_json[].start").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].class_time_mask").type(JsonFieldType.ARRAY).description(""),
                                fieldWithPath("lectures[].classification").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].credit").type(JsonFieldType.NUMBER).description(""),
                                fieldWithPath("lectures[].department").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].instructor").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].lecture_number").type(JsonFieldType.STRING).description(""),
                                fieldWithPath("lectures[].quota").type(JsonFieldType.NUMBER).optional().description(""),
                                fieldWithPath("lectures[].remark").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].semester").type(JsonFieldType.STRING).optional()
                                    .description(""),
                                fieldWithPath("lectures[].year").type(JsonFieldType.STRING).optional().description(""),
                                fieldWithPath("lectures[].course_number").type(JsonFieldType.STRING).description(""),
                                fieldWithPath("lectures[].course_title").type(JsonFieldType.STRING).description(""),
                            ),
                        )
                    )
            }
        }
        "get bookmark" should {
            "should response bookmark" {
                webTestClient.get().uri { builder ->
                    builder.path("/v1/bookmarks").queryParam("year", "2021").queryParam("semester", "1").build()
                }
                    .exchange().expectStatus().isOk.expectBody(BookmarkResponse::class.java)
                    .consumeWith(
                        document(
                            "bookmark_get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                fieldWithPath("year").type(JsonFieldType.NUMBER).description("year"),
                                fieldWithPath("semester").type(JsonFieldType.NUMBER)
                                    .description("semester - 1(봄),2(여름),3(가을),4(겨울)"),
                                fieldWithPath("lectures[]").type(JsonFieldType.ARRAY).description("post와 동일"),
                            ),
                        )
                    )
            }
        }
    }
}
