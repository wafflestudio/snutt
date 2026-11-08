package com.wafflestudio.snutt.common.push

import com.wafflestudio.snutt.config.PhaseUtils
import java.net.URLEncoder

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
    DIARY("diary?lectureId=%s&courseTitle=%s"),
    ;

    fun build(
        vararg params: Any,
        referrer: String? = null,
        pushId: String? = null,
    ): Deeplink {
        val phase = PhaseUtils.getPhase()
        val protocol = if (phase.isProd) "snutt" else "snutt-dev"

        val baseScheme = "$protocol://${url.format(*params)}"
        val fullScheme =
            if (pushId.isNullOrBlank()) {
                baseScheme
            } else {
                val separator = if (baseScheme.contains('?')) "&" else "?"
                "$baseScheme${separator}push_id=${URLEncoder.encode(pushId, "UTF-8")}"
            }

        if (referrer.isNullOrBlank()) {
            return Deeplink(fullScheme)
        }

        return Deeplink("$fullScheme?referrer=$referrer")
    }
}
