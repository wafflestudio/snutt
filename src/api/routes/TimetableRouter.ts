import ExpressPromiseRouter from 'express-promise-router';
var router = ExpressPromiseRouter();

import TimetableService = require('@app/core/timetable/TimetableService');
import TimetableLectureService = require('@app/core/timetable/TimetableLectureService');
import User from '@app/core/user/model/User';
import winston = require('winston');
import DuplicateTimetableTitleError from '@app/core/timetable/error/DuplicateTimetableTitleError';
import DuplicateLectureError from '@app/core/timetable/error/DuplicateLectureError';
import LectureTimeOverlapError from '@app/core/timetable/error/LectureTimeOverlapError';
import RefLectrureNotFoundError from '@app/core/lecture/error/RefLectureNotFoundError';
import WrongRefLectureSemesterError from '@app/core/timetable/error/WrongRefLectureSemesterError';
import InvalidLectureTimemaskError from '@app/core/lecture/error/InvalidLectureTimemaskError';
import InvalidLectureColorError from '@app/core/timetable/error/InvalidLectureColorError';
import InvalidLectureColorIndexError from '@app/core/timetable/error/InvalidLectureColorIndexError';
import InvalidLectureUpdateRequestError from '@app/core/timetable/error/InvalidLectureUpdateRequestError';
import NotCustomLectureError from '@app/core/timetable/error/NotCustomLectureError';
import CustomLectureResetError from '@app/core/timetable/error/CusromLectureResetError';
import UserLectureNotFoundError from '@app/core/timetable/error/UserLectureNotFoundError';
import TimetableNotFoundError from '@app/core/timetable/error/TimetableNotFoundError';
import InvalidLectureTimeJsonError from '@app/core/lecture/error/InvalidLectureTimeJsonError';
import RequestContext from '../model/RequestContext';
import { restGet, restPost } from '../decorator/RestDecorator';
import ApiError from '../error/ApiError';
import ErrorCode from '../enum/ErrorCode';
import UserAuthorizeMiddleware from '../middleware/UserAuthorizeMiddleware';
import {NUMBER_OF_THEME} from "@app/core/timetable/TimetableRepository";
let logger = winston.loggers.get('default');

router.use(UserAuthorizeMiddleware);

restGet(router, '/')(function(context, req) {
  let user:User = context.user;
  return TimetableService.getAbstractListByUserId(user._id);
});

restGet(router, '/recent')(async function(context, req) {
  let user:User = context.user;
  let result = await TimetableService.getRecentByUserId(user._id);
  if (!result) {
    throw new ApiError(404, ErrorCode.TIMETABLE_NOT_FOUND, "no timetable");
  }
  return result;
});

restGet(router, '/:id')(async function(context, req) {
  let user:User = context.user;
  let result = await TimetableService.getByMongooseId(user._id, req.params.id);
  if (!result) {
    throw new ApiError(404, ErrorCode.TIMETABLE_NOT_FOUND, "timetable not found");
  }
  return result;
});

restGet(router, '/:year/:semester')(async function(context, req) {
  let user:User = context.user;
  let result = await TimetableService.getByUserIdAndSemester(user._id, req.params.year, req.params.semester);
  if (!result) {
    throw new ApiError(404, ErrorCode.TIMETABLE_NOT_FOUND, "No timetable for given semester");
  }
  return result;
});

restPost(router, '/')(async function(context, req) {
  let user:User = context.user;
  if (req.query.source) {
    try {
      await TimetableService.addCopyFromSourceId(user, req.query.source);

      return await TimetableService.getAbstractListByUserId(user._id);
    } catch (err) {
      throw err;
    }
  }
  else {
    if (!req.body.year || !req.body.semester || !req.body.title)
      throw new ApiError(400, ErrorCode.NOT_ENOUGH_TO_CREATE_TIMETABLE, "not enough parameters");

    try {
      await TimetableService.addFromParam({
        user_id: user._id,
        year: req.body.year,
        semester: req.body.semester,
        title: req.body.title
      });

      return await TimetableService.getAbstractListByUserId(user._id);
    } catch (err) {
      if (err instanceof DuplicateTimetableTitleError) {
        throw new ApiError(403, ErrorCode.DUPLICATE_TIMETABLE_TITLE, "duplicate timetable title");
      }
      throw err;
    }
  }
});

