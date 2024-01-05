package com.wafflestudio.snu4t.sugangsnu.common.data

import com.fasterxml.jackson.annotation.JsonProperty

data class SugangSnuLectureInfo(
    @JsonProperty("ltTime")
    val ltTime: List<String> = listOf(),
    @JsonProperty("ltRoom")
    val ltRoom: List<String> = listOf(),
    @JsonProperty("LISTTAB01")
    val subInfo: SugangSnuLectureSubInfo = SugangSnuLectureSubInfo(),
//    @JsonProperty("sbjtSubhCd")
//    val sbjtSubhCd: String? = null,
//    @JsonProperty("t_profPersNo")
//    val tProfPersNo: String? = null,
//    @JsonProperty("openShtmFg")
//    val openShtmFg: String? = null,
//    @JsonProperty("cscLocale")
//    val cscLocale: String? = null,
//    @JsonProperty("sbjtCd")
//    val sbjtCd: String? = null,
//    @JsonProperty("bdegrSystemFg")
//    val bdegrSystemFg: String? = null,
//    @JsonProperty("openSchyy")
//    val openSchyy: String? = null,
//    @JsonProperty("ltType")
//    val ltType: ArrayList<String> = arrayListOf(),
//    @JsonProperty("openDetaShtmFg")
//    val openDetaShtmFg: String? = null,
//    @JsonProperty("workType")
//    val workType: String? = null,
//    @JsonProperty("ltNo")
//    val ltNo: String? = null,
//    @JsonProperty("SCOR_CNT")
//    val SCORCNT: String? = null
)

