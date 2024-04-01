package com.wafflestudio.snu4t.evaluation.repository

import com.wafflestudio.snu4t.common.dto.ListResponse
import com.wafflestudio.snu4t.config.SnuttEvWebClient
import com.wafflestudio.snu4t.evaluation.dto.SnuttEvLectureSummaryDto
import org.springframework.web.reactive.function.client.awaitBody

class SnuttEvRepository(private val SnuttEvWebClient: SnuttEvWebClient) {
    suspend fun getSummariesByIds(ids: List<String>): List<SnuttEvLectureSummaryDto> =
        SnuttEvWebClient.get().uri { builder ->
            builder.path("/v1/lectures/snutt-summary")
                .queryParam("semesterLectureSnuttIds", ids)
                .build()
        }
            .retrieve()
            .awaitBody<ListResponse<SnuttEvLectureSummaryDto>>().content
}
