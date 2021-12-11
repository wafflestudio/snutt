import LectureColorService = require('./LectureColorService');
import TimePlaceUtil = require('@app/core/timetable/util/TimePlaceUtil');
import LectureService = require('@app/core/lecture/LectureService');
import RefLectureService = require('@app/core/lecture/RefLectureService');
import UserLecture from './model/UserLecture';
import RefLectrureNotFoundError from "../lecture/error/RefLectureNotFoundError";
import WrongRefLectureSemesterError from "./error/WrongRefLectureSemesterError";
import DuplicateLectureError from "./error/DuplicateLectureError";
import TimetableNotFoundError from "./error/TimetableNotFoundError";
import InvalidLectureUpdateRequestError from './error/InvalidLectureUpdateRequestError';
import LectureTimeOverlapError from './error/LectureTimeOverlapError';
import CustomLectureResetError from './error/CusromLectureResetError';
import NotCustomLectureError from './error/NotCustomLectureError';
import UserLectureNotFoundError from './error/UserLectureNotFoundError';
import ObjectUtil = require('@app/core/common/util/ObjectUtil');
import RefLecture from '@app/core/lecture/model/RefLecture';

import TimetableRepository = require('./TimetableRepository');
import TimetableService = require('./TimetableService');
import Timetable from './model/Timetable';
import TimePlace from './model/TimePlace';
import InvalidLectureTimeJsonError from '../lecture/error/InvalidLectureTimeJsonError';
import winston = require('winston');
let logger = winston.loggers.get('default');

export async function addRefLecture(timetable: Timetable, lectureId: string): Promise<void> {
  let lecture = await RefLectureService.getByMongooseId(lectureId);
  if (!lecture) throw new RefLectrureNotFoundError();
  if (lecture.year != timetable.year || lecture.semester != timetable.semester) {
    throw new WrongRefLectureSemesterError(lecture.year, lecture.semester);
  }
  let colorIndex = getAvailableColorIndex(timetable);
  let userLecture = fromRefLecture(lecture, colorIndex);
  await addLecture(timetable, userLecture);
}

export async function addLecture(timetable: Timetable, lecture: UserLecture): Promise<void> {
  ObjectUtil.deleteObjectId(lecture);

  if (lecture.credit && (typeof lecture.credit === 'string' || <any>lecture.credit instanceof String)) {
    lecture.credit = Number(lecture.credit);
  }

  for (var i = 0; i< timetable.lecture_list.length; i++){
    if (isIdenticalCourseLecture(lecture, timetable.lecture_list[i])) {
      throw new DuplicateLectureError();
    }
  }

  validateLectureTime(timetable, lecture);

  LectureColorService.validateLectureColor(lecture)

  let creationDate = new Date();
  lecture.created_at = creationDate;
  lecture.updated_at = creationDate;
  await TimetableRepository.insertUserLecture(timetable._id, lecture);
  await TimetableRepository.updateUpdatedAt(timetable._id, Date.now());
}



export async function addCustomLecture(timetable: Timetable, lecture: UserLecture): Promise<void> {
  /* If no time json is found, mask is invalid */
  LectureService.setTimemask(lecture);
  if (!lecture.course_title) throw new InvalidLectureUpdateRequestError(lecture);

  if (!isCustomLecture(lecture)) throw new NotCustomLectureError(lecture);

  if (!lecture.color && !lecture.colorIndex) {
    lecture.colorIndex = getAvailableColorIndex(timetable);
  }

  await addLecture(timetable, lecture);
}


export async function resetLecture(userId:string, tableId: string, lectureId: string): Promise<void> {
  let table: Timetable = await TimetableService.getByMongooseId(userId, tableId);
  let lecture: UserLecture = getUserLectureFromTimetableByLectureId(table, lectureId);
  if (isCustomLecture(lecture)) {
    throw new CustomLectureResetError();
  }

  let refLecture = await RefLectureService.getByCourseNumber(table.year, table.semester, lecture.course_number, lecture.lecture_number);
  if (refLecture === null) {
    throw new RefLectrureNotFoundError();
  }

  let colorIndex = getAvailableColorIndex(table);
  let newLecture = fromRefLecture(refLecture, colorIndex);
  newLecture._id = lectureId;

  await TimetableRepository.partialUpdateUserLecture(tableId, newLecture);
  await TimetableRepository.updateUpdatedAt(tableId, Date.now());
}

export async function partialModifyUserLecture(userId: string, tableId: string, lecture: any): Promise<void> {
  let table = await TimetableRepository.findByUserIdAndMongooseId(userId, tableId);

  if (!table) {
    throw new TimetableNotFoundError();
  }

  if (lecture.course_number || lecture.lecture_number) {
    throw new InvalidLectureUpdateRequestError(lecture);
  }

  if (lecture['class_time_json']) {
    LectureService.setTimemask(lecture);
    lecture['class_time_mask'] = TimePlaceUtil.timeJsonToMask(lecture['class_time_json'], true);
  }

  if (lecture['class_time_mask']) {
    validateLectureTime(table, lecture);
  }

  if (lecture['color']) {
    LectureColorService.validateLectureColor(lecture);
  }

  lecture.updated_at = Date.now();

  await TimetableRepository.partialUpdateUserLecture(table._id, lecture);
  await TimetableRepository.updateUpdatedAt(table._id, Date.now());
}

