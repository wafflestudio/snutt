package com.wafflestudio.snu4t.common.enum

enum class TimetableTheme(val value: Int) {
    SNUTT(0),
    FALL(1),
    MODERN(2),
    CHERRY_BLOSSOM(3),
    ICE(4),
    LAWN(5),
    ;

    companion object {
        private val valueMap = TimetableTheme.values().associateBy { e -> e.value }
        fun from(value: Int) = valueMap[value]
    }
}
