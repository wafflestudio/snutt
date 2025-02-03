package com.wafflestudio.snutt.common.util

import com.wafflestudio.snutt.common.enum.Semester
import org.springframework.web.util.DefaultUriBuilderFactory

object SugangSnuUrlUtils {
    const val REDIRECT_PREFIX_URL = "https://libproxy.snu.ac.kr/_Lib_Proxy_Url/"

    fun convertSemesterToSugangSnuSearchString(semester: Semester): String =
        when (semester) {
            Semester.SPRING -> "U000200001U000300001"
            Semester.SUMMER -> "U000200001U000300002"
            Semester.AUTUMN -> "U000200002U000300001"
            Semester.WINTER -> "U000200002U000300002"
        }

    fun parseSyllabusUrl(
        year: Int,
        semester: Semester,
        courseNumber: String,
        lectureNumber: String,
    ): String =
        DefaultUriBuilderFactory().builder()
            .scheme("https")
            .host("sugang.snu.ac.kr")
            .path("/sugang/cc/cc103.action")
            .queryParam("openSchyy", year)
            .queryParam("openShtmFg", makeOpenShtmFg(semester))
            .queryParam("openDetaShtmFg", makeOpenDetaShtmFg(semester))
            .queryParam("sbjtCd", courseNumber)
            .queryParam("ltNo", lectureNumber)
            .queryParam("sbjtSubhCd", "000")
            .build()
            .toString()

    private fun makeOpenShtmFg(semester: Semester) =
        when (semester) {
            Semester.SPRING, Semester.SUMMER -> "U000200001"
            Semester.AUTUMN, Semester.WINTER -> "U000200002"
        }

    private fun makeOpenDetaShtmFg(semester: Semester) =
        when (semester) {
            Semester.SPRING, Semester.AUTUMN -> "U000300001"
            Semester.SUMMER, Semester.WINTER -> "U000300002"
        }
}
