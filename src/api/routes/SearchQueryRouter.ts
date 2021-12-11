import ExpressPromiseRouter from 'express-promise-router';
var router = ExpressPromiseRouter();
import RefLectureQueryService = require('@app/core/lecture/RefLectureQueryService');
import InvalidLectureTimemaskError from '@app/core/lecture/error/InvalidLectureTimemaskError';
import { restPost } from '../decorator/RestDecorator';
import ApiError from '../error/ApiError';
import ErrorCode from '../enum/ErrorCode';

restPost(router, '/')(async function(context, req) {
  if (!req.body.year || !req.body.semester) {
    throw new ApiError(400, ErrorCode.NO_YEAR_OR_SEMESTER, "no year or semester");
  }

  var query: any = req.body;
  try {
    RefLectureQueryService.addQueryLogAsync(query);
    return await RefLectureQueryService.getLectureListByLectureQuery(query);
  } catch (err) {
    if (err instanceof InvalidLectureTimemaskError) {
      throw new ApiError(400, ErrorCode.INVALID_TIMEMASK, "invalid timemask");
    }
    throw err;
  }
});

export = router;
