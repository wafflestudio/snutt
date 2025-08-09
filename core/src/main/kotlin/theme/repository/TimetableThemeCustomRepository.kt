package com.wafflestudio.snutt.theme.repository

import com.wafflestudio.snutt.common.extension.desc
import com.wafflestudio.snutt.common.extension.isEqualTo
import com.wafflestudio.snutt.common.extension.regex
import com.wafflestudio.snutt.theme.data.ThemeMarketInfo
import com.wafflestudio.snutt.theme.data.ThemeOrigin
import com.wafflestudio.snutt.theme.data.ThemeStatus
import com.wafflestudio.snutt.theme.data.TimetableTheme
import com.wafflestudio.snutt.theme.dto.TimetableThemeDto
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.div
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.aggregation.ConvertOperators
import org.springframework.data.mongodb.core.aggregation.TypedAggregation
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.regex
import org.springframework.data.mongodb.core.updateFirst

interface TimetableThemeCustomRepository {
    suspend fun findLastChild(
        userId: String,
        name: String,
    ): TimetableTheme?

    suspend fun findPublishedTimetablesByPublishNameContaining(name: String): List<TimetableTheme>

    suspend fun findPublishedTimetablesOrderByDownloadsDesc(page: Int): List<TimetableTheme>

    suspend fun existsByOriginIdAndUserId(
        originId: String,
        userId: String,
    ): Boolean

    suspend fun addDownloadCount(id: String)

    suspend fun findOriginalThemesByUserIds(
        userIds: List<String>,
        page: Int,
    ): List<TimetableTheme>
}

class TimetableThemeCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : TimetableThemeCustomRepository {
    companion object {
        private const val EFFECTIVE_THEME_ID_ALIAS = "effectiveThemeId"
        private const val THEME_DETAILS_ALIAS = "themeDetails"
        private const val DOWNLOADED_THEMES_ALIAS = "downloadedThemes"
        private const val PUBLISHED_THEMES_ALIAS = "publishedThemes"
        private const val COMBINED_THEMES_ALIAS = "combinedThemes"
        private const val DOC_ALIAS = "doc"
        private const val ID_FIELD = "_id"
    }

    override suspend fun findLastChild(
        userId: String,
        name: String,
    ): TimetableTheme? =
        reactiveMongoTemplate
            .findOne(
                Query
                    .query(
                        TimetableTheme::userId isEqualTo userId and
                            TimetableTheme::name regex """^${Regex.escape(name)}(\s+\(\d+\))?$""",
                    ).with(TimetableTheme::name.desc()),
                TimetableTheme::class.java,
            ).awaitSingleOrNull()

    override suspend fun findPublishedTimetablesByPublishNameContaining(name: String): List<TimetableTheme> =
        reactiveMongoTemplate
            .find<TimetableTheme>(
                Query
                    .query(
                        (TimetableTheme::publishInfo / ThemeMarketInfo::publishName)
                            .regex(""".*${Regex.escape(name)}.*"""),
                    ).with(TimetableTheme::name.desc()),
            ).collectList()
            .awaitSingle()

    override suspend fun findPublishedTimetablesOrderByDownloadsDesc(page: Int): List<TimetableTheme> =
        reactiveMongoTemplate
            .find<TimetableTheme>(
                Query
                    .query(
                        TimetableTheme::status isEqualTo ThemeStatus.PUBLISHED,
                    ).with((TimetableTheme::publishInfo / ThemeMarketInfo::downloads).desc()),
            ).skip((page - 1) * 10L)
            .take(10)
            .collectList()
            .awaitSingle()

    override suspend fun existsByOriginIdAndUserId(
        originId: String,
        userId: String,
    ): Boolean =
        reactiveMongoTemplate
            .exists<TimetableTheme>(
                Query.query(
                    (TimetableTheme::origin / ThemeOrigin::originId) isEqualTo originId and
                        TimetableTheme::userId isEqualTo userId,
                ),
            ).awaitSingle()

    override suspend fun addDownloadCount(id: String) {
        reactiveMongoTemplate
            .updateFirst<TimetableTheme>(
                Query.query(TimetableTheme::id isEqualTo id),
                Update().inc((TimetableTheme::publishInfo / ThemeMarketInfo::downloads).toDotPath(), 1),
            ).awaitSingle()
    }

    override suspend fun findOriginalThemesByUserIds(
        userIds: List<String>,
        page: Int,
    ): List<TimetableTheme> {
        val downloadedOriginStages =
            arrayOf(
                Aggregation.match(
                    TimetableThemeDto::userId
                        .inValues(userIds)
                        .and(TimetableThemeDto::status)
                        .isEqualTo(ThemeStatus.DOWNLOADED),
                ),
                Aggregation
                    .project()
                    .and(ConvertOperators.ToObjectId.toObjectId("$${TimetableTheme::origin.div(ThemeOrigin::originId).toDotPath()}"))
                    .`as`(EFFECTIVE_THEME_ID_ALIAS),
                Aggregation
                    .lookup()
                    .from(reactiveMongoTemplate.getCollectionName(TimetableTheme::class.java))
                    .localField(EFFECTIVE_THEME_ID_ALIAS)
                    .foreignField(ID_FIELD)
                    .`as`(THEME_DETAILS_ALIAS),
                Aggregation.unwind(THEME_DETAILS_ALIAS),
                Aggregation.replaceRoot(THEME_DETAILS_ALIAS),
            )
        val publishedStages =
            arrayOf(
                Aggregation.match(
                    TimetableTheme::userId
                        .inValues(userIds)
                        .and(TimetableTheme::status)
                        .isEqualTo(ThemeStatus.PUBLISHED),
                ),
            )
        val facetOperation =
            Aggregation
                .facet()
                .and(*downloadedOriginStages)
                .`as`(DOWNLOADED_THEMES_ALIAS)
                .and(*publishedStages)
                .`as`(PUBLISHED_THEMES_ALIAS)
        val combinedAggregation =
            TypedAggregation.newAggregation(
                TimetableTheme::class.java,
                facetOperation,
                Aggregation
                    .project()
                    .and(ArrayOperators.ConcatArrays.arrayOf("$$DOWNLOADED_THEMES_ALIAS").concat("$$PUBLISHED_THEMES_ALIAS"))
                    .`as`(COMBINED_THEMES_ALIAS),
                Aggregation.unwind("$$COMBINED_THEMES_ALIAS"),
                Aggregation.replaceRoot("$$COMBINED_THEMES_ALIAS"),
                Aggregation
                    .group(ID_FIELD)
                    .first(Aggregation.ROOT)
                    .`as`(DOC_ALIAS),
                Aggregation.replaceRoot("$$DOC_ALIAS"),
                Aggregation.sort(
                    Sort.Direction.DESC,
                    TimetableTheme::publishInfo.div(ThemeMarketInfo::downloads).toDotPath(),
                ),
            )
        return reactiveMongoTemplate
            .aggregate(
                combinedAggregation,
                TimetableTheme::class.java,
            ).skip((page - 1) * 10L)
            .take(10)
            .collectList()
            .awaitSingle()
    }
}
