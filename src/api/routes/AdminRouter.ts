import ExpressPromiseRouter from 'express-promise-router';
import User from '@app/core/user/model/User';
import UserService = require('@app/core/user/UserService');
import UserCredentialService = require('@app/core/user/UserCredentialService');
import NotificationService = require('@app/core/notification/NotificationService');
import CourseBookService = require('@app/core/coursebook/CourseBookService');
import FcmLogService = require('@app/core/fcm/FcmLogService');
import AdminService = require('@app/core/admin/AdminService');
import NotificationTypeEnum from '@app/core/notification/model/NotificationTypeEnum';
import winston = require('winston');
import NoFcmKeyError from '@app/core/notification/error/NoFcmKeyError';
import InvalidNotificationDetailError from '@app/core/notification/error/InvalidNotificationDetailError';
import RequestContext from '../model/RequestContext';
import { restPost, restGet } from '../decorator/RestDecorator';
import ApiError from '../error/ApiError';
import ApiServerFaultError from '../error/ApiServerFaultError';
import ErrorCode from '../enum/ErrorCode';
import UserAuthorizeMiddleware from '../middleware/UserAuthorizeMiddleware';
import InvalidLocalPasswordError from '@app/core/user/error/InvalidLocalPasswordError';
var logger = winston.loggers.get('default');

var router = ExpressPromiseRouter();

router.use(UserAuthorizeMiddleware);

router.use(function(req, res, next) {
  let context: RequestContext = req['context'];
  if (context.user.isAdmin) return next();
  else {
    return res.status(403).json({ errcode: ErrorCode.NO_ADMIN_PRIVILEGE, message: 'Admin privilege required.' });
  }
});

restPost(router, '/insert_noti')(async function (context, req) {
  let sender: User = context.user;

  let userId: string     = req.body.user_id;
  let title: string      = req.body.title;
  let body: string       = req.body.body;
  let insertFcm: boolean = req.body.insert_fcm ? true            : false;
  let type               = req.body.type       ? Number(req.body.type)   : NotificationTypeEnum.NORMAL;
  let detail             = req.body.detail     ? req.body.detail : null;

  try {
    if (userId && userId.length > 0) {
      let receiver = await UserService.getByLocalId(userId);
      if (!receiver) {
        throw new ApiError(404, ErrorCode.USER_NOT_FOUND, "user not found");
      }
      if (insertFcm) {
        await NotificationService.sendFcmMsg(receiver, title, body, sender._id, "admin");
      }
      await NotificationService.add({
        user_id: receiver._id,
        message: body,
        type: type,
        detail: detail,
        created_at: new Date()
      });
    } else {
      if (insertFcm) {
        await NotificationService.sendGlobalFcmMsg(title, body, sender._id, "admin");
      }
      await NotificationService.add({
        user_id: null,
        message: body,
        type: type,
        detail: detail,
        created_at: new Date()
      });
    }
    return {
      message: "ok"
    };
  } catch (err) {
    if (err instanceof NoFcmKeyError)
      throw new ApiError(404, ErrorCode.USER_HAS_NO_FCM_KEY, "user has no fcm key");
    if (err instanceof InvalidNotificationDetailError)
      throw new ApiError(404, ErrorCode.INVALID_NOTIFICATION_DETAIL, "invalid notification detail");
    if (err instanceof ApiError) {
      throw err;
    } 
    logger.error(err);
    throw new ApiServerFaultError();
  }
})

restPost(router, '/change_pw')(async function (context, req) {
  let userId: string = req.body.userId;
  let toPassword: string = req.body.toPassword;

  try {
    let user: User = await UserService.getByMongooseId(userId);
    if (user === null) {
      throw new ApiError(404, ErrorCode.USER_NOT_FOUND, "user not found");
    }
    if (!UserCredentialService.hasLocal(user)) {
      throw new ApiError(403, ErrorCode.NOT_LOCAL_ACCOUNT, "not local account");
    }
    UserCredentialService.changeLocalPassword(user, toPassword);
    return {
      message: "ok"
    }
  } catch (err) {
    if (err instanceof InvalidLocalPasswordError) {
      throw new ApiError(403, ErrorCode.INVALID_PASSWORD, "invalid password");
    } else if (err instanceof ApiError) {
      throw err;
    } 
    logger.error(err);
    throw new ApiServerFaultError();
  }
})

restGet(router, '/recent_fcm_log')(FcmLogService.getRecentFcmLog)

restGet(router, '/coursebooks')(CourseBookService.getAll)

restGet(router, '/statistics')(AdminService.getStatistics);

export = router;