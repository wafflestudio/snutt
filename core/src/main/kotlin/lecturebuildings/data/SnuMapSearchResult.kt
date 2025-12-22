package com.wafflestudio.snutt.lecturebuildings.data

import com.fasterxml.jackson.annotation.JsonProperty

data class SnuMapSearchResult(
    @param:JsonProperty("search_list")
    val searchList: List<SnuMapSearchItem>,
)

data class SnuMapSearchItem(
    @param:JsonProperty("lat_val")
    val latitudeInDMS: Double,
    @param:JsonProperty("lon_val")
    val longitudeInDMS: Double,
    @param:JsonProperty("lat_val1")
    val latitudeInDecimal: Double,
    @param:JsonProperty("lon_val1")
    val longitudeInDecimal: Double,
    @param:JsonProperty("vil_dong_nm")
    val buildingNumber: String?,
    val name: String,
    @param:JsonProperty("ename")
    val englishName: String? = null,
    @param:JsonProperty("con_type")
    val contentType: String,
    @param:JsonProperty("fac_type")
    val facType: String,
)
