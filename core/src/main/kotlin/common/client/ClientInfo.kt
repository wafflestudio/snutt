package com.wafflestudio.snutt.common.client

data class ClientInfo(
    val osType: OsType,
    val osVersion: String?,
    val appType: AppType?,
    val appVersion: AppVersion?,
    val deviceId: String?,
    val deviceModel: String?,
)