/**
 * POST /tables/:timetable_id/lecture/:lecture_id
 * add a lecture into a timetable
 * param ===================================
 * Lecture id from search query
 */
restPost(router, '/:timetable_id/lecture/:lecture_id')(async function(context, req) {
  let user:User = context.user;
  try {
    let table = await TimetableService.getByMongooseId(user._id, req.params.timetable_id);
    if (!table) {
      throw new ApiError(404, ErrorCode.TIMETABLE_NOT_FOUND, "timetable not found");
    }

    await TimetableLectureService.addRefLecture(table, req.params.lecture_id);
    return await TimetableService.getByMongooseId(user._id, req.params.timetable_id);
  } catch (err) {
    if (err instanceof DuplicateLectureError)
      throw new ApiError(403, ErrorCode.DUPLICATE_LECTURE, "duplicate lecture");
    else if (err instanceof LectureTimeOverlapError)
      throw new ApiError(403, ErrorCode.LECTURE_TIME_OVERLAP, "lecture time overlap");
    else if (err instanceof RefLectrureNotFoundError)
      throw new ApiError(404, ErrorCode.REF_LECTURE_NOT_FOUND, "ref lecture not found");
    else if (err instanceof WrongRefLectureSemesterError)
      throw new ApiError(403, ErrorCode.WRONG_SEMESTER, "wrong semester");

    throw err;
  }
});

/**
 * POST /tables/:id/lecture
 * add a lecture into a timetable
 * param ===================================
 * json object of lecture to add
 */
restPost(router, '/:id/lecture')(async function(context, req) {
  let user:User = context.user;
  try {
    let table = await TimetableService.getByMongooseId(user._id, req.params.id);
    if (!table) {
      throw new ApiError(404, ErrorCode.TIMETABLE_NOT_FOUND, "timetable not found");
    }
    await TimetableLectureService.addCustomLecture(table, req.body);
    return await TimetableService.getByMongooseId(user._id, req.params.id);
  } catch (err) {
    if (err instanceof InvalidLectureTimemaskError)
      throw new ApiError(400, ErrorCode.INVALID_TIMEMASK, "invalid timemask");
    if (err instanceof InvalidLectureTimeJsonError)
      throw new ApiError(400, ErrorCode.INVALID_TIMEJSON, "invalid timejson");
    if (err instanceof DuplicateLectureError)
      throw new ApiError(403, ErrorCode.DUPLICATE_LECTURE, "duplicate lecture");
    if (err instanceof LectureTimeOverlapError)
      throw new ApiError(403, ErrorCode.LECTURE_TIME_OVERLAP, "lecture time overlap");
    if (err instanceof InvalidLectureColorError)
      throw new ApiError(400, ErrorCode.INVALID_COLOR, "invalid color");
    if (err instanceof InvalidLectureColorIndexError)
      throw new ApiError(400, ErrorCode.INVALID_COLOR, "invalid color");
    if (err instanceof InvalidLectureUpdateRequestError)
      throw new ApiError(400, ErrorCode.NO_LECTURE_TITLE, "no lecture title");
    if (err instanceof NotCustomLectureError)
      throw new ApiError(403, ErrorCode.NOT_CUSTOM_LECTURE, "only custon lectures allowed");
    if (err instanceof WrongRefLectureSemesterError)
      throw new ApiError(403, ErrorCode.WRONG_SEMESTER, "wrong semester");
    throw err;
  }
});

/**
 * PUT /tables/:table_id/lecture/:lecture_id
 * update a lecture of a timetable
 * param ===================================
 * json object of lecture to update
 */

