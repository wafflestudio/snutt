package com.wafflestudio.snutt.users.dto

import com.wafflestudio.snutt.auth.AuthProvider
import com.wafflestudio.snutt.users.data.User
import java.time.LocalDateTime

data class AdminUserSearchResponse(
    val id: String,
    val email: String?,
    val isEmailVerified: Boolean?,
    val nickname: String,
    val localId: String?,
    val isAdmin: Boolean,
    val active: Boolean,
    val regDate: LocalDateTime,
    val lastLoginTimestamp: Long,
    val authProviders: List<AuthProvider>,
    val socialAccounts: SocialAccounts,
) {
    data class SocialAccounts(
        val googleEmail: String?,
        val kakaoEmail: String?,
        val appleEmail: String?,
        val facebookName: String?,
    )

    companion object {
        fun from(user: User): AdminUserSearchResponse =
            AdminUserSearchResponse(
                id = user.id!!,
                email = user.email,
                isEmailVerified = user.isEmailVerified,
                nickname = user.nickname,
                localId = user.credential.localId,
                isAdmin = user.isAdmin,
                active = user.active,
                regDate = user.regDate,
                lastLoginTimestamp = user.lastLoginTimestamp,
                authProviders =
                    listOfNotNull(
                        user.credential.localId?.let { AuthProvider.LOCAL },
                        user.credential.fbId?.let { AuthProvider.FACEBOOK },
                        user.credential.googleSub?.let { AuthProvider.GOOGLE },
                        user.credential.kakaoSub?.let { AuthProvider.KAKAO },
                        user.credential.appleSub?.let { AuthProvider.APPLE },
                    ),
                socialAccounts =
                    SocialAccounts(
                        googleEmail = user.credential.googleEmail,
                        kakaoEmail = user.credential.kakaoEmail,
                        appleEmail = user.credential.appleEmail,
                        facebookName = user.credential.fbName,
                    ),
            )
    }
}
