import TimetableService = require('@app/core/timetable/TimetableService');
import RefLectureService = require('@app/core/lecture/RefLectureService');
import TimetableLectureService = require('@app/core/timetable/TimetableLectureService');
import ObjectUtil = require('@app/core/common/util/ObjectUtil');
import RedisUtil = require('@app/core/redis/RedisUtil');
import RefLecture from '@app/core/lecture/model/RefLecture';
import LectureDifference from './model/LectureDifference';
import winston = require('winston');
import LectureTimeOverlapError from '@app/core/timetable/error/LectureTimeOverlapError';
import CoursebookUpdateNotificationService = require('./CoursebookUpdateNotificationService');

let logger = winston.loggers.get('default');

export async function processUpdatedAndRemoved(year:number, semesterIndex:number,
  updatedList: LectureDifference[], removedList: RefLecture[], isFcmEnabled:boolean):Promise<void> {
  let userIdNumUpdatedMap: Map<string, number> = new Map();
  let userIdNumRemovedMap: Map<string, number> = new Map();

  function incrementUpdated(userId: any) {
    userId = (typeof userId == 'string') ? userId : String(userId);
    let oldValue = userIdNumUpdatedMap.get(userId);
    if (oldValue) {
      userIdNumUpdatedMap.set(userId, oldValue + 1);
    } else {
      userIdNumUpdatedMap.set(userId, 1);
    }
  }

  function incrementRemoved(userId: any) {
    userId = (typeof userId == 'string') ? userId : String(userId);
    let oldValue = userIdNumRemovedMap.get(userId);
    if (oldValue) {
      userIdNumRemovedMap.set(userId, oldValue + 1);
    } else {
      userIdNumRemovedMap.set(userId, 1);
    }
  }

  async function processUpdated(lectureDifference: LectureDifference) {
    let refLectureId = lectureDifference.oldLecture._id;
    let course_number = lectureDifference.oldLecture.course_number;
    let lecture_number = lectureDifference.oldLecture.lecture_number;

    await RefLectureService.partialModifiy(refLectureId, lectureDifference.difference);

    let timetables = await TimetableService.getHavingLecture(
      year, semesterIndex, course_number, lecture_number);

    for (let i=0; i<timetables.length; i++) {
      let timetable = timetables[i];

      try {
        let userLectureId = TimetableLectureService.getUserLectureFromTimetableByCourseNumber(
          timetable, course_number, lecture_number)._id;
        let userLecture = ObjectUtil.deepCopy(lectureDifference.difference);
        userLecture._id = userLectureId;
        await TimetableLectureService.partialModifyUserLecture(timetable.user_id, timetable._id, userLecture, false);
        incrementUpdated(timetable.user_id);
        await CoursebookUpdateNotificationService.addLectureUpdateNotification(timetable, lectureDifference);
      } catch (err) {
        if (err instanceof LectureTimeOverlapError) {
          await TimetableLectureService.removeLectureByCourseNumber(timetable._id, course_number, lecture_number);
          incrementRemoved(timetable.user_id);
          await CoursebookUpdateNotificationService.addTimeOverlappedLectureRemovedNotification(timetable, lectureDifference);
        } else throw err;
      }
    }
  }

  async function processRemoved(removed: RefLecture) {
    await RefLectureService.remove(removed._id);

    let timetables = await TimetableService.getHavingLecture(
      year, semesterIndex, removed.course_number, removed.lecture_number);

    for (let timetable of timetables) {
      await TimetableLectureService.removeLectureByCourseNumber(timetable._id, removed.course_number, removed.lecture_number);
      incrementRemoved(timetable.user_id);
      await CoursebookUpdateNotificationService.addLectureRemovedNotification(timetable, removed);
    }
  }

  for (let i=0; i<updatedList.length; i++) {
    try {
      await processUpdated(updatedList[i]);
      logger.info((i + 1) + "th updated");
    } catch (err) {
      let newLecture = updatedList[i].newLecture;
      logger.error("Failed to process " + (i + 1) + "th updated " +
        newLecture.course_number + " " + newLecture.lecture_number + " " + newLecture.course_title + "\nCaused by : " + err);
    }
  }

  for (let i=0; i<removedList.length; i++) {
    try {
      await processRemoved(removedList[i]);
      logger.info((i + 1) + "th removed");
    } catch (err) {
      let removedLecture = removedList[i];
      logger.error("Failed to process " + (i + 1) + "th removed " +
        removedLecture.course_number + " " + removedLecture.lecture_number + " " + removedLecture.course_title + "\nCaused by : " + err);
    }
  }

  logger.info('Flushing all redis data');
  await RedisUtil.flushdb();

  if (isFcmEnabled) {
    let users: Set<string> = new Set();

    for (let userId of userIdNumRemovedMap.keys()) {
      users.add(userId);
    }

    for (let userId of userIdNumUpdatedMap.keys()) {
      users.add(userId);
    }

    let index = 1;
    for (let userId of users) {
      logger.info((index++) + "th user fcm");
      let numUpdated = userIdNumUpdatedMap.get(userId);
      let numRemoved = userIdNumRemovedMap.get(userId);
      await CoursebookUpdateNotificationService.sendCoursebookUpdateFcmNotification(userId, numUpdated, numRemoved);
    }
  }
}

