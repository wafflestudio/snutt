package com.wafflestudio.snu4t.theme.repository

import com.wafflestudio.snu4t.common.extension.desc
import com.wafflestudio.snu4t.common.extension.isEqualTo
import com.wafflestudio.snu4t.common.extension.regex
import com.wafflestudio.snu4t.theme.data.ThemeMarketInfo
import com.wafflestudio.snu4t.theme.data.ThemeOrigin
import com.wafflestudio.snu4t.theme.data.ThemeStatus
import com.wafflestudio.snu4t.theme.data.TimetableTheme
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
import org.springframework.data.mongodb.core.query.regex
import org.springframework.data.mongodb.core.updateFirst

interface TimetableThemeCustomRepository {
    suspend fun findLastChild(
        userId: String,
        name: String,
    ): TimetableTheme?
    suspend fun findPublishedTimetablesByPublishNameContaining(name: String): List<TimetableTheme>
    suspend fun findPublishedTimetablesOrderByDownloadsDesc(page: Int): List<TimetableTheme>
    suspend fun existsByOriginId(originId: String): Boolean
    suspend fun addDownloadCount(id: String)
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
                    TimetableTheme::name regex """${Regex.escape(name)}(\s+\(\d+\))?""",
            ).with(TimetableTheme::name.desc()),
            TimetableTheme::class.java,
        ).awaitSingleOrNull()
    }

    override suspend fun findPublishedTimetablesByPublishNameContaining(name: String): List<TimetableTheme> {
        return reactiveMongoTemplate.find<TimetableTheme>(
            Query.query(
                (TimetableTheme::publishInfo / ThemeMarketInfo::publishName)
                    .regex(""".*${Regex.escape(name)}.*""")
            ).with(TimetableTheme::name.desc()),
        ).collectList().awaitSingle()
    }

    override suspend fun findPublishedTimetablesOrderByDownloadsDesc(page: Int): List<TimetableTheme> {
        return reactiveMongoTemplate.find<TimetableTheme>(
            Query.query(
                TimetableTheme::status isEqualTo ThemeStatus.PUBLISHED
            ).with((TimetableTheme::publishInfo / ThemeMarketInfo::downloads).desc())
        ).skip((page - 1) * 10L).take(10).collectList().awaitSingle()
    }

    override suspend fun existsByOriginId(originId: String): Boolean =
        reactiveMongoTemplate.exists<TimetableTheme>(
            Query.query((TimetableTheme::origin / ThemeOrigin::originId) isEqualTo originId)
        ).awaitSingle()

    override suspend fun addDownloadCount(id: String) {
        reactiveMongoTemplate.updateFirst<TimetableTheme>(
            Query.query(TimetableTheme::id isEqualTo id),
            Update().inc((TimetableTheme::publishInfo / ThemeMarketInfo::downloads).toDotPath(), 1),
        ).awaitSingle()
    }
}
