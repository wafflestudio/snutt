package com.wafflestudio.snu4t.common.storage

enum class StorageSource(val value: String, val storageType: StorageType, val path: String? = null) {
    POPUP("popup", StorageType.ASSETS, "popup-images"),
    ;

    companion object {
        fun from(value: String): StorageSource? {
            return entries.find { it.value == value }
        }
    }
}
