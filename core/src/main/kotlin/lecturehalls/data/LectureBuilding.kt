package com.wafflestudio.snu4t.lecturehalls.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class LectureBuilding(
    @Id
    val id: String? = null,

    // 동
    val buildingNumber: String,

    // 건물 이름(한국어) - 자연과학대학(500)
    val buildingNameKor: String? = null,

    // 건물 이름(영어) - College of Natural Sciences(500)
    val buildingNameEng: String? = null,

    // 위경도 - 37.4592190840394, 126.948120067187
    val locationInDMS: GeoCoordinate,

    // 위경도(십진표기) - 488525, 1099948
    val locationInDecimal: GeoCoordinate,

    // 캠퍼스 - 관악
    val campus: Campus,
)
