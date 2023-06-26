package com.wafflestudio.snu4t.notification.data

import com.wafflestudio.snu4t.common.client.AppType
import com.wafflestudio.snu4t.common.client.OsType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document("user_devices")
data class UserDevice(
    @Id
    val id: String? = null,
    @Indexed
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    val userId: String,
    @Field("os_type")
    var osType: OsType,
    @Field("os_version")
    var osVersion: String?,
    @Indexed
    @Field("device_id")
    var deviceId: String?,
    @Field("device_model")
    var deviceModel: String?,
    @Field("app_type")
    var appType: AppType?,
    @Field("app_version")
    var appVersion: String?,
    @Indexed
    @Field("fcm_registration_id")
    var fcmRegistrationId: String,
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Field("updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Field("is_deleted")
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