function validateLectureTime(table: Timetable, lecture: UserLecture): void {
  if (isOverlappingLecture(table, lecture)) {
    throw new LectureTimeOverlapError();
  }

  for (let i=0; i<lecture.class_time_json.length; i++) {
    validateLectureTimeJson(lecture.class_time_json[i]);
  }
}

function isOverlappingLecture(table: Timetable, lecture: UserLecture): boolean {
  let overlappingLectureIds = getOverlappingLectureIds(table, lecture);
  if (overlappingLectureIds.length == 0) {
    return false;
  } else if (overlappingLectureIds.length == 1 && String(overlappingLectureIds[0]) == String(lecture._id)) {
    return false;
  } else {
    logger.error("Lecture overlap: " + JSON.stringify(lecture._id) + " with " + JSON.stringify(overlappingLectureIds));
    return true;
  }
}

function getOverlappingLectureIds(table: Timetable, lecture: UserLecture): string[] {
  let lectureIds = [];
  for (var i=0; i<table.lecture_list.length; i++) {
    var tableLecture:any = table.lecture_list[i];
    for (var j=0; j<tableLecture.class_time_mask.length; j++) {
      if ((tableLecture.class_time_mask[j] & lecture.class_time_mask[j]) != 0) {
        lectureIds.push(tableLecture._id);
        break;
      }
    }
  }
  return lectureIds;
}

function validateLectureTimeJson(timePlace: TimePlace): void {
  if (!ObjectUtil.isNumber(timePlace.day) || !ObjectUtil.isNumber(timePlace.len) || !ObjectUtil.isNumber(timePlace.start)) {
    throw new InvalidLectureTimeJsonError();
  }
}

function getAvailableColorIndices(table: Timetable): number[] {
  var checked:boolean[] = [];
  for (var i=0; i<table.lecture_list.length; i++) {
    var lecture_color = table.lecture_list[i].colorIndex;
    checked[lecture_color] = true;
  }

  var ret:number[] = [];
  // colorIndex = 0 is custom color!
  for (var i=1; i<LectureColorService.MAX_NUM_COLOR; i++) {
    if (!checked[i]) ret.push(i);
  }
  return ret;
}

function getAvailableColorIndex(table: Timetable): number {
  let availableIndices = getAvailableColorIndices(table);
  if (availableIndices.length == 0) return Math.floor(Math.random() * LectureColorService.MAX_NUM_COLOR) + 1;
  else return availableIndices[Math.floor(Math.random() * availableIndices.length)]
}

function getUserLectureFromTimetableByLectureId(table: Timetable, lectureId: string): UserLecture {
  for (let i=0; i<table.lecture_list.length; i++) {
    let lecture = table.lecture_list[i];
    if (lecture._id == lectureId) {
      return lecture;
    }
  }
  throw new UserLectureNotFoundError();
}

export async function removeLecture(userId: string, tableId: string, lectureId: string): Promise<void> {
  await TimetableRepository.deleteLectureWithUserId(userId, tableId, lectureId);
  await TimetableRepository.updateUpdatedAt(tableId, Date.now());
}

export async function removeLectureByCourseNumber(tableId: string, courseNumber: string, lectureNumber: string): Promise<void> {
  await TimetableRepository.deleteLectureByCourseNumber(tableId, courseNumber, lectureNumber);
}

export function getUserLectureFromTimetableByCourseNumber(table: Timetable, courseNumber: string, lectureNumber:string): UserLecture {
  for (let i=0; i<table.lecture_list.length; i++) {
    let lecture = table.lecture_list[i];
    if (lecture.course_number === courseNumber && lecture.lecture_number === lectureNumber) {
      return lecture;
    }
  }
  return null;
}

function isCustomLecture(lecture: UserLecture): boolean {
  return !lecture.course_number && !lecture.lecture_number;
}

function isIdenticalCourseLecture(l1: UserLecture, l2: UserLecture): boolean {
  if (isCustomLecture(l1) || isCustomLecture(l2)) return false;
  return (l1.course_number === l2.course_number && l1.lecture_number === l2.lecture_number);
}

function fromRefLecture(refLecture: RefLecture, colorIndex: number): UserLecture {
  let creationDate = new Date();
  return {
    classification: refLecture.classification,                           // 교과 구분
    department: refLecture.department,                               // 학부
    academic_year: refLecture.academic_year,                            // 학년
    course_title: refLecture.course_title,   // 과목명
    credit: refLecture.credit,                                   // 학점
    class_time: refLecture.class_time,
    class_time_json: refLecture.class_time_json,
    class_time_mask: refLecture.class_time_mask,
    instructor: refLecture.instructor,                               // 강사
    quota: refLecture.quota,                                    // 정원
    remark: refLecture.remark,                                   // 비고
    category: refLecture.category,
    course_number: refLecture.course_number,   // 교과목 번호
    lecture_number: refLecture.lecture_number,  // 강좌 번호
    created_at: creationDate,
    updated_at: creationDate,
    colorIndex: colorIndex
  }
}
