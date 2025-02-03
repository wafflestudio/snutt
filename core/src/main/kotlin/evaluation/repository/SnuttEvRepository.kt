package com.wafflestudio.snutt.evaluation.repository

import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.config.SnuttEvWebClient
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureIdDto
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureSummaryDto
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.awaitBody

@Component
class SnuttEvRepository(private val snuttEvWebClient: SnuttEvWebClient) {
    suspend fun getSummariesByIds(ids: List<String>): List<SnuttEvLectureSummaryDto> =
        runCatching {
            snuttEvWebClient.get().uri { builder ->
                builder.path("/v1/lectures/snutt-summary")
                    .queryParam("semesterLectureSnuttIds", ids.joinToString(","))
                    .build()
            }
                .retrieve()
                .awaitBody<ListResponse<SnuttEvLectureSummaryDto>>().content
        }.getOrDefault(emptyList())

    suspend fun getEvIdsBySnuttIds(snuttIds: List<String>): List<SnuttEvLectureIdDto> =
        runCatching {
            snuttEvWebClient.get().uri { builder ->
                builder.path("/v1/lectures/ids")
                    .queryParam("semesterLectureSnuttIds", snuttIds.joinToString(","))
                    .build()
            }
                .retrieve()
                .awaitBody<ListResponse<SnuttEvLectureIdDto>>().content
        }.getOrDefault(emptyList())
}
