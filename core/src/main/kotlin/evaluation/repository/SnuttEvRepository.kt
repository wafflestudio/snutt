package com.wafflestudio.snu4t.evaluation.repository

import com.wafflestudio.snu4t.common.dto.ListResponse
import com.wafflestudio.snu4t.config.SnuttEvWebClient
import com.wafflestudio.snu4t.evaluation.dto.SnuttEvLectureSummaryDto
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.awaitBody

@Component
class SnuttEvRepository(private val snuttEvWebClient: SnuttEvWebClient) {
    suspend fun getSummariesByIds(ids: List<String>): List<SnuttEvLectureSummaryDto> = runCatching {
        snuttEvWebClient.get().uri { builder ->
            builder.path("/v1/lectures/snutt-summary")
                .queryParam("semesterLectureSnuttIds", ids.joinToString(","))
                .build()
        }
            .retrieve()
            .awaitBody<ListResponse<SnuttEvLectureSummaryDto>>().content
    }.getOrDefault(emptyList())

    suspend fun getEvIdsBySnuttIds(snuttIds: List<String>): List<SnuttEvLectureSummaryDto> = runCatching {
        snuttEvWebClient.get().uri { builder ->
            builder.path("/v1/lectures/ids")
                .queryParam("semesterLectureSnuttIds", snuttIds.joinToString(","))
                .build()
        }
            .retrieve()
            .awaitBody<ListResponse<SnuttEvLectureSummaryDto>>().content
    }.getOrDefault(emptyList())
}
