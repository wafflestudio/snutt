package com.wafflestudio.snu4t.timetables.utils

object ColorUtils {
    const val MAX_COLOR_INDEX: Int = 9

    fun getLeastUsedColorIndexByRandom(alreadyUsedIndexes: Iterable<Int>): Int {
        val colorCount = (alreadyUsedIndexes + (1..MAX_COLOR_INDEX)).groupingBy { it }.eachCount()
        val minCount = colorCount.minOf { it.value }
        return colorCount.entries.filter { (color, count) -> count == minCount }.map { it.key }.random()
    }
}
