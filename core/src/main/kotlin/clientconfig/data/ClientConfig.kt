package com.wafflestudio.snu4t.clientconfig.data

import com.wafflestudio.snu4t.common.client.AppVersion
import com.wafflestudio.snu4t.common.client.OsType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document
data class ClientConfig(
    @Id
    val id: String? = null,
    @Indexed
    @Field
    val name: String,
    @Field
    val value: String,
    @Field
    val minIosVersion: AppVersion?,
    @Field
    val minAndroidVersion: AppVersion?,
    @Field
    val maxIosVersion: AppVersion?,
    @Field
    val maxAndroidVersion: AppVersion?,
    @Field
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Field
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun isAdaptable(osType: OsType, appVersion: AppVersion): Boolean {
        return (osType == OsType.IOS && ((minIosVersion == null || appVersion >= minIosVersion) && (maxIosVersion == null || appVersion <= maxIosVersion))) ||
            (osType == OsType.ANDROID && ((minAndroidVersion == null || appVersion >= minAndroidVersion) && (maxAndroidVersion == null || appVersion <= maxAndroidVersion)))
    }
}
