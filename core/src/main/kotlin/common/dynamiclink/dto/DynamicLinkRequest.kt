package com.wafflestudio.snu4t.common.dynamiclink.dto

data class DynamicLinkRequest(
    val dynamicLinkInfo: DynamicLinkInfo,
    val suffix: Suffix? = Suffix(),
)

data class DynamicLinkInfo(
    val domainUriPrefix: String,
    val link: String,
    val androidInfo: AndroidInfo,
    val iosInfo: IosInfo,
    val navigationInfo: NavigationInfo? = NavigationInfo(),
    val socialMetaTagInfo: SocialMetaTagInfo? = null,
)

data class AndroidInfo(
    val androidPackageName: String,
    val androidFallbackLink: String? = null,
    val androidMinPackageVersionCode: String? = null,
)

data class IosInfo(
    val iosBundleId: String,
    val iosAppStoreId: String? = null,
    val iosCustomScheme: String? = null,
    val iosFallbackLink: String? = null,
    val iosIpadBundleId: String? = null,
    val iosIpadFallbackLink: String? = null,
)

data class SocialMetaTagInfo(
    val socialTitle: String?,
    val socialDescription: String?,
    val socialImageLink: String?,
)

data class NavigationInfo(
    val enableForcedRedirect: Boolean = false,
)

data class Suffix(
    val option: SuffixOption = SuffixOption.SHORT
)

enum class SuffixOption {
    SHORT, UNGUESSABLE
}