router.put('/:table_id/lecture/:lecture_id', async function(req, res, next) {
  let context: RequestContext = req['context'];
  let user:User = context.user;
  var rawLecture = req.body;
  if(!rawLecture) return res.status(400).json({errcode: ErrorCode.NO_LECTURE_INPUT, message:"empty body"});

  if (!req.params.lecture_id)
    return res.status(400).json({errcode: ErrorCode.NO_LECTURE_ID, message:"need lecture_id"});

  try {
    let table = await TimetableService.getByMongooseId(user._id, req.params.table_id);
    if (!table) return res.status(404).json({errcode: ErrorCode.TIMETABLE_NOT_FOUND, message:"timetable not found"});

    rawLecture._id = req.params.lecture_id;
    await TimetableLectureService.partialModifyUserLecture(user._id, table._id, rawLecture);
    res.json(await TimetableService.getByMongooseId(user._id, req.params.table_id));
  } catch (err) {
    if (err instanceof InvalidLectureTimemaskError)
      return res.status(400).json({errcode: ErrorCode.INVALID_TIMEMASK, message:"invalid timemask"});
    if (err instanceof InvalidLectureTimeJsonError)
      return res.status(400).json({errcode: ErrorCode.INVALID_TIMEJSON, message:"invalid timejson"});
    if (err instanceof LectureTimeOverlapError)
      return res.status(403).json({errcode: ErrorCode.LECTURE_TIME_OVERLAP, message:"lecture time overlapped"});
    if (err instanceof InvalidLectureUpdateRequestError)
      return res.status(403).json({errcode: ErrorCode.ATTEMPT_TO_MODIFY_IDENTITY, message:"modifying identities forbidden"})
    if (err instanceof InvalidLectureColorError)
      return res.status(400).json({errcode: ErrorCode.INVALID_COLOR, message:"invalid color"})
    if (err instanceof InvalidLectureColorIndexError)
      return res.status(400).json({errcode: ErrorCode.INVALID_COLOR, message:"invalid color"})
    if (err instanceof UserLectureNotFoundError)
      return res.status(404).json({errcode: ErrorCode.LECTURE_NOT_FOUND, message:"lecture not found"});
    logger.error(err);
    return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message:"server fault"});
  }
});

router.put('/:table_id/lecture/:lecture_id/reset', async function(req, res, next) {
  let context: RequestContext = req['context'];
  let user:User = context.user;

  if (!req.params.lecture_id)
    return res.status(400).json({errcode: ErrorCode.NO_LECTURE_ID, message:"need lecture_id"});

  try {
    let table = await TimetableService.getByMongooseId(user._id, req.params.table_id);
    if (!table) return res.status(404).json({errcode:ErrorCode.TIMETABLE_NOT_FOUND, message:"timetable not found"});
    await TimetableLectureService.resetLecture(user._id, table._id, req.params.lecture_id);
    res.json(await TimetableService.getByMongooseId(user._id, req.params.table_id));
  } catch (err) {
    if (err instanceof CustomLectureResetError) {
      return res.status(403).json({errcode: ErrorCode.IS_CUSTOM_LECTURE, message:"cannot reset custom lectures"});
    } else if (err instanceof RefLectrureNotFoundError) {
      return res.status(404).json({errcode: ErrorCode.REF_LECTURE_NOT_FOUND, message:"ref lecture not found"});
    } else if (err instanceof UserLectureNotFoundError) {
      return res.status(404).json({errcode: ErrorCode.LECTURE_NOT_FOUND, message:"lecture not found"});
    } else if (err instanceof LectureTimeOverlapError) {
      return res.status(403).json({errcode: ErrorCode.LECTURE_TIME_OVERLAP, message:"lecture time overlap"});
    } else {
      logger.error(err);
      return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message:"reset lecture failed"});
    }
  }
});

/**
 * DELETE /tables/:table_id/lecture/:lecture_id
 * delete a lecture from a timetable
 */
