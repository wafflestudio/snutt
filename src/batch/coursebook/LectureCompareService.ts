import RefLecture from '@app/core/lecture/model/RefLecture';
import LectureDifference from './model/LectureDifference';
import RefLectureService = require('@app/core/lecture/RefLectureService');
import TimePlaceUtil = require('@app/core/timetable/util/TimePlaceUtil');
import winston = require('winston');
var logger = winston.loggers.get('default');

function compareLecture(oldLecture: RefLecture, newLecture: RefLecture): LectureDifference {
  var difference = {};
  var keys = [
    'classification',
    'department',
    'academic_year',
    'course_title',
    'credit',
    'instructor',
    'quota',
    'remark',
    'category',
    'class_time',
    'real_class_time'
    ];
  for (let key of keys) {
    if (oldLecture[key] != newLecture[key]) {
      difference[key] = newLecture[key];
    }
  }

  if (!TimePlaceUtil.equalTimeJson(oldLecture.class_time_json, newLecture.class_time_json)) {
    difference["class_time_json"] = newLecture.class_time_json;
  }

  if (Object.keys(difference).length === 0) {
    return null;
  } else {
    return {
      oldLecture: oldLecture,
      newLecture: newLecture,
      difference: difference
    };
  }
};

export async function compareLectures(year:number, semester: number, newLectureList:RefLecture[]) {
  logger.info("Pulling existing lectures...");
  var oldLectureList = await RefLectureService.getBySemester(year, semester);

  let createdList: RefLecture[] = [];
  let removedList: RefLecture[] = [];
  let updatedList: LectureDifference[] = [];

  let oldLectureMap = makeLectureMap(oldLectureList);
  let newLectureMap = makeLectureMap(newLectureList);

  for (let newLectureKey of newLectureMap.keys()) {
    let oldLecture = oldLectureMap.get(newLectureKey);
    let newLecture = newLectureMap.get(newLectureKey);
    if (oldLecture) {
      let difference = compareLecture(oldLecture, newLecture);
      if (difference) {
        updatedList.push(difference);
      }
    } else {
      createdList.push(newLecture);
    }
  }

  for (let oldLectureKey of oldLectureMap.keys()) {
    let oldLecture = oldLectureMap.get(oldLectureKey);
    let newLecture = newLectureMap.get(oldLectureKey);
    if (!newLecture) {
      removedList.push(oldLecture);
    }
  }

  return {
    createdList: createdList,
    removedList: removedList,
    updatedList: updatedList
  };
}

function makeLectureMap(lectureList: RefLecture[]): Map<string, RefLecture> {
  let lectureMap: Map<string, RefLecture> = new Map();
  for (let lecture of lectureList) {
    lectureMap.set(makeLectureMapKey(lecture), lecture);
  }
  return lectureMap;
}

function makeLectureMapKey(lecture: RefLecture): string {
  return lecture.course_number + '##' + lecture.lecture_number;
}
