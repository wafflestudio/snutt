require('module-alias/register')
require('@app/batch/config/log');
require('@app/core/config/mongo');
require('@app/core/config/redis');

import { compareLectures } from './LectureCompareService';
import { processUpdatedAndRemoved } from './LectureProcessService';
import CourseBookService = require('@app/core/coursebook/CourseBookService');
import RefLectureService = require('@app/core/lecture/RefLectureService');
import TagListService = require('@app/core/taglist/TagListService');
import SugangSnu2Service = require('./sugangsnu/SugangSnu2Service');
import TagParseService = require('./TagParseService');
import winston = require('winston');
import SimpleJob from '../common/SimpleJob';
import RefLecture from '@app/core/lecture/model/RefLecture';
import CoursebookUpdateNotificationService = require('./CoursebookUpdateNotificationService');
import RedisUtil = require('@app/core/redis/RedisUtil');
let logger = winston.loggers.get('default');

/**
 * 현재 수강편람과 다음 수강편람
 */
async function getUpdateCandidate(): Promise<Array<[number, number]>> {
  let recentCoursebook = await CourseBookService.getRecent();
  if (!recentCoursebook) {
    let date = new Date();
    let year = date.getFullYear();
    let month = date.getMonth();
    let semester: number;
    if (month < 3) {
      semester = 4; // Winter
    } else if (month < 7) {
      semester = 1; // Spring
    } else if (month < 9) {
      semester = 2; // Summer
    } else {
      semester = 3; // Fall
    }
    logger.info("No recent coursebook found, infer from the current date.");
    logger.info("Inferred ", year, semester);
    return [[year, semester]];
  }
  let year = recentCoursebook.year;
  let semester = recentCoursebook.semester;

  let nextYear = year;
  let nextSemester = semester + 1;
  if (nextSemester > 4) {
    nextYear++;
    nextSemester = 1;
  }

  return [[year, semester],
  [nextYear, nextSemester]];
}


export async function fetchAndInsert(year: number, semester: number, isFcmEnabled: boolean): Promise<void> {
  logger.info("Fetching from sugang.snu.ac.kr...");
  let fetched = await SugangSnu2Service.getRefLectureList(year, semester);
  if (fetched.length == 0) {
    logger.warn("No lecture found.");
    return;
  }
  logger.info("Load complete with " + fetched.length + " courses");
  logger.info("Compare lectures...");
  let { updatedList, removedList, createdList } = await compareLectures(year, semester, fetched);
  if (updatedList.length === 0 &&
    createdList.length === 0 &&
    removedList.length === 0) {
    logger.info("Nothing updated.");
    return;
  }
  logger.info(updatedList.length + " updated, " +
    createdList.length + " created, " +
    removedList.length + " removed.");

  logger.info("Sending notifications...");
  await RefLectureService.addAll(createdList);
  await processUpdatedAndRemoved(year, semester, updatedList, removedList, isFcmEnabled);

  await validateRefLecture(year, semester, fetched);

  await upsertTagList(year, semester, fetched);

  await upsertCoursebook(year, semester, isFcmEnabled);

  return;
}

async function validateRefLecture(year: number, semester: number, fetched: RefLecture[]) {
  let { updatedList, removedList, createdList } = await compareLectures(year, semester, fetched);
  if (updatedList.length === 0 &&
    createdList.length === 0 &&
    removedList.length === 0) {
    return;
  }

  logger.error("validateRefLecture failed, upsert all lectures");
  await upsertRefLectureList(year, semester, fetched);
}

async function upsertRefLectureList(year: number, semester: number, fetched: RefLecture[]) {
  await RefLectureService.removeBySemester(year, semester);
  logger.info("Removed existing lecture for this semester");

  logger.info("Inserting new lectures...");
  var inserted = await RefLectureService.addAll(fetched);
  logger.info("Insert complete with " + inserted + " success and " + (fetched.length - inserted) + " errors");

  logger.info('Flushing all redis data');
  await RedisUtil.flushdb();
}

async function upsertTagList(year: number, semester: number, fetched: RefLecture[]) {
  logger.info("Parsing tags...");
  let tags = TagParseService.parseTagFromLectureList(fetched);
  logger.info("Inserting tags from new lectures...");
  await TagListService.upsert({year: year, semester: semester, tags: tags});
  logger.info("Inserted tags");
}

async function upsertCoursebook(year: number, semester: number, isFcmEnabled: boolean) {
  logger.info("saving coursebooks...");
  /* Send notification only when coursebook is new */
  var existingDoc = await CourseBookService.get(year, semester);
  if (!existingDoc) {
    await CourseBookService.add({
      year: year,
      semester: semester,
      updated_at: new Date()
    });
    await CoursebookUpdateNotificationService.addCoursebookUpdateNotification(year, semester, isFcmEnabled);
  } else {
    await CourseBookService.modifyUpdatedAt(existingDoc, new Date());
  }
}

async function run() {
  let cands: Array<[number, number]>;
  if (process.argv.length != 4) {
    cands = await getUpdateCandidate();
  } else {
    cands = [[parseInt(process.argv[2]), parseInt(process.argv[3])]];
  }
  for (let i = 0; i < cands.length; i++) {
    let year = cands[i][0];
    let semester = cands[i][1];
    try {
      await fetchAndInsert(Number(year), semester, true);
    } catch (err) {
      logger.error(err);
      continue;
    }
  }
}

async function main() {
  await new SimpleJob("coursebook", run).run();
  setTimeout(() => process.exit(0), 1000);
}

if (!module.parent) {
  main();
}
