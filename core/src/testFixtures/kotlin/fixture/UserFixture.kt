package com.wafflestudio.snu4t.fixture

import com.wafflestudio.snu4t.users.data.Credential
import com.wafflestudio.snu4t.users.data.User
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
