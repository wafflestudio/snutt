package com.wafflestudio.snu4t.feedback.dto

data class FeedbackDto(
    val email: String,
    val platform: String,
    val appVersion: String,
    val deviceModel: String,
    val currentSeoulTime: String,
    val message: String,
) {
    fun toGithubIssueBody(): String {
        val header =
            mapOf(
                "이메일" to "`$email`",
                "플랫폼" to platform,
                "버전" to appVersion,
                "디바이스" to deviceModel,
                "날짜/시간(UTC+9)" to currentSeoulTime,
            )
        return header.map { "${it.key}: ${it.value}" }.joinToString("\n") + "\n\n$message"
    }
}
