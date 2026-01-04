package com.wafflestudio.snutt.sugangsnu.common

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.util.SugangSnuUrlUtils.convertSemesterToSugangSnuSearchString
import com.wafflestudio.snutt.sugangsnu.common.api.SugangSnuApi
import com.wafflestudio.snutt.sugangsnu.common.data.SugangSnuCoursebookCondition
import com.wafflestudio.snutt.sugangsnu.common.data.SugangSnuLectureInfo
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.core.io.buffer.PooledDataBuffer
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.createExceptionAndAwait
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue

@Component
@RegisterReflectionForBinding(
    SugangSnuCoursebookCondition::class,
    SugangSnuLectureInfo::class,
)
class SugangSnuRepository(
    private val sugangSnuApi: SugangSnuApi,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        const val SUGANG_SNU_COURSEBOOK_PATH = "/sugang/cc/cc100ajax.action"
        const val DEFAULT_COURSEBOOK_PARAMS = "openUpDeptCd=&openDeptCd="
        const val SUGANG_SNU_SEARCH_PATH = "/sugang/cc/cc100InterfaceSrch.action"
        const val DEFAULT_SEARCH_PAGE_PARAMS = "workType=S&sortKey=&sortOrder="
        const val SUGANG_SNU_SEARCH_POPUP_PATH = "/sugang/cc/cc101ajax.action"
        const val DEFAULT_SEARCH_POPUP_PARAMS = """t_profPersNo=&workType=+&sbjtSubhCd=000"""
        const val SUGANG_SNU_LECTURE_EXCEL_DOWNLOAD_PATH = "/sugang/cc/cc100InterfaceExcel.action"
        val DEFAULT_LECTURE_EXCEL_DOWNLOAD_PARAMS =
            """
            seeMore=더보기&
            srchBdNo=&srchCamp=&srchOpenSbjtFldCd=&srchCptnCorsFg=&
            srchCurrPage=1&
            srchExcept=&srchGenrlRemoteLtYn=&srchIsEngSbjt=&
            srchIsPendingCourse=&srchLsnProgType=&srchMrksApprMthdChgPosbYn=&srchMrksGvMthd=&
            srchOpenUpDeptCd=&srchOpenMjCd=&srchOpenPntMax=&srchOpenPntMin=&srchOpenSbjtDayNm=&
            srchOpenSbjtNm=&srchOpenSbjtTm=&srchOpenSbjtTmNm=&srchOpenShyr=&srchOpenSubmattCorsFg=&
            srchOpenSubmattFgCd1=&srchOpenSubmattFgCd2=&srchOpenSubmattFgCd3=&srchOpenSubmattFgCd4=&
            srchOpenSubmattFgCd5=&srchOpenSubmattFgCd6=&srchOpenSubmattFgCd7=&srchOpenSubmattFgCd8=&
            srchOpenSubmattFgCd9=&srchOpenDeptCd=&srchOpenUpSbjtFldCd=&
            srchPageSize=9999&
            srchProfNm=&srchSbjtCd=&srchSbjtNm=&srchTlsnAplyCapaCntMax=&srchTlsnAplyCapaCntMin=&srchTlsnRcntMax=&srchTlsnRcntMin=&
            workType=EX
            """.trimIndent().replace("\n", "")
    }

    suspend fun getSearchPageHtml(
        year: Int,
        semester: Semester,
        pageNo: Int = 1
    ): PooledDataBuffer =
        sugangSnuApi
            .get()
            .uri { builder ->
                val semesterSearchString = convertSemesterToSugangSnuSearchString(semester)
                builder
                    .path(SUGANG_SNU_SEARCH_PATH)
                    .query(DEFAULT_SEARCH_PAGE_PARAMS)
                    .queryParam("srchOpenSchyy", year)
                    .queryParam("srchOpenShtm", semesterSearchString)
                    .queryParam("pageNo", pageNo)
                    .build()
            }.accept(MediaType.TEXT_HTML)
            .retrieve()
            .awaitBody<PooledDataBuffer>()

    suspend fun getLectureInfo(
        year: Int,
        semester: Semester,
        courseNumber: String,
        lectureNumber: String,
    ): SugangSnuLectureInfo =
        sugangSnuApi
            .get()
            .uri { builder ->
                val semesterSearchString = convertSemesterToSugangSnuSearchString(semester)
                builder
                    .path(SUGANG_SNU_SEARCH_POPUP_PATH)
                    .query(DEFAULT_SEARCH_POPUP_PARAMS)
                    .queryParam("openSchyy", year)
                    .queryParam("openShtmFg", semesterSearchString.substring(0..9))
                    .queryParam("openDetaShtmFg", semesterSearchString.substring(10))
                    .queryParam("sbjtCd", courseNumber)
                    .queryParam("ltNo", lectureNumber)
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody<String>()
            .let { objectMapper.readValue<SugangSnuLectureInfo>(it) }

    suspend fun getCoursebookCondition(): SugangSnuCoursebookCondition =
        sugangSnuApi
            .get()
            .uri { builder ->
                builder
                    .path(SUGANG_SNU_COURSEBOOK_PATH)
                    .query(DEFAULT_COURSEBOOK_PARAMS)
                    .build()
            }.awaitExchange {
                if (it.statusCode().is2xxSuccessful) {
                    it.awaitBody()
                } else {
                    throw it.createExceptionAndAwait()
                }
            }

    suspend fun getSugangSnuLectures(
        year: Int,
        semester: Semester,
        language: String = "ko",
    ): PooledDataBuffer =
        sugangSnuApi
            .get()
            .uri { builder ->
                builder.run {
                    path(SUGANG_SNU_LECTURE_EXCEL_DOWNLOAD_PATH)
                    query(DEFAULT_LECTURE_EXCEL_DOWNLOAD_PARAMS)
                    queryParam("srchLanguage", language)
                    queryParam("srchOpenSchyy", year)
                    queryParam("srchOpenShtm", convertSemesterToSugangSnuSearchString(semester))
                    build()
                }
            }.accept(MediaType.TEXT_HTML)
            .awaitExchange {
                if (it.statusCode().is2xxSuccessful) {
                    it.awaitBody()
                } else {
                    throw it.createExceptionAndAwait()
                }
            }
}
