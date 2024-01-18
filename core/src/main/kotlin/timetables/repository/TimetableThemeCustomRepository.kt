package com.wafflestudio.snu4t.timetables.repository

import com.wafflestudio.snu4t.common.extension.desc
import com.wafflestudio.snu4t.common.extension.isEqualTo
import com.wafflestudio.snu4t.common.extension.regex
import com.wafflestudio.snu4t.timetables.data.TimetableTheme
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.and

interface TimetableThemeCustomRepository {
    suspend fun findLastChild(userId: String, name: String): TimetableTheme?
}

class TimetableThemeCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : TimetableThemeCustomRepository {
    override suspend fun findLastChild(userId: String, name: String): TimetableTheme? {
        return reactiveMongoTemplate.findOne(
            Query.query(
                TimetableTheme::userId isEqualTo userId and
                    TimetableTheme::name regex """${Regex.escape(name)}(\s+\(\d+\))?"""
            ).with(TimetableTheme::name.desc()),
            TimetableTheme::class.java
        ).awaitSingleOrNull()
    }
}
