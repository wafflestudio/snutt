package com.wafflestudio.snutt.common.github.client

import com.wafflestudio.snutt.common.extension.post
import com.wafflestudio.snutt.common.github.api.GithubRestApi
import com.wafflestudio.snutt.common.github.dto.GithubIssue
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Service

interface GithubRestApiClient {
    suspend fun addRepoIssue(
        repoOwner: String,
        repoName: String,
        token: String,
        issue: GithubIssue,
    )
}

@Service
class GithubRestApiClientImpl(
    private val githubRestApi: GithubRestApi,
) : GithubRestApiClient {
    override suspend fun addRepoIssue(
        repoOwner: String,
        repoName: String,
        token: String,
        issue: GithubIssue,
    ) {
        githubRestApi.post<Any>(
            uri = "/repos/$repoOwner/$repoName/issues",
            body = issue,
            headers =
                mapOf(
                    AUTHORIZATION to "token $token",
                ),
        )
    }
}
