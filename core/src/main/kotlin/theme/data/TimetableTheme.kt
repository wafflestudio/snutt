package com.wafflestudio.snutt.theme.data

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document
@CompoundIndex(def = "{ 'userId': 1, 'name': 1 }", unique = true)
data class TimetableTheme(
    @Id
    var id: String? = null,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    var name: String,
    // basic 테마는 null (클라이언트 처리)
    var colors: List<ColorSet>?,
    // basic 테마는 false
    val isCustom: Boolean,
    var origin: ThemeOrigin? = null,
    var status: ThemeStatus = if (isCustom) ThemeStatus.PRIVATE else ThemeStatus.BASIC,
    var publishInfo: ThemeMarketInfo? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @Transient // iOS, Android 3.5.0 버전 대응하는 레거시 로직을 위해서만 남아있고 실제 DB 에선 제거한 필드
    var isDefault: Boolean = false
}

data class ThemeMarketInfo(
    val publishName: String,
    val authorAnonymous: Boolean,
    var downloads: Int,
)

data class ThemeOrigin(
    val originId: String,
    val authorId: String,
)

enum class ThemeStatus {
    BASIC, // 기본 테마
    DOWNLOADED, // 다운로드 받은 테마
    PUBLISHED, // 공유된 테마
    PRIVATE, // 개인 테마
}
