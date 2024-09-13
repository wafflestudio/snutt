package com.wafflestudio.snu4t.lecturebuildings.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.wafflestudio.snu4t.lecturebuildings.data.SnuMapSearchResult
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.awaitBody

@Component
class SnuMapRepository(
    private val snuMapApi: SnuMapApi,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        const val SNU_MAP_SEARCH_PATH = "/api/search.action"
        const val DEFAULT_SEARCH_PARAMS = "lang_type=KOR"
    }

    suspend fun getLectureBuildingSearchResult(buildingNum: String): SnuMapSearchResult =
        snuMapApi.get().uri { builder ->
            builder.path(SNU_MAP_SEARCH_PATH)
                .query(DEFAULT_SEARCH_PARAMS)
                .queryParam("search_word", buildingNum)
                .build()
        }
            .retrieve().awaitBody<String>()
            .let { objectMapper.readValue<SnuMapSearchResult>(it) }
}
