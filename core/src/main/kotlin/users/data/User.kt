package com.wafflestudio.snu4t.users.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.notification.data.UserDevice
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("users")
data class User(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    var email: String?,
    @TextIndexed
    var nickname: String?,
    var isEmailVerified: Boolean?,
    var credential: Credential,
    var credentialHash: String,
    /**
     * Legacy FCM 기기 그룹 관련 API 를 이용하던 당시의 값. 유효한 [UserDevice] 가 존재하는 경우, 해당 유저에 대해서는 사용하지 않음.
     */
    var fcmKey: String?,
    var active: Boolean = true,
    var isAdmin: Boolean = false,
    val regDate: LocalDateTime = LocalDateTime.now(),
    var lastLoginTimestamp: Long = System.currentTimeMillis(),
    var notificationCheckedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun getNicknameTag(): Int? {
        return nickname
            ?.split(NICKNAME_TAG_DELIMITER)
            ?.last()
            ?.toInt()
    }

    companion object {
        const val NICKNAME_TAG_DELIMITER = "#"
    }
}
