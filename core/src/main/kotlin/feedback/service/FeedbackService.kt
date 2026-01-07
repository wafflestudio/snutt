package com.wafflestudio.snutt.feedback.service

import com.wafflestudio.snutt.common.client.AppVersion
import com.wafflestudio.snutt.common.client.OsType
import com.wafflestudio.snutt.common.github.client.GithubRestApiClient
import com.wafflestudio.snutt.common.github.dto.GithubIssue
import com.wafflestudio.snutt.config.PhaseUtils
import com.wafflestudio.snutt.feedback.dto.FeedbackDto
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
    @param:Value("\${github.feedback.token}") private val token: String,
    @param:Value("\${github.feedback.repo.owner}") private val repoOwner: String,
    @param:Value("\${github.feedback.repo.name}") private val repoName: String,
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