router.delete('/:table_id/lecture/:lecture_id', async function(req, res, next) {
  let context: RequestContext = req['context'];
  let user:User = context.user;
  try {
    await TimetableLectureService.removeLecture(user._id, req.params.table_id, req.params.lecture_id);
    let table = await TimetableService.getByMongooseId(user._id, req.params.table_id);
    res.json(table);
  } catch (err) {
    if (err instanceof TimetableNotFoundError)
      return res.status(404).json({errcode:ErrorCode.TIMETABLE_NOT_FOUND, message:"timetable not found"});
    logger.error(err);
    return res.status(500).json({errcode:ErrorCode.SERVER_FAULT, message:"delete lecture failed"});
  }
});

/**
 * DELETE /tables/:id
 * delete a timetable
 */
router.delete('/:id', async function(req, res, next) { // delete
  let context: RequestContext = req['context'];
  let user:User = context.user;
  try {
    await TimetableService.remove(user._id, req.params.id);
    let tableList = await TimetableService.getAbstractListByUserId(user._id);
    res.json(tableList);
  } catch (err) {
    if (err instanceof TimetableNotFoundError)
      return res.status(404).json({errcode:ErrorCode.TIMETABLE_NOT_FOUND, message:"timetable not found"});
    logger.error(err);
    return res.status(500).json({errcode:ErrorCode.SERVER_FAULT, message:"delete timetable failed"});
  }
});

/**
 * POST /tables/:id/copy
 * copy a timetable
 */
router.post('/:id/copy', async function(req, res, next) {
  let context: RequestContext = req['context'];
  let user:User = context.user;
  try {
    let table = await TimetableService.getByMongooseId(user._id, req.params.id);
    if (!table) return res.status(404).json({errcode: ErrorCode.TIMETABLE_NOT_FOUND, message:"timetable not found"});
    await TimetableService.copy(table);
    let tableList = await TimetableService.getAbstractListByUserId(user._id);
    res.json(tableList);
  } catch (err) {
    logger.error("/:id/copy ", err);
    return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message:"copy table failed"});
  }
});

router.put('/:id', async function(req, res, next) {
  let context: RequestContext = req['context'];
  let user:User = context.user;
  if (!req.body.title) return res.status(400).json({errcode: ErrorCode.NO_TIMETABLE_TITLE, message:"should provide title"});

  try {
    await TimetableService.modifyTitle(req.params.id, user._id, req.body.title);
    let tableList = await TimetableService.getAbstractListByUserId(user._id);
    res.json(tableList);
  } catch (err) {
    if (err instanceof DuplicateTimetableTitleError)
      return res.status(403).json({errcode: ErrorCode.DUPLICATE_TIMETABLE_TITLE, message:"duplicate title"});
    if (err instanceof TimetableNotFoundError)
      return res.status(404).json({errcode: ErrorCode.TIMETABLE_NOT_FOUND, message:"timetable not found"});
    logger.error(err);
    return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message:"update timetable title failed"});
  }
});

router.put('/:id/theme', async function(req, res, next) {
  let context: RequestContext = req['context'];
  let user:User = context.user;
  if (req.body.theme === '' || req.body.theme === null || req.body.theme === undefined) {
    return res.status(400).json({errcode: ErrorCode.NO_TIMETABLE_THEME, message:"should provide theme number"});
  }
  if (isNaN(req.body.theme) || parseInt(req.body.theme) >= NUMBER_OF_THEME || parseInt(req.body.theme) < 0) {
    return res.status(400).json({errcode: ErrorCode.INPUT_OUT_OF_RANGE, message: `theme should be between 0 and ${NUMBER_OF_THEME-1}`});
  }

  try {
    await TimetableService.modifyTheme(req.params.id, user._id, req.body.theme);
    let updatedTable = await TimetableService.getByMongooseId(user._id, req.params.id);
    res.json(updatedTable);
  } catch (err) {
    if (err instanceof TimetableNotFoundError)
      return res.status(404).json({errcode: ErrorCode.TIMETABLE_NOT_FOUND, message:"timetable not found"});
    logger.error(err);
    return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message:"update timetable theme failed"});
  }
});

export = router;
