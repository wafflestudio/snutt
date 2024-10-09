package com.wafflestudio.snu4t.feedback.dto

data class FeedbackDto(
    val email: String,
    val platform: String,
    val appVersion: String,
    val deviceModel: String,
    val currentSeoulTime: String,
    val profileName: String,
    val message: String,
) {
    fun toGithubIssueBody() =
        """
        이메일: `$email`
        플랫폼/디바이스: $platform / $deviceModel
        버전: $appVersion
        프로필: $profileName
        날짜/시간(UTC+9): $currentSeoulTime
            
        $message
        """.trimIndent()
}
