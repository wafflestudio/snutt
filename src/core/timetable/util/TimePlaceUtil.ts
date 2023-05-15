import winston = require('winston');

import TimePlace from '@app/core/timetable/model/TimePlace';
import LectureTimeOverlapError from '../error/LectureTimeOverlapError';
import InvalidLectureTimeJsonError from '../../lecture/error/InvalidLectureTimeJsonError';

var logger = winston.loggers.get('default');

export function timeAndPlaceToJson(timesString: string, locationsString: string, realTimesString: string): TimePlace[] {
    try {
        // 시간 정보가 없다면 빈 정보를 반환한다.
        if (timesString === '') {
            return [];
        }

        let locations = locationsString.split('/');
        let times = timesString.split('/');
        let realTimes = realTimesString.split('/')

        // 만약 강의실이 하나 뿐이거나 없다면, 시간에 맞춰서 강의 장소를 추가해준다.
        if (locations.length != times.length) {
            if (locations.length == 0) {
                for (let i=0; i<times.length; i++) {
                    locations.push('');
                }
            } else if (locations.length == 1) {
                for (let i=1; i<times.length; i++) {
                    locations.push(locations[0]);
                }
            } else {
                throw "locations does not match with times";
            }
        }

        let classes = times.map((time, idx) => {
            let timeSplitted = time.split('-');
            const parsedRealTimes = realTimes[idx].match(/\d{2}:\d{2}/g)
            const startTime = parsedRealTimes[0]
            const endTime = parsedRealTimes[1]
            let day = ['월', '화', '수', '목', '금', '토', '일'].indexOf(timeSplitted[0].charAt(0));
            let start = Number(timeSplitted[0].slice(2));
            let len = Number(timeSplitted[1].slice(0, -1));
            let place = locations[idx];
            return {
                day,
                start,
                start_time: startTime,
                end_time: endTime,
                len,
                place,
                startMinute: null,
                endMinute: null,
            };
        });

        for (let i = 0; i < classes.length; i++) {
            // If the day of the week is not the one we expected
            if (classes[i].day < 0) {
                throw "wrong day (i: " + i + ", day: " + classes[i].day + ")";
            }
        }

        // 시작 시간 순으로 오름차순 정렬
        classes.sort((a, b) => {
            if (a.day < b.day) return -1;
            if (a.day > b.day) return 1;
            if (a.start < b.start) return -1;
            if (a.start > b.start) return 1;
            return 0;
        })

        // Merge same time with different location
        for (let i = 1; i < classes.length; i++) {
            let prev = classes[i-1];
            let curr = classes[i];
            if (prev.day == curr.day && prev.start == curr.start && prev.len == curr.len) {
                prev.place += '/' + curr.place;
                classes.splice(i--, 1);
            }
        }
        return classes;
    } catch (err) {
        logger.error(err);
        logger.error("Failed to parse timePlace (times: " + timesString + ", locations: " + locationsString + ", realTime: " + realTimesString + ")");
        return [];
    }
}

export function equalTimeJson(t1:Array<TimePlace>, t2:Array<TimePlace>) {
    if (t1.length != t2.length) return false;
    for (var i=0; i<t1.length; i++) {
        if (t1[i].day != t2[i].day ||
        t1[i].start != t2[i].start ||
        t1[i].len != t2[i].len ||
        t1[i].place != t2[i].place)
        return false;
    }
    return true;
}

export function timeJsonToMask(timeJson:Array<TimePlace>, duplicateCheck?:boolean): number[] {
    var i,j;
    var bitTable2D = [];
    for (i = 0; i < 7; i++) {
        bitTable2D.push(new Array().fill(0, 0, 30));
    }

    timeJson.forEach(function(lecture, lectureIdx) {
        var dayIdx = Number(lecture.day);
        var end = Number(lecture.start) + Math.ceil(Number(lecture.len) * 2) / 2;
        if (Number(lecture.len) <= 0) throw new InvalidLectureTimeJsonError();
        if (lecture.start < 0) logger.warn("timeJsonToMask: lecture start less than 0");
        if (lecture.start > 14) logger.warn("timeJsonToMask: lecture start bigger than 14");
        if (duplicateCheck) {
            for (var i = lecture.start * 2; i < end*2; i++) {
                if (bitTable2D[dayIdx][i]) throw new LectureTimeOverlapError();
                bitTable2D[dayIdx][i] = 1;
            }
        } else {
            for (var i = lecture.start * 2; i < end*2; i++) {
                bitTable2D[dayIdx][i] = 1;
            }
        }
    });

    var timeMasks = [];
    for (i = 0; i < 7; i++) {
        var mask = 0;
        for (j = 0; j < 30; j++) {
            mask = mask << 1;
            if (bitTable2D[i][j] === 1)
                mask = mask + 1;
        }
        timeMasks.push(mask);
    }
    return timeMasks;
 }
