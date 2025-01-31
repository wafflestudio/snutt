package com.wafflestudio.snutt.common.storage

import com.wafflestudio.snutt.config.PhaseUtils

enum class StorageType(bucketName: String, val acl: Acl = Acl.PRIVATE) {
    ASSETS("snutt-asset", Acl.PUBLIC_READ),
    ;

    val bucketName = bucketName
        get(): String {
            val phase = PhaseUtils.getPhase()
            return if (phase.isProd) field else "$field$BUCKET_NAME_DEV_SUFFIX"
        }

    companion object {
        private const val BUCKET_NAME_DEV_SUFFIX = "-dev"

        fun from(bucketName: String): StorageType {
            return entries.first { it.bucketName == bucketName }
        }
    }
}
