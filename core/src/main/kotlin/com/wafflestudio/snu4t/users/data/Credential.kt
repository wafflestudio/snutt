package com.wafflestudio.snu4t.users.data

data class Credential(
        var appleEmail: String?,
        var appleSub: String?,
        var appleTransferSub: String?,
        var fbId: String?,
        var fbName: String?,
        var localId: String?,
        var localPw: String?,
        var tempDate: String?,
        var tempSeed: String?,
)