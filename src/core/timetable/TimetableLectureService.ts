import LectureColorService = require('./LectureColorService');
import TimePlaceUtil = require('@app/core/timetable/util/TimePlaceUtil');
import LectureService = require('@app/core/lecture/LectureService');
import RefLectureService = require('@app/core/lecture/RefLectureService');
import ObjectUtil = require('@app/core/common/util/ObjectUtil');

import TimetableRepository = require('./TimetableRepository');
import TimetableService = require('./TimetableService');
import winston = require('winston');
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
import RefLecture from '@app/core/lecture/model/RefLecture';
import Timetable from './model/Timetable';
import TimePlace from './model/TimePlace';
import InvalidLectureTimeJsonError from '../lecture/error/InvalidLectureTimeJsonError';
import {Time} from "@app/core/timetable/model/Time";
import Lecture from "@app/core/lecture/model/Lecture";

let logger = winston.loggers.get('default');

const ZERO_PERIOD_START_HOUR = 8

export async function addRefLecture(timetable: Timetable, lectureId: string, isForced: boolean): Promise<void> {
  let lecture = await RefLectureService.getByMongooseId(lectureId);
  if (!lecture) throw new RefLectrureNotFoundError();
  if (lecture.year != timetable.year || lecture.semester != timetable.semester) {
    throw new WrongRefLectureSemesterError(lecture.year, lecture.semester);
  }
  let colorIndex = getAvailableColorIndex(timetable);
  let userLecture = fromRefLecture(lecture, colorIndex);
  await addLecture(timetable, userLecture, isForced);
}

export async function addLecture(timetable: Timetable, lecture: UserLecture, isForced: boolean = false): Promise<void> {
  ObjectUtil.deleteObjectId(lecture);

  if (lecture.credit && (typeof lecture.credit === 'string' || <any>lecture.credit instanceof String)) {
    lecture.credit = Number(lecture.credit);
  }

  for (let i = 0; i< timetable.lecture_list.length; i++){
    if (isIdenticalCourseLecture(lecture, timetable.lecture_list[i])) {
      throw new DuplicateLectureError();
    }
  }

  validateLectureTime(lecture);

  LectureColorService.validateLectureColor(lecture)

  const overlappingLectures = getOverlappingLectures(timetable, lecture)
  const overlappingLectureIds = overlappingLectures.map(eachLecture => eachLecture._id)

  if (isForced) {
    await TimetableRepository.deleteLectures(timetable._id, overlappingLectureIds);
  } else if (isOverlappingLecture(timetable, lecture)) {
    const confirmMessage = makeOverwritingConfirmMessage(overlappingLectures)
    throw new LectureTimeOverlapError(confirmMessage);
  }

  let creationDate = new Date();
  lecture.created_at = creationDate;
  lecture.updated_at = creationDate;
  await TimetableRepository.insertUserLecture(timetable._id, lecture);
  await TimetableRepository.updateUpdatedAt(timetable._id, Date.now());
}