data class SugangSnuLectureSubInfo(
    @JsonProperty("sbjtNm")
    val courseName: String? = null,
    @JsonProperty("sbjtSubhNm")
    val courseSubName: String? = null,
    @JsonProperty("profNm")
    val professorName: String? = null,
    @JsonProperty("departmentKorNm")
    val departmentKorNm: String? = null,
    @JsonProperty("deptKorNm")
    val deptKorNm: String? = null,
    @JsonProperty("majorKorNm")
    val majorKorNm: String? = null,
    @JsonProperty("sbjtFldNm")
    val sbjtFldNm: String? = null,
    @JsonProperty("departmentEngNm")
    val departmentEngNm: String? = null,
    @JsonProperty("deptEngNm")
    val deptEngNm: String? = null,
    @JsonProperty("majorEngNm")
    val majorEngNm: String? = null,
    @JsonProperty("upSbjtFldEngNm")
    val upSbjtFldEngNm: String? = null,
//    @JsonProperty("openLtEngRemk")
//    val openLtEngRemk: String? = null,
//    @JsonProperty("frnStdTlsnLmtYn")
//    val frnStdTlsnLmtYn: String? = null,
//    @JsonProperty("mrksGvMthd")
//    val mrksGvMthd: String? = null,
//    @JsonProperty("pracSbjtFg")
//    val pracSbjtFg: String? = null,
//    @JsonProperty("elAttendUseYn")
//    val elAttendUseYn: String? = null,
//    @JsonProperty("openShtm")
//    val openShtm: String? = null,
//    @JsonProperty("openDetaShtmFg")
//    val openDetaShtmFg: String? = null,
//    @JsonProperty("sbjtSubhEngNm")
//    val sbjtSubhEngNm: String? = null,
//    @JsonProperty("submattCorsEng")
//    val submattCorsEng: String? = null,
//    @JsonProperty("tlsnAddSltTypeFgEngNm")
//    val tlsnAddSltTypeFgEngNm: String? = null,
//    @JsonProperty("lsnProgType")
//    val lsnProgType: String? = null,
//    @JsonProperty("sbjtCdAndNm")
//    val sbjtCdAndNm: String? = null,
//    @JsonProperty("univsEngNm")
//    val univsEngNm: String? = null,
//    @JsonProperty("ltTime")
//    val ltTime: String? = null,
//    @JsonProperty("lsnTmtablFormaSmryCtnt")
//    val lsnTmtablFormaSmryCtnt: String? = null,
//    @JsonProperty("thssRechSbjtYn")
//    val thssRechSbjtYn: String? = null,
//    @JsonProperty("mrksApprMthdChgPosbYn")
//    val mrksApprMthdChgPosbYn: String? = null,
//    @JsonProperty("schyyIntgrtnSbjtYn")
//    val schyyIntgrtnSbjtYn: String? = null,
//    @JsonProperty("cptnCorsFg")
//    val cptnCorsFg: String? = null,
//    @JsonProperty("forexamSubstYn")
//    val forexamSubstYn: String? = null,
//    @JsonProperty("submattCors")
//    val submattCors: String? = null,
//    @JsonProperty("lsnProgLang")
//    val lsnProgLang: String? = null,
//    @JsonProperty("sbjtReptCptnPosbYn")
//    val sbjtReptCptnPosbYn: String? = null,
//    @JsonProperty("ltType")
//    val ltType: String? = null,
//    @JsonProperty("stdIntrvPosbTmList")
//    val stdIntrvPosbTmList: String? = null,
//    @JsonProperty("stdIntrvPosbTmEngList")
//    val stdIntrvPosbTmEngList: String? = null,
//    @JsonProperty("openPnt")
//    val openPnt: Int? = null,
//    @JsonProperty("mrksRelevalYn")
//    val mrksRelevalYn: String? = null,
//    @JsonProperty("theoryLtTmCnt")
//    val theoryLtTmCnt: Int? = null,
//    @JsonProperty("cptnCorsFgNm")
//    val cptnCorsFgNm: String? = null,
//    @JsonProperty("univsKorNm")
//    val univsKorNm: String? = null,
//    @JsonProperty("submattFgNm")
//    val submattFgNm: String? = null,
//    @JsonProperty("testPracLtTmCnt")
//    val testPracLtTmCnt: Int? = null,
//    @JsonProperty("genrlRemoteLtYn")
//    val genrlRemoteLtYn: String? = null,
//    @JsonProperty("openShtmFg")
//    val openShtmFg: String? = null,
//    @JsonProperty("cptnCorsFgEngNm")
//    val cptnCorsFgEngNm: String? = null,
//    @JsonProperty("openDetaShtmEng")
//    val openDetaShtmEng: String? = null,
//    @JsonProperty("openDetaShtm")
//    val openDetaShtm: String? = null,
//    @JsonProperty("sbjtCd")
//    val sbjtCd: String? = null,
//    @JsonProperty("submattPartEng")
//    val submattPartEng: String? = null,
//    @JsonProperty("lsnProgTypeEng")
//    val lsnProgTypeEng: String? = null,
//    @JsonProperty("sbjtEngNm")
//    val sbjtEngNm: String? = null,
//    @JsonProperty("upSbjtFldNm")
//    val upSbjtFldNm: String? = null,
//    @JsonProperty("ltRoom")
//    val ltRoom: String? = null,
//    @JsonProperty("rowIdx")
//    val rowIdx: Int? = null,
//    @JsonProperty("ltNo")
//    val ltNo: String? = null,
//    @JsonProperty("lsnProgLangEng")
//    val lsnProgLangEng: String? = null,
//    @JsonProperty("sbjtSubhCd")
//    val sbjtSubhCd: String? = null,
//    @JsonProperty("sbjtFldEngNm")
//    val sbjtFldEngNm: String? = null,
//    @JsonProperty("openLtRemk")
//    val openLtRemk: String? = null,
//    @JsonProperty("tlsnAplyCapaCnt")
//    val tlsnAplyCapaCnt: Int? = null,
//    @JsonProperty("mrksGvMthdEng")
//    val mrksGvMthdEng: String? = null,
//    @JsonProperty("tlsnAddSltTypeFg")
//    val tlsnAddSltTypeFg: String? = null,
//    @JsonProperty("pntPerTlsnAmtUntprc")
//    val pntPerTlsnAmtUntprc: String? = null,
//    @JsonProperty("submattPart")
//    val submattPart: String? = null,
//    @JsonProperty("submattFgEngNm")
//    val submattFgEngNm: String? = null,
//    @JsonProperty("openShyr")
//    val openShyr: String? = null,
//    @JsonProperty("openSchyy")
//    val openSchyy: String? = null,
//    @JsonProperty("rowCnt")
//    val rowCnt: Int? = null,
//    @JsonProperty("openShtmEng")
//    val openShtmEng: String? = null,
//    @JsonProperty("tlsnAddSltTypeFgNm")
//    val tlsnAddSltTypeFgNm: String? = null,
//    @JsonProperty("profEngNm")
//    val profEngNm: String? = null,
//    @JsonProperty("teachSbjtYn")
//    val teachSbjtYn: String? = null,
//    @JsonProperty("lsnTmtablFormaSmryEngCtnt")
//    val lsnTmtablFormaSmryEngCtnt: String? = null
)
