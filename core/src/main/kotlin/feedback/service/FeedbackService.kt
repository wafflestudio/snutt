package com.wafflestudio.snu4t.feedback.service

import com.wafflestudio.snu4t.common.client.AppVersion
import com.wafflestudio.snu4t.common.client.OsType
import com.wafflestudio.snu4t.common.github.client.GithubRestApiClient
import com.wafflestudio.snu4t.common.github.dto.GithubIssue
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

interface FeedbackService {
    suspend fun add(
        email: String,
        message: String,
        osType: OsType,
        osVersion: String?,
        appVersion: AppVersion,
        deviceModel: String,
    )
}

@Service
class FeedbackServiceImpl(
    @Value("\${github.feedback.repo.owner}") private val repoOwner: String,
    @Value("\${github.feedback.repo.name}") private val repoName: String,
    private val githubRestApiClient: GithubRestApiClient,
) : FeedbackService {
    override suspend fun add(
        email: String,
        message: String,
        osType: OsType,
        osVersion: String?,
        appVersion: AppVersion,
        deviceModel: String,
    ) {
        val osTypeString = osType.toString().lowercase()
        val platform = osVersion?.let { "$osTypeString ($osVersion)" } ?: osTypeString
        val currentSeoulTime =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
                ZonedDateTime.ofInstant(
                    Instant.now(),
                    ZoneId.of("Asia/Seoul"),
                ),
            )
        githubRestApiClient.addRepoIssue(
            repoOwner = repoOwner,
            repoName = repoName,
            issue =
                GithubIssue(
                    title = message,
                    header =
                        mapOf(
                            "이메일" to "`$email`",
                            "플랫폼" to platform,
                            "버전" to appVersion,
                            "디바이스" to deviceModel,
                            "날짜/시간(UTC+9)" to currentSeoulTime,
                        ),
                    body = message,
                    labels = listOf(osTypeString),
                ),
        )
    }
}
