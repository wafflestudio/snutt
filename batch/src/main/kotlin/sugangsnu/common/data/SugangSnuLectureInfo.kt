package com.wafflestudio.snutt.sugangsnu.common.data

import com.fasterxml.jackson.annotation.JsonProperty

data class SugangSnuLectureInfo(
    @param:JsonProperty("ltTime")
    val ltTime: List<String> = listOf(),
    @param:JsonProperty("ltRoom")
    val ltRoom: List<String> = listOf(),
    @param:JsonProperty("LISTTAB01")
    val subInfo: SugangSnuLectureSubInfo = SugangSnuLectureSubInfo(),
//    @param:JsonProperty("sbjtSubhCd")
//    val sbjtSubhCd: String? = null,
//    @param:JsonProperty("t_profPersNo")
//    val tProfPersNo: String? = null,
//    @param:JsonProperty("openShtmFg")
//    val openShtmFg: String? = null,
//    @param:JsonProperty("cscLocale")
//    val cscLocale: String? = null,
//    @param:JsonProperty("sbjtCd")
//    val sbjtCd: String? = null,
//    @param:JsonProperty("bdegrSystemFg")
//    val bdegrSystemFg: String? = null,
//    @param:JsonProperty("openSchyy")
//    val openSchyy: String? = null,
//    @param:JsonProperty("ltType")
//    val ltType: ArrayList<String> = arrayListOf(),
//    @param:JsonProperty("openDetaShtmFg")
//    val openDetaShtmFg: String? = null,
//    @param:JsonProperty("workType")
//    val workType: String? = null,
//    @param:JsonProperty("ltNo")
//    val ltNo: String? = null,
//    @param:JsonProperty("SCOR_CNT")
//    val SCORCNT: String? = null
)

