package com.wafflestudio.snu4t.common.client

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

data class AppVersion(val appVersion: String) : Comparable<AppVersion> {
    override fun compareTo(other: AppVersion): Int {
        val thisVersion = this.toInts()
        val otherVersion = other.toInts()
        val len = maxOf(thisVersion.size, otherVersion.size)

        for (i in 0 until len) {
            val thisPart = thisVersion.getOrElse(i) { 0 }
            val otherPart = otherVersion.getOrElse(i) { 0 }
            when {
                thisPart < otherPart -> return -1
                thisPart > otherPart -> return 1
            }
        }
        return 0
    }

    private fun toInts() = appVersion.split(".").map { it.takeWhile { c -> c.isDigit() }.toInt() }

    override fun toString(): String = appVersion
}

@ReadingConverter
@Component
class AppVersionReadConverter : Converter<String, AppVersion> {
    override fun convert(source: String): AppVersion = AppVersion(source)
}

@Component
@WritingConverter
class AppVersionWriteConverter : Converter<AppVersion, String> {
    override fun convert(source: AppVersion): String = source.appVersion
}
