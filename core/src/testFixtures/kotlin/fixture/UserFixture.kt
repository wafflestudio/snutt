package com.wafflestudio.snutt.fixture

import com.wafflestudio.snutt.users.data.Credential
import com.wafflestudio.snutt.users.data.User
import org.springframework.stereotype.Component

@Component
class UserFixture {
    val testUser =
        User(
            id = "59e206efdc67c31ad5fa0d35",
            email = "snutt@wafflestudio.com",
            nickname = "",
            isEmailVerified = false,
            credential = Credential(localId = "", localPw = ""),
            credentialHash = "test",
            fcmKey = null,
        )
}
