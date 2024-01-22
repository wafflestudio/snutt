package com.wafflestudio.snu4t.lecturebuildings.data

class PlaceInfo(
    val campus: Campus,
    val buildingNumber: String
)

fun PlaceInfo(place: String): PlaceInfo? = place
    .split("-").dropLast(1).joinToString("-")
    .ifBlank { null }
    ?.let {
        var (buildingNumber, campus) = when (it.first()) {
            '#' -> Pair(it.removePrefix("#"), Campus.YEONGEON)
            '*' -> Pair(it.removePrefix("*"), Campus.PYEONGCHANG)
            else -> Pair(it, Campus.GWANAK)
        }

        PlaceInfo(campus, buildingNumber.trimStart { it == '0' })
    }
