package com.wafflestudio.snutt.theme.repository

import com.wafflestudio.snutt.common.extension.desc
import com.wafflestudio.snutt.common.extension.isEqualTo
import com.wafflestudio.snutt.common.extension.regex
import com.wafflestudio.snutt.theme.data.ThemeMarketInfo
import com.wafflestudio.snutt.theme.data.ThemeOrigin
import com.wafflestudio.snutt.theme.data.ThemeStatus
import com.wafflestudio.snutt.theme.data.TimetableTheme
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.mapping.div
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
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

    suspend fun findOriginalThemesByUserIds(userIds: List<String>): List<TimetableTheme>
}

class TimetableThemeCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : TimetableThemeCustomRepository {
    override suspend fun findLastChild(
        userId: String,
        name: String,
    ): TimetableTheme? {
        return reactiveMongoTemplate.findOne(
            Query.query(
                TimetableTheme::userId isEqualTo userId and
                    TimetableTheme::name regex """^${Regex.escape(name)}(\s+\(\d+\))?$""",
            ).with(TimetableTheme::name.desc()),
            TimetableTheme::class.java,
        ).awaitSingleOrNull()
    }

    override suspend fun findPublishedTimetablesByPublishNameContaining(name: String): List<TimetableTheme> {
        return reactiveMongoTemplate.find<TimetableTheme>(
            Query.query(
                (TimetableTheme::publishInfo / ThemeMarketInfo::publishName)
                    .regex(""".*${Regex.escape(name)}.*"""),
            ).with(TimetableTheme::name.desc()),
        ).collectList().awaitSingle()
    }

    override suspend fun findPublishedTimetablesOrderByDownloadsDesc(page: Int): List<TimetableTheme> {
        return reactiveMongoTemplate.find<TimetableTheme>(
            Query.query(
                TimetableTheme::status isEqualTo ThemeStatus.PUBLISHED,
            ).with((TimetableTheme::publishInfo / ThemeMarketInfo::downloads).desc()),
        ).skip((page - 1) * 10L).take(10).collectList().awaitSingle()
    }

    override suspend fun existsByOriginIdAndUserId(
        originId: String,
        userId: String,
    ): Boolean =
        reactiveMongoTemplate.exists<TimetableTheme>(
            Query.query(
                (TimetableTheme::origin / ThemeOrigin::originId) isEqualTo originId and
                    TimetableTheme::userId isEqualTo userId,
            ),
        ).awaitSingle()

    override suspend fun addDownloadCount(id: String) {
        reactiveMongoTemplate.updateFirst<TimetableTheme>(
            Query.query(TimetableTheme::id isEqualTo id),
            Update().inc((TimetableTheme::publishInfo / ThemeMarketInfo::downloads).toDotPath(), 1),
        ).awaitSingle()
    }

    override suspend fun findOriginalThemesByUserIds(userIds: List<String>): List<TimetableTheme> {
        val publishedThemeIds =
            reactiveMongoTemplate.find<TimetableTheme>(
                Query.query(
                    TimetableTheme::userId.inValues(userIds) and TimetableTheme::status isEqualTo ThemeStatus.PUBLISHED,
                ),
            ).collectList().awaitSingle().map { theme -> theme.id }

        val sourceIds =
            reactiveMongoTemplate.find<TimetableTheme>(
                Query.query(
                    TimetableTheme::userId.inValues(userIds) and TimetableTheme::status isEqualTo ThemeStatus.DOWNLOADED,
                ),
            ).collectList().awaitSingle().mapNotNull { theme -> theme.origin?.originId }

        val ids = (publishedThemeIds + sourceIds).toSet()

        return reactiveMongoTemplate.find<TimetableTheme>(
            Query.query(
                TimetableTheme::id.inValues(ids),
            ).with((TimetableTheme::publishInfo / ThemeMarketInfo::downloads).desc()),
        ).collectList().awaitSingle()
    }
}