export async function addCustomLecture(timetable: Timetable, lecture: UserLecture, isForced: boolean): Promise<void> {
  if (isInvalidClassTime(lecture)) throw new InvalidLectureTimeJsonError()
  syncRealTimeWithPeriod(lecture)

  // ios 3.1.3 UUID 넘겨주는 에러 hotfix
  delete lecture.lecture_id

  if (!lecture.course_title) throw new InvalidLectureUpdateRequestError(lecture);

  if (!isCustomLecture(lecture)) throw new NotCustomLectureError(lecture);

  if (!lecture.color && !lecture.colorIndex) {
    lecture.colorIndex = getAvailableColorIndex(timetable);
  }

  await addLecture(timetable, lecture, isForced);
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

export async function partialModifyUserLecture(userId: string, tableId: string, lecture: any, isForced: boolean): Promise<void> {
  let table = await TimetableRepository.findByUserIdAndMongooseId(userId, tableId);

  if (!table) {
    throw new TimetableNotFoundError();
  }

  if (lecture.course_number || lecture.lecture_number) {
    throw new InvalidLectureUpdateRequestError(lecture);
  }

  if (lecture['class_time_json']) {
    if(isInvalidClassTime(lecture)) throw new InvalidLectureTimeJsonError()
    syncRealTimeWithPeriod(lecture)
    LectureService.setTimemask(lecture);
    lecture['class_time_mask'] = TimePlaceUtil.timeJsonToMask(lecture['class_time_json'], true);
  }

  if (lecture['color']) {
    LectureColorService.validateLectureColor(lecture);
  }

  if (lecture['class_time_mask']) {
    validateLectureTime(lecture);

    const overlappingLectures = getOverlappingLectures(table, lecture).filter(overlappingLecture => overlappingLecture._id != lecture._id)
    const overlappingLectureIds = overlappingLectures.map(eachLecture => eachLecture._id)

    if (isForced) {
      await TimetableRepository.deleteLectures(table._id, overlappingLectureIds);
    } else if (isOverlappingLecture(table, lecture)) {
      const confirmMessage = makeOverwritingConfirmMessage(overlappingLectures)
      throw new LectureTimeOverlapError(confirmMessage);
    }
  }

  lecture.updated_at = Date.now();

  await TimetableRepository.partialUpdateUserLecture(table._id, lecture);
  await TimetableRepository.updateUpdatedAt(table._id, Date.now());
}

function validateLectureTime(lecture: UserLecture): void {
  for (let i=0; i<lecture.class_time_json.length; i++) {
    validateLectureTimeJson(lecture.class_time_json[i]);
    for (let j = i + 1; j < lecture.class_time_json.length; j++) {
      if(timesOverlap(lecture.class_time_json[i], lecture.class_time_json[j]))
        throw new LectureTimeOverlapError();
    }
  }
}

function isOverlappingLecture(table: Timetable, lecture: UserLecture): boolean {
  let overlappingLectures = getOverlappingLectures(table, lecture);
  if (overlappingLectures.length === 0) {
    return false;
  } else if (overlappingLectures.length === 1 && String(overlappingLectures[0]._id) === String(lecture._id)) {
    return false;
  } else {
    const overlappingLectureIds = overlappingLectures.map((lecture: UserLecture) => lecture._id)
    logger.error("Lecture overlap: " + lecture._id + " with " + JSON.stringify(overlappingLectureIds));
    return true;
  }
}

function makeOverwritingConfirmMessage(overlappingLectures: UserLecture[]) {
  const overlappingLectureTitles = overlappingLectures.map(lecture => lecture.course_title).slice(0,2).join(", ")
  const shortFormOfTitles = overlappingLectures.length < 3 ? "" : `외 ${overlappingLectures.length - 2}개의 `
  return `${overlappingLectureTitles} ${shortFormOfTitles}강의가 중복되어 있습니다. 강의를 덮어쓰시겠습니까?`
}

function getOverlappingLectures(table: Timetable, lecture: UserLecture): UserLecture[] {
  return table.lecture_list.filter(existingLecture => twoLecturesOverlap(existingLecture, lecture) )
}

function validateLectureTimeJson(timePlace: TimePlace): void {
  const startTime = Time.fromHourMinuteString(timePlace.start_time)
  const endTime = Time.fromHourMinuteString(timePlace.end_time)
  const endsTooLate = endTime.minute > Time.fromHourMinuteString("23:55").minute
  const lectureTimeTooShort = (endTime.minute - startTime.minute) < 5
  const hasInvalidNumbers = !ObjectUtil.isNumber(timePlace.day) || !ObjectUtil.isNumber(timePlace.len) || !ObjectUtil.isNumber(timePlace.start)
  if (endsTooLate || lectureTimeTooShort || hasInvalidNumbers) {
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
  for (var i=1; i<=LectureColorService.MAX_NUM_COLOR; i++) {
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

function twoLecturesOverlap(lectureA: Lecture, lectureB: Lecture): boolean {
  return lectureA.class_time_json.some(classTimeA =>
    lectureB.class_time_json.some(classTimeB => timesOverlap(classTimeA, classTimeB))
  )
}

function timesOverlap(time1:TimePlace, time2: TimePlace): boolean {
  return time1.day === time2.day
    && (Time.fromHourMinuteString(time1.start_time).minute < Time.fromHourMinuteString(time2.end_time).minute)
    && (Time.fromHourMinuteString(time1.end_time).minute > Time.fromHourMinuteString(time2.start_time).minute)
}

function syncRealTimeWithPeriod(lecture: any): void  {
  lecture.class_time_json.forEach(it => {
    it.start_time = it.start_time || new Time((it.start + 8) * 60).toHourMinuteFormat()
    it.end_time = it.end_time || new Time((it.start + it.len + 8) * 60).toHourMinuteFormat()
    const startTime = Time.fromHourMinuteString(it.start_time)
    const endTime = Time.fromHourMinuteString(it.end_time)
    it.len = it.len ? Number(it.len) : Math.ceil(endTime.subtract(startTime).minute / 30) / 2
    it.start = it.start ? Number(it.start) : Math.floor(startTime.subtractHour(8).minute / 30) / 2
  })
}

function isInvalidClassTime(lecture: Lecture): boolean {
  return lecture.class_time_json.some(
    it => it.start_time == null && it.start == null || it.end_time == null && it.len == null
  );
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
    real_class_time: refLecture.real_class_time,
    class_time_json: refLecture.class_time_json,
    class_time_mask: refLecture.class_time_mask,
    instructor: refLecture.instructor,                               // 강사
    quota: refLecture.quota,                                    // 정원
    freshmanQuota: refLecture.freshmanQuota,                     // 신입생정원
    remark: refLecture.remark,                                   // 비고
    category: refLecture.category,
    course_number: refLecture.course_number,   // 교과목 번호
    lecture_number: refLecture.lecture_number,  // 강좌 번호
    created_at: creationDate,
    updated_at: creationDate,
    colorIndex: colorIndex,
    lecture_id: refLecture._id
  }
}
