import ExpressPromiseRouter from 'express-promise-router';

import User from '@app/core/user/model/User';
import InvalidLocalPasswordError from '@app/core/user/error/InvalidLocalPasswordError';
import InvalidLocalIdError from '@app/core/user/error/InvalidLocalIdError';
import AlreadyRegisteredFbIdError from '@app/core/user/error/AlreadyRegisteredFbIdError';
import DuplicateLocalIdError from '@app/core/user/error/DuplicateLocalIdError';
import RequestContext from '../model/RequestContext';
import ErrorCode from '../enum/ErrorCode';
import {restDelete, restGet, restPost} from '../decorator/RestDecorator';
import UserAuthorizeMiddleware from '../middleware/UserAuthorizeMiddleware';
import {
  isUserEmailVerified,
  resetEmailVerification,
  sendVerificationCode,
  verifyEmail
} from "@app/core/user/UserService";
import winston = require('winston');
import UserCredentialService = require('@app/core/user/UserCredentialService');
import UserService = require('@app/core/user/UserService');
import UserDeviceService = require('@app/core/user/UserDeviceService');

var logger = winston.loggers.get('default');
var router = ExpressPromiseRouter();

router.use(UserAuthorizeMiddleware);

restGet(router, '/info')(async function (context, req) {
  let user: User = context.user;
  return UserService.getUserInfo(user);
});

restDelete(router, '/email/verification')(async function (context, req) {
  const user: User = context.user;
  await resetEmailVerification(user)
  return {message: "ok"}
})

restPost(router, '/email/verification')(async function (context, req) {
  const user: User = context.user;
  await sendVerificationCode(user, req.body.email)
  return {message: "ok"};
})

restPost(router, '/email/verification/code')(async function (context, req) {
  const user: User = context.user;
  const result = await verifyEmail(user, req.body.code)
  return {is_email_verified: result}
})

restGet(router, '/email/verification')(async function (context, req) {
  const user: User = context.user
  return {is_email_verified: isUserEmailVerified(user)};
})

router.post('/password', async function (req, res, next) {
  let context: RequestContext = req['context'];
  let user: User = context.user;
  if (UserCredentialService.hasLocal(user)) return res.status(403).json({
    errcode: ErrorCode.ALREADY_LOCAL_ACCOUNT,
    message: "already have local id"
  });
  try {
    await UserCredentialService.attachLocal(user, req.body.id, req.body.password);
  } catch (err) {
    if (err instanceof InvalidLocalPasswordError)
      return res.status(403).json({errcode: ErrorCode.INVALID_PASSWORD, message: "invalid password"});
    else if (err instanceof InvalidLocalIdError)
      return res.status(403).json({errcode: ErrorCode.INVALID_ID, message: "invalid id"});
    else if (err instanceof DuplicateLocalIdError)
      return res.status(403).json({errcode: ErrorCode.DUPLICATE_ID, message: "duplicate id"});
    logger.error(err);
    return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message: "server fault"});
  }
  res.json({token: user.credentialHash});
});

router.put('/password', async function (req, res, next) {
  let context: RequestContext = req['context'];
  let user: User = context.user;
  if (!UserCredentialService.hasLocal(user)) return res.status(403).json({
    errcode: ErrorCode.NOT_LOCAL_ACCOUNT,
    message: "no local id"
  });
  try {
    let result = await UserCredentialService.isRightPassword(user, req.body.old_password);
    if (!result) return res.status(403).json({errcode: ErrorCode.WRONG_PASSWORD, message: "wrong old password"});
    await UserCredentialService.changeLocalPassword(user, req.body.new_password);
  } catch (err) {
    if (err instanceof InvalidLocalPasswordError)
      return res.status(403).json({errcode: ErrorCode.INVALID_PASSWORD, message: "invalid password"});
    logger.error(err);
    return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message: "server fault"});
  }
  res.json({token: user.credentialHash});
});

// Credential has been modified. Should re-send token
router.post('/facebook', async function (req, res, next) {
  let context: RequestContext = req['context'];
  let user: User = context.user;
  if (!req.body.fb_token || !req.body.fb_id)
    return res.status(400).json({errcode: ErrorCode.NO_FB_ID_OR_TOKEN, message: "both fb_id and fb_token required"});

  let fbToken = req.body.fb_token;
  let fbId = req.body.fb_id;

  try {
    if (UserCredentialService.hasFb(user)) {
      return res.status(403).json({errcode: ErrorCode.ALREADY_FB_ACCOUNT, message: "already attached"});
    }
    await UserCredentialService.attachFb(user, fbId, fbToken);
    return res.json({token: user.credentialHash});
  } catch (err) {
    if (err instanceof AlreadyRegisteredFbIdError) {
      return res.status(403).json({
        errcode: ErrorCode.FB_ID_WITH_SOMEONE_ELSE,
        message: "already attached with this fb_id"
      });
    } else {
      logger.error(err);
      return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message: "server error"});
    }
  }
});

router.delete('/facebook', async function (req, res, next) {
  let context: RequestContext = req['context'];
  let user: User = context.user;
  if (!UserCredentialService.hasFb(user)) return res.status(403).json({
    errcode: ErrorCode.NOT_FB_ACCOUNT,
    message: "not attached yet"
  });
  if (!UserCredentialService.hasLocal(user)) return res.status(403).json({
    errcode: ErrorCode.NOT_LOCAL_ACCOUNT,
    message: "no local id"
  });
  try {
    await UserCredentialService.detachFb(user);
  } catch (err) {
    return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message: "server error"});
  }

  return res.json({token: user.credentialHash});
});

router.get('/facebook', function (req, res, next) {
  let context: RequestContext = req['context'];
  let user: User = context.user;
  return res.json({attached: UserCredentialService.hasFb(user), name: user.credential.fbName});
});

router.post('/device/:registration_id', async function (req, res, next) {
  let context: RequestContext = req['context'];
  let user: User = context.user;
  try {
    await UserDeviceService.attachDevice(user, req.params.registration_id);
  } catch (err) {
    logger.error("error: ", err, ", registrationId: ", req.params.registration_id, ", platform: ", context.platform);
    return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message: err});
  }
  res.json({message: "ok"});
});

router.delete('/device/:registration_id', async function (req, res, next) {
  let context: RequestContext = req['context'];
  let user: User = context.user;
  try {
    await UserDeviceService.detachDevice(user, req.params.registration_id);
  } catch (err) {
    logger.error("error: ", err, ", registrationId: ", req.params.registration_id, ", platform: ", context.platform);
    return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, message: err});
  }
  res.json({message: "ok"});
});

router.delete('/account', async function (req, res, next) {
  let context: RequestContext = req['context'];
  let user: User = context.user;
  try {
    await UserService.deactivate(user);
  } catch (err) {
    logger.error(err);
    return res.status(500).json({errcode: ErrorCode.SERVER_FAULT, messsage: "server fault"});
  }
  res.json({message: "ok"});
});

export = router;
