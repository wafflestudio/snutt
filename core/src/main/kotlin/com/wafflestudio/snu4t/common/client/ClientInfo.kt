package com.wafflestudio.snu4t.common.client

data class ClientInfo(
    val osType: OsType = OsType.UNKNOWN,
    val osVersion: String? = null,
    val appType: AppType? = null,
    val appVersion: String? = null,
)
