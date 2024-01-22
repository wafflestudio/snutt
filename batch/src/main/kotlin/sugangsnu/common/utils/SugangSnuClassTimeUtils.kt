package com.wafflestudio.snu4t.sugangsnu.common.utils

import com.wafflestudio.snu4t.common.enum.DayOfWeek
import com.wafflestudio.snu4t.lectures.data.ClassPlaceAndTime
import com.wafflestudio.snu4t.sugangsnu.common.data.SugangSnuClassTime
import org.slf4j.LoggerFactory

object SugangSnuClassTimeUtils {
    private val log = LoggerFactory.getLogger(javaClass)
    private val classTimeRegEx =
        """^(?<day>[월화수목금토일])\((?<startHour>\d{2}):(?<startMinute>\d{2})~(?<endHour>\d{2}):(?<endMinute>\d{2})\)$""".toRegex()

    fun convertTextToClassTimeObject(classTimesTexts: List<String>, locationsTexts: List<String>): List<ClassPlaceAndTime> = runCatching {
        val sugangSnuClassTimes = classTimesTexts
            .filter { it.isNotBlank() }.map(SugangSnuClassTimeUtils::parseSugangSnuClassTime)
        val locationTexts = locationsTexts.let { locationText ->
            when (locationText.size) {
                sugangSnuClassTimes.size -> locationText
                1 -> List(sugangSnuClassTimes.size) { locationText.first() }
                0 -> List(sugangSnuClassTimes.size) { "" }
                else -> throw RuntimeException("locations does not match with times $classTimesTexts $locationsTexts")
            }
        }
        sugangSnuClassTimes.zip(locationTexts)
            .groupBy({ it.first }, { it.second })
            .map { (sugangSnuClassTime, locationTexts) ->
                ClassPlaceAndTime(
                    day = DayOfWeek.getByKoreanText(sugangSnuClassTime.dayOfWeek)!!,
                    place = locationTexts.joinToString("/"),
                    startMinute = sugangSnuClassTime.startHour.toInt() * 60 + sugangSnuClassTime.startMinute.toInt(),
                    endMinute = sugangSnuClassTime.endHour.toInt() * 60 + sugangSnuClassTime.endMinute.toInt(),
                    lectureBuilding = null
                )
            }
            .sortedWith(compareBy({ it.day.value }, { it.startMinute }))
    }.getOrElse {
        log.error("classtime으로 변환 실패 (time: {}, location: {})", classTimesTexts, locationsTexts)
        emptyList()
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
