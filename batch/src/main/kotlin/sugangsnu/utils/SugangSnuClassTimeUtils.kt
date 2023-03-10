package com.wafflestudio.snu4t.sugangsnu.utils

import com.wafflestudio.snu4t.common.enum.DayOfWeek
import com.wafflestudio.snu4t.lectures.data.ClassTime
import com.wafflestudio.snu4t.sugangsnu.data.SugangSnuClassTime
import org.slf4j.LoggerFactory
import java.text.DecimalFormat

object SugangSnuClassTimeUtils {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val classTimeRegEx =
        """^(?<day>[월화수목금토일])\((?<startHour>\d{2}):(?<startMinute>\d{2})~(?<endHour>\d{2}):(?<endMinute>\d{2})\)$""".toRegex()
    private val periodFormat = DecimalFormat("#.#")

    fun convertTextToClassTimeObject(classTimesText: String, locationsText: String): List<ClassTime> = runCatching {
        val sugangSnuClassTimes = classTimesText.split("/")
            .filter { it.isNotBlank() }.map(::parseSugangSnuClassTime)
        val locationTexts = locationsText.split("/").filter { it.isNotBlank() }.let { locationText ->
            when (locationText.size) {
                sugangSnuClassTimes.size -> locationText
                1 -> List(sugangSnuClassTimes.size) { locationText.first() }
                0 -> List(sugangSnuClassTimes.size) { "" }
                else -> throw RuntimeException("locations does not match with times $classTimesText $locationsText")
            }
        }
        sugangSnuClassTimes.zip(locationTexts)
            .groupBy({ it.first }, { it.second })
            .map { (sugangSnuClassTime, locationTexts) ->
                ClassTime(
                    day = DayOfWeek.getByKoreanText(sugangSnuClassTime.dayOfWeek)!!,
                    startTime = "${sugangSnuClassTime.startHour}:${sugangSnuClassTime.startMinute}",
                    endTime = "${sugangSnuClassTime.endHour}:${sugangSnuClassTime.endMinute}",
                    periodLength = sugangSnuClassTime.endPeriod - sugangSnuClassTime.startPeriod,
                    startPeriod = sugangSnuClassTime.startPeriod,
                    place = locationTexts.joinToString("/"),
                )
            }
            .sortedWith(compareBy({ it.day.value }, { it.startPeriod }))
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
                        "$dayOfWeek(${periodFormat.format(startPeriod)}-${periodFormat.format(endPeriod - startPeriod)})"
                    }
                }.getOrElse {
                    logger.error("교시 파싱 에러 {}", classTimeText)
                    ""
                }
            }

    private fun parseSugangSnuClassTime(classTime: String): SugangSnuClassTime {
        return classTimeRegEx.find(classTime)!!.groups.let { matchResult ->
            SugangSnuClassTime(
                dayOfWeek = matchResult["day"]!!.value,
                startHour = matchResult["startHour"]!!.value,
                startMinute = matchResult["startMinute"]!!.value,
                endHour = matchResult["endHour"]!!.value,
                endMinute = matchResult["endMinute"]!!.value,
            )
        }
    }
}
