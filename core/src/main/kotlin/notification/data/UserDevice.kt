package com.wafflestudio.snu4t.notification.data

import com.wafflestudio.snu4t.common.client.AppType
import com.wafflestudio.snu4t.common.client.AppVersion
import com.wafflestudio.snu4t.common.client.OsType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document
data class UserDevice(
    @Id
    val id: String? = null,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    @Field
    var osType: OsType,
    @Field
    var osVersion: String?,
    @Indexed
    @Field
    var deviceId: String?,
    @Field
    var deviceModel: String?,
    @Field
    var appType: AppType?,
    @Field
    var appVersion: AppVersion?,
    @Indexed
    @Field
    var fcmRegistrationId: String,
    @Field
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Field
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Field
    var isDeleted: Boolean = false,
) {
    companion object {
        fun of(userId: String, fcmGroupKey: String): UserDevice =
            UserDevice(
                userId = userId,
                osType = OsType.UNKNOWN,
                osVersion = null,
                deviceId = null,
                deviceModel = null,
                appType = null,
                appVersion = null,
                fcmRegistrationId = fcmGroupKey,
            )
    }
}
