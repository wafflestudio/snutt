package com.wafflestudio.snu4t.feedback.service

import com.wafflestudio.snu4t.common.client.AppVersion
import com.wafflestudio.snu4t.common.client.OsType
import com.wafflestudio.snu4t.common.github.client.GithubRestApiClient
import com.wafflestudio.snu4t.common.github.dto.GithubIssue
import com.wafflestudio.snu4t.config.PhaseUtils
import com.wafflestudio.snu4t.feedback.dto.FeedbackDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

interface FeedbackService {
    suspend fun addGithubIssue(
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
    @Value("\${github.feedback.token}") private val token: String,
    @Value("\${github.feedback.repo.owner}") private val repoOwner: String,
    @Value("\${github.feedback.repo.name}") private val repoName: String,
    private val githubRestApiClient: GithubRestApiClient,
) : FeedbackService {
    override suspend fun addGithubIssue(
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
        val profileName = PhaseUtils.getPhase().name.lowercase()
        githubRestApiClient.addRepoIssue(
            repoOwner = repoOwner,
            repoName = repoName,
            token = token,
            issue =
                GithubIssue(
                    title = message,
                    body =
                        FeedbackDto(
                            email = email,
                            platform = platform,
                            appVersion = appVersion.toString(),
                            deviceModel = deviceModel,
                            currentSeoulTime = currentSeoulTime,
                            profileName = profileName,
                            message = message,
                        ).toGithubIssueBody(),
                    labels = listOf(osTypeString),
                ),
        )
    }
}
