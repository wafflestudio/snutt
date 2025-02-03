package com.wafflestudio.snutt.users.data

data class Credential(
    var localId: String? = null,
    var localPw: String? = null,
    var fbId: String? = null,
    var fbName: String? = null,
    var appleSub: String? = null,
    var appleEmail: String? = null,
    var appleTransferSub: String? = null,
    var googleSub: String? = null,
    var googleEmail: String? = null,
    var kakaoSub: String? = null,
    var kakaoEmail: String? = null,
    var tempDate: String? = null,
    var tempSeed: String? = null,
)
