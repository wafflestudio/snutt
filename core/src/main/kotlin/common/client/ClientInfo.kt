package com.wafflestudio.snu4t.common.client

data class ClientInfo(
    val osType: OsType,
    val osVersion: String?,
    val appType: AppType?,
    val appVersion: String?,
    val deviceId: String?,
    val deviceModel: String?,
)
