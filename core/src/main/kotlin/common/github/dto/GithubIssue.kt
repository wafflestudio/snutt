package com.wafflestudio.snutt.common.github.dto

data class GithubIssue(
    val title: String,
    val body: String,
    val labels: List<String> = listOf(),
)
