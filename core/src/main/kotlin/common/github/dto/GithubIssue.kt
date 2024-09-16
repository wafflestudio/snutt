package com.wafflestudio.snu4t.common.github.dto

data class GithubIssue(
    val title: String,
    val body: String,
    val labels: List<String> = listOf(),
)

fun GithubIssue(
    title: String,
    header: Map<String, Any>,
    body: String,
    labels: List<String>,
) = GithubIssue(
    title = title,
    body = header.map { "${it.key}: ${it.value}" }.joinToString("\n") + "\n\n$body",
    labels = labels,
)