data class SugangSnuLectureSubInfo(
    @param:JsonProperty("sbjtNm")
    val courseName: String? = null,
    @param:JsonProperty("sbjtSubhNm")
    val courseSubName: String? = null,
    @param:JsonProperty("profNm")
    val professorName: String? = null,
    @param:JsonProperty("sbjtFldNm")
    val category: String? = null,
    @param:JsonProperty("departmentKorNm")
    val departmentKorNm: String? = null,
    @param:JsonProperty("deptKorNm")
    val deptKorNm: String? = null,
    @param:JsonProperty("majorKorNm")
    val majorKorNm: String? = null,
    @param:JsonProperty("departmentEngNm")
    val departmentEngNm: String? = null,
    @param:JsonProperty("deptEngNm")
    val deptEngNm: String? = null,
    @param:JsonProperty("majorEngNm")
    val majorEngNm: String? = null,
    @param:JsonProperty("upSbjtFldEngNm")
    val upSbjtFldEngNm: String? = null,
    // 학사 석사
    @param:JsonProperty("cptnCorsFgNm")
    val academicCourse: String? = null,
    // 학년
    @param:JsonProperty("openShyr")
    val academicYear: String? = null,
    // 정원
    @param:JsonProperty("tlsnAplyCapaCnt")
    val quota: Int? = null,
    // 비고
    @param:JsonProperty("openLtRemk")
    val remark: String? = null,
    // 교과 구분
    @param:JsonProperty("submattFgNm")
    val classification: String? = null,
    // 단과대학
    @param:JsonProperty("univsKorNm")
    val college: String? = null,
    // 학점
    @param:JsonProperty("openPnt")
    val credit: Int? = null,
//    @param:JsonProperty("openLtEngRemk")
//    val openLtEngRemk: String? = null,
//    @param:JsonProperty("frnStdTlsnLmtYn")
//    val frnStdTlsnLmtYn: String? = null,
//    @param:JsonProperty("mrksGvMthd")
//    val mrksGvMthd: String? = null,
//    @param:JsonProperty("pracSbjtFg")
//    val pracSbjtFg: String? = null,
//    @param:JsonProperty("elAttendUseYn")
//    val elAttendUseYn: String? = null,
//    @param:JsonProperty("openShtm")
//    val openShtm: String? = null,
//    @param:JsonProperty("openDetaShtmFg")
//    val openDetaShtmFg: String? = null,
//    @param:JsonProperty("sbjtSubhEngNm")
//    val sbjtSubhEngNm: String? = null,
//    @param:JsonProperty("submattCorsEng")
//    val submattCorsEng: String? = null,
//    @param:JsonProperty("tlsnAddSltTypeFgEngNm")
//    val tlsnAddSltTypeFgEngNm: String? = null,
//    @param:JsonProperty("lsnProgType")
//    val lsnProgType: String? = null,
//    @param:JsonProperty("sbjtCdAndNm")
//    val sbjtCdAndNm: String? = null,
//    @param:JsonProperty("univsEngNm")
//    val univsEngNm: String? = null,
//    @param:JsonProperty("ltTime")
//    val ltTime: String? = null,
//    @param:JsonProperty("lsnTmtablFormaSmryCtnt")
//    val lsnTmtablFormaSmryCtnt: String? = null,
//    @param:JsonProperty("thssRechSbjtYn")
//    val thssRechSbjtYn: String? = null,
//    @param:JsonProperty("mrksApprMthdChgPosbYn")
//    val mrksApprMthdChgPosbYn: String? = null,
//    @param:JsonProperty("schyyIntgrtnSbjtYn")
//    val schyyIntgrtnSbjtYn: String? = null,
//    @param:JsonProperty("cptnCorsFg")
//    val cptnCorsFg: String? = null,
//    @param:JsonProperty("forexamSubstYn")
//    val forexamSubstYn: String? = null,
//    @param:JsonProperty("submattCors")
//    val submattCors: String? = null,
//    @param:JsonProperty("lsnProgLang")
//    val lsnProgLang: String? = null,
//    @param:JsonProperty("sbjtReptCptnPosbYn")
//    val sbjtReptCptnPosbYn: String? = null,
//    @param:JsonProperty("ltType")
//    val ltType: String? = null,
//    @param:JsonProperty("stdIntrvPosbTmList")
//    val stdIntrvPosbTmList: String? = null,
//    @param:JsonProperty("stdIntrvPosbTmEngList")
//    val stdIntrvPosbTmEngList: String? = null,
//    @param:JsonProperty("mrksRelevalYn")
//    val mrksRelevalYn: String? = null,
//    @param:JsonProperty("theoryLtTmCnt")
//    val theoryLtTmCnt: Int? = null,
//    @param:JsonProperty("testPracLtTmCnt")
//    val testPracLtTmCnt: Int? = null,
//    @param:JsonProperty("genrlRemoteLtYn")
//    val genrlRemoteLtYn: String? = null,
//    @param:JsonProperty("openShtmFg")
//    val openShtmFg: String? = null,
//    @param:JsonProperty("cptnCorsFgEngNm")
//    val cptnCorsFgEngNm: String? = null,
//    @param:JsonProperty("openDetaShtmEng")
//    val openDetaShtmEng: String? = null,
//    @param:JsonProperty("openDetaShtm")
//    val openDetaShtm: String? = null,
//    @param:JsonProperty("sbjtCd")
//    val sbjtCd: String? = null,
//    @param:JsonProperty("submattPartEng")
//    val submattPartEng: String? = null,
//    @param:JsonProperty("lsnProgTypeEng")
//    val lsnProgTypeEng: String? = null,
//    @param:JsonProperty("sbjtEngNm")
//    val sbjtEngNm: String? = null,
//    @param:JsonProperty("upSbjtFldNm")
//    val upSbjtFldNm: String? = null,
//    @param:JsonProperty("ltRoom")
//    val ltRoom: String? = null,
//    @param:JsonProperty("rowIdx")
//    val rowIdx: Int? = null,
//    @param:JsonProperty("ltNo")
//    val ltNo: String? = null,
//    @param:JsonProperty("lsnProgLangEng")
//    val lsnProgLangEng: String? = null,
//    @param:JsonProperty("sbjtSubhCd")
//    val sbjtSubhCd: String? = null,
//    @param:JsonProperty("sbjtFldEngNm")
//    val sbjtFldEngNm: String? = null,
//    @param:JsonProperty("mrksGvMthdEng")
//    val mrksGvMthdEng: String? = null,
//    @param:JsonProperty("tlsnAddSltTypeFg")
//    val tlsnAddSltTypeFg: String? = null,
//    @param:JsonProperty("pntPerTlsnAmtUntprc")
//    val pntPerTlsnAmtUntprc: String? = null,
//    @param:JsonProperty("submattPart")
//    val submattPart: String? = null,
//    @param:JsonProperty("submattFgEngNm")
//    val submattFgEngNm: String? = null,
//    @param:JsonProperty("openSchyy")
//    val openSchyy: String? = null,
//    @param:JsonProperty("rowCnt")
//    val rowCnt: Int? = null,
//    @param:JsonProperty("openShtmEng")
//    val openShtmEng: String? = null,
//    @param:JsonProperty("tlsnAddSltTypeFgNm")
//    val tlsnAddSltTypeFgNm: String? = null,
//    @param:JsonProperty("profEngNm")
//    val profEngNm: String? = null,
//    @param:JsonProperty("teachSbjtYn")
//    val teachSbjtYn: String? = null,
//    @param:JsonProperty("lsnTmtablFormaSmryEngCtnt")
//    val lsnTmtablFormaSmryEngCtnt: String? = null
)
