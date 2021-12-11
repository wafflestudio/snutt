import Timetable from "./model/Timetable";
import DuplicateTimetableTitleError from "./error/DuplicateTimetableTitleError";
import ObjectUtil = require('@app/core/common/util/ObjectUtil');
import TimetableRepository = require('./TimetableRepository');

import AbstractTimetable from './model/AbstractTimetable';
import TimetableNotEnoughParamError from './error/TimetableNotEnoughParamError';
import ThemeTypeEnum from "@app/core/timetable/model/ThemeTypeEnum";

//deprecated
export async function copy(timetable: Timetable): Promise<void> {
    for (let trial = 1; true; trial++) {
        let newTitle = timetable.title + " (" + trial + ")";
        try {
          return await copyWithTitle(timetable, newTitle);
        } catch (err) {
          if (err instanceof DuplicateTimetableTitleError) {
            continue;
          }
          throw err;
        }
    }
}

//deprecated
export async function copyWithTitle(src: Timetable, newTitle: string): Promise<void> {
    if (newTitle === src.title) {
        throw new DuplicateTimetableTitleError(src.user_id, src.year, src.semester, newTitle);
    }

    let copied = ObjectUtil.deepCopy(src);
    ObjectUtil.deleteObjectId(copied);
    copied.title = newTitle;
    copied.updated_at = Date.now();

    await validateTimetable(copied);

    await TimetableRepository.insert(copied);
}

export function remove(userId, tableId): Promise<void> {
  return TimetableRepository.deleteByUserIdAndMongooseId(userId, tableId);
}

export async function getByTitle(userId: string, year: number, semester: number, title: string): Promise<Timetable> {
  return TimetableRepository.findByUserIdAndSemesterAndTitle(userId, year, semester, title);
}

export async function getByMongooseId(userId, tableId: string): Promise<Timetable> {
  return TimetableRepository.findByUserIdAndMongooseId(userId, tableId);
}

export async function getHavingLecture(year: number, semester: number, courseNumber: string, lectureNumber: string): Promise<Timetable[]> {
  return TimetableRepository.findHavingLecture(year, semester, courseNumber, lectureNumber);
}

export async function modifyTitle(tableId, userId, newTitle): Promise<void> {
  let target = await TimetableRepository.findByUserIdAndMongooseId(userId, tableId);
  if (target.title === newTitle) {
    return;
  }

  let duplicate = await TimetableRepository.findByUserIdAndSemesterAndTitle(userId, target.year, target.semester, newTitle);
  if (duplicate !== null) {
    throw new DuplicateTimetableTitleError(userId, target.year, target.semester, newTitle);
  }

  await TimetableRepository.updateTitleByUserId(tableId, userId, newTitle);
  await TimetableRepository.updateUpdatedAt(tableId, Date.now());
}

export async function modifyTheme(tableId, userId, newTheme): Promise<void> {
  let target = await TimetableRepository.findByUserIdAndMongooseId(userId, tableId);
  if (target.theme === newTheme) {
    return;
  }

  await TimetableRepository.updateThemeByUserId(tableId, userId, newTheme);
  await TimetableRepository.updateUpdatedAt(tableId, Date.now());
}

export async function addCopyFromSourceId(user, sourceId): Promise<Timetable> {
  const source:Timetable = await TimetableRepository.findByUserIdAndMongooseId(user._id, sourceId)
  let newTitle: string = `${source.title} copy`
  for(let trial = 1; true; trial++) {
    try {
      const newTimetable: Timetable = {
        user_id: user.user_id,
        year: source.year,
        semester: source.semester,
        title: newTitle,
        theme : source.theme,
        lecture_list: source.lecture_list,
        updated_at: Date.now()
      };
      await validateTimetable(newTimetable)
      return await TimetableRepository.insert(newTimetable);
    } catch (err) {
      if (err instanceof DuplicateTimetableTitleError) {
        newTitle = source.title + ` copy(${trial})`
        continue;
      }
      throw err;
    }
  }
}

export async function addFromParam(params): Promise<Timetable> {
  let newTimetable: Timetable = {
    user_id : params.user_id,
    year : params.year,
    semester : params.semester,
    title : params.title,
    theme : ThemeTypeEnum.SNUTT,
    lecture_list : [],
    updated_at: Date.now()
  };

  await validateTimetable(newTimetable);

  return await TimetableRepository.insert(newTimetable);
};

async function validateTimetable(timetable: Timetable) {
  if (!timetable.user_id || !timetable.year || !timetable.semester || !timetable.title) {
    throw new TimetableNotEnoughParamError(timetable);
  }

  let duplicate = await TimetableRepository.findByUserIdAndSemesterAndTitle(timetable.user_id, timetable.year, timetable.semester, timetable.title);
  if (duplicate !== null) {
    throw new DuplicateTimetableTitleError(timetable.user_id, timetable.year, timetable.semester, timetable.title);
  }
}

export function getAbstractListByUserId(userId: string): Promise<AbstractTimetable[]> {
  return TimetableRepository.findAbstractListByUserId(userId);
}

export function getRecentByUserId(userId: string): Promise<Timetable> {
  return TimetableRepository.findRecentByUserId(userId);
}

export function getBySemester(year: number, semester: number): Promise<Timetable[]> {
  return TimetableRepository.findBySemester(year, semester);
}

export function getByUserIdAndSemester(userId: string, year: number, semester: number): Promise<Timetable[]> {
  return TimetableRepository.findByUserIdAndSemester(userId, year, semester);
}
