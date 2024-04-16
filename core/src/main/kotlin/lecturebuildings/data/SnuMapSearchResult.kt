package com.wafflestudio.snu4t.lecturebuildings.data

import com.fasterxml.jackson.annotation.JsonProperty

data class SnuMapSearchResult(
    @JsonProperty("search_list")
    val searchList: List<SnuMapSearchItem>,
)

data class SnuMapSearchItem(
    @JsonProperty("lat_val")
    val latitudeInDMS: Double,
    @JsonProperty("lon_val")
    val longitudeInDMS: Double,
    @JsonProperty("lat_val1")
    val latitudeInDecimal: Double,
    @JsonProperty("lon_val1")
    val longitudeInDecimal: Double,
    @JsonProperty("vil_dong_nm")
    val buildingNumber: String,
    val name: String,
    @JsonProperty("ename")
    val englishName: String? = null,
    @JsonProperty("con_type")
    val contentType: String,
    @JsonProperty("fac_type")
    val facType: String,
)
