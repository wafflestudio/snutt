package com.wafflestudio.snu4t.sugangsnu.utils

import com.wafflestudio.snu4t.common.enum.DayOfWeek
import com.wafflestudio.snu4t.lectures.data.ClassTime
import com.wafflestudio.snu4t.sugangsnu.SugangSnuClassTime
import org.slf4j.LoggerFactory

object SugangSnuClassTimeUtils {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val classTimeRegEx =
        """/(?<day>[월화수목금토일])\((?<startHour>\d{2}):(?<startMinute>\d{2})~(?<endHour>\d{2}):(?<endMinute>\d{2})\)/"""
            .toRegex()

    fun convertTextToClassTimeObject(classTimesText: String, locationsText: String): List<ClassTime> = runCatching {
        val sugangSnuClassTimes = classTimesText.split("/").map(::parseSugangSnuClassTime)
        val locationTexts = locationsText.split("/").let { locationText ->
            when (locationText.size) {
                sugangSnuClassTimes.size -> locationText
                1 -> List(sugangSnuClassTimes.size) { locationText.first() }
                0 -> List(sugangSnuClassTimes.size) { "" }
                else -> throw RuntimeException("locations does not match with times")
            }
        }
        sugangSnuClassTimes.zip(locationTexts)
            .map { (sugangSnuClassTime, locationText) ->
                ClassTime(
                    day = DayOfWeek.getByKoreanText(sugangSnuClassTime.dayOfWeek)!!,
                    startTime = "${sugangSnuClassTime.startHour}:${sugangSnuClassTime.startMinute}",
                    endTime = "${sugangSnuClassTime.endHour}:${sugangSnuClassTime.endMinute}",
                    periodLength = sugangSnuClassTime.endPeriod - sugangSnuClassTime.startPeriod,
                    startPeriod = sugangSnuClassTime.startPeriod,
                    place = locationText,
                )
            }
    }.getOrElse {
        logger.error("classtime으로 변환 실패 (time: {}, location: {})", classTimesText, locationsText)
        emptyList()
    }

    // 교시 기준으로 변환 ( 구버전 호환 필드용 - classTimeText )
    fun convertClassTimeTextToPeriodText(classTimeText: String): String =
        classTimeText.split('/')
            .filter { it.isNotEmpty() && classTimeRegEx.matches(it) }
            .joinToString("/") { classTime ->
                runCatching {
                    with(parseSugangSnuClassTime(classTime)) {
                        "$dayOfWeek($startPeriod-${endPeriod - startPeriod})"
                    }
                }.getOrElse {
                    logger.error("교시 파싱 에러 {}", classTimeText)
                    ""
                }
            }


    private fun parseSugangSnuClassTime(classTime: String): SugangSnuClassTime {
        val matchResult = classTimeRegEx.find(classTime)!!.groups

        return SugangSnuClassTime(
            dayOfWeek = matchResult["day"]!!.value,
            startHour = matchResult["startHour"]!!.value,
            startMinute = matchResult["startMinute"]!!.value,
            endHour = matchResult["endHour"]!!.value,
            endMinute = matchResult["endMinute"]!!.value,
        )
    }
}
