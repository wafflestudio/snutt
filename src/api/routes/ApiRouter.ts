import ExpressPromiseRouter from 'express-promise-router';

var router = ExpressPromiseRouter();

import CourseBookService = require('@app/core/coursebook/CourseBookService');

import AuthRouter = require('./AuthRouter');
import TimetableRouter = require('./TimetableRouter');
import SearchQueryRouter = require('./SearchQueryRouter');
import TagListRouter = require('./TagListRouter');
import NotificationRouter = require('./NotificationRouter');
import EvaluationRouter = require('./EvaluationRouter')
import UserRouter = require('./UserRouter');
import AdminRouter = require('./AdminRouter');
import FeedbackService = require('@app/core/feedback/FeedbackService');
import LectureColorService = require('@app/core/timetable/LectureColorService');
import SugangSnuSyllabusService = require('@app/core/coursebook/sugangsnu/SugangSnuSyllabusService')

import { restGet, restPost } from '../decorator/RestDecorator';
import ErrorCode from '../enum/ErrorCode';
import ApiError from '../error/ApiError';
import ApiKeyAuthorizeMiddleware from '../middleware/ApiKeyAuthorizeMiddleware';
import PublicApiCacheControlMiddleware from '../middleware/PublicApiCacheControlMiddleware';

router.use(ApiKeyAuthorizeMiddleware);
router.use(PublicApiCacheControlMiddleware);

restGet(router, '/course_books')(CourseBookService.getAll);

restGet(router, '/course_books/recent')(CourseBookService.getRecent);

restGet(router, '/course_books/official')(async function(context, req) {
  var year = req.query.year;
  var semester = Number(req.query.semester);
  var lecture_number = req.query.lecture_number;
  var course_number = req.query.course_number;
  return {
    url: SugangSnuSyllabusService.getSyllabusUrl(year, semester, lecture_number, course_number)
  };
});

router.use('/search_query', SearchQueryRouter);

router.use('/tags', TagListRouter);

restGet(router, '/colors')(async function(context, req) {
  return {message: "ok", colors: LectureColorService.getLegacyColors(), names: LectureColorService.getLegacyNames()};
});

restGet(router, '/colors/:colorName')(async function(context, req) {
  let colorWithName = LectureColorService.getColorList(req.params.colorName);
  if (colorWithName) return {message: "ok", colors: colorWithName.colors, names: colorWithName.names};
  else throw new ApiError(404, ErrorCode.COLORLIST_NOT_FOUND, "color list not found");
});

// deprecated
restGet(router, '/app_version')(async function() {
  throw new ApiError(404, ErrorCode.UNKNOWN_APP, "unknown app");
});

restPost(router, '/feedback')(async function(context, req) {
  await FeedbackService.add(req.body.email, req.body.message, context.platform);
  return {message:"ok"};
});

router.use('/ev-service', EvaluationRouter)

router.use('/auth', AuthRouter);

router.use('/tables', TimetableRouter);

router.use('/user', UserRouter);

router.use('/notification', NotificationRouter);

router.use('/admin', AdminRouter);

export = router;
