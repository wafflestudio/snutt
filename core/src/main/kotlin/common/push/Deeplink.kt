package com.wafflestudio.snutt.common.push

import com.wafflestudio.snutt.config.PhaseUtils

@JvmInline
value class Deeplink(
    val value: String,
)

enum class DeeplinkType(
    private val url: String,
) {
    NOTIFICATIONS("notifications"),
    VACANCY("vacancy"),
    FRIENDS("friends?openDrawer=true"),
    TIMETABLE_LECTURE("timetable-lecture?timetableId=%s&lectureId=%s"),
    BOOKMARKS("bookmarks?year=%s&semester=%s&lectureId=%s"),
    DIARY("diary?timetableId=%s&lectureId=%s"),
    ;

    fun build(
        vararg params: Any,
        referrer: String? = null,
    ): Deeplink {
        val phase = PhaseUtils.getPhase()
        val protocol = if (phase.isProd) "snutt" else "snutt-dev"

        val fullScheme = "$protocol://${url.format(*params)}"

        if (referrer.isNullOrBlank()) {
            return Deeplink(fullScheme)
        }

        return Deeplink("$fullScheme?referrer=$referrer")
    }
}
