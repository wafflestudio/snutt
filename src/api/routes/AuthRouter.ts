import ExpressPromiseRouter from 'express-promise-router';
var router = ExpressPromiseRouter();

import User from '@app/core/user/model/User';
import UserService = require('@app/core/user/UserService');
import UserCredentialService = require('@app/core/user/UserCredentialService');
import UserDeviceService = require('@app/core/user/UserDeviceService');
import InvalidLocalIdError from '@app/core/user/error/InvalidLocalIdError';
import winston = require('winston');
import InvalidLocalPasswordError from '@app/core/user/error/InvalidLocalPasswordError';
import DuplicateLocalIdError from '@app/core/user/error/DuplicateLocalIdError';
import InvalidFbIdOrTokenError from '@app/core/facebook/error/InvalidFbIdOrTokenError';
import { restPost } from '../decorator/RestDecorator';
import ApiError from '../error/ApiError';
import ErrorCode from '../enum/ErrorCode';
var logger = winston.loggers.get('default');

restPost(router, '/request_temp')(async function(context, req) {
    let credential = await UserCredentialService.makeTempCredential();
    let credentialHash = await UserCredentialService.makeCredentialHmac(credential);
    let user: User = {
      credential: credential,
      credentialHash: credentialHash
    }
    let inserted = await UserService.add(user);
    return {message:"ok", token: inserted.credentialHash, user_id: inserted._id};
});

restPost(router, '/login_local')(async function(context, req) {
  let user = await UserService.getByLocalId(req.body.id);
  if (!user) {
    throw new ApiError(403, ErrorCode.WRONG_ID, "wrong id");
  }
  let passwordMatch = await UserCredentialService.isRightPassword(user, req.body.password);
  if (!passwordMatch) {
    throw new ApiError(403, ErrorCode.WRONG_PASSWORD, "wrong password");
  }
  return {token: user.credentialHash, user_id: user._id};
});

restPost(router, '/register_local')(async function (context, req) {
  try {
    let credential = await UserCredentialService.makeLocalCredential(req.body.id, req.body.password);
    let credentialHash = await UserCredentialService.makeCredentialHmac(credential);
    let user: User = {
      credential: credential,
      credentialHash: credentialHash,
      email: req.body.email
    }
    let inserted = await UserService.add(user);
    return {message: "ok", token: inserted.credentialHash, user_id: inserted._id};
  } catch (err) {
    if (err instanceof InvalidLocalIdError)
      throw new ApiError(403, ErrorCode.INVALID_ID, "invalid id");
    if (err instanceof DuplicateLocalIdError)
      throw new ApiError(403, ErrorCode.DUPLICATE_ID, "duplicate id");
    if (err instanceof InvalidLocalPasswordError)
      throw new ApiError(403, ErrorCode.INVALID_PASSWORD, "invalid password");
    throw err;
  }
});

restPost(router, '/login_fb')(async function(context, req) {
  if (!req.body.fb_token || !req.body.fb_id)
    throw new ApiError(400, ErrorCode.NO_FB_ID_OR_TOKEN, "both fb_id and fb_token required");

  try {
    let user = await UserService.getByFb(req.body.fb_id);
    if (user) {
      if (await UserCredentialService.isRightFbToken(user, req.body.fb_token)) {
        return {token: user.credentialHash, user_id: user._id};
      } else {
        throw new ApiError(403, ErrorCode.WRONG_FB_TOKEN, "wrong fb token");
      }
    } else {
      let credential = await UserCredentialService.makeFbCredential(req.body.fb_id, req.body.fb_token);
      logger.info("Made fb credential: " + JSON.stringify(credential));
      let credentialHash = await UserCredentialService.makeCredentialHmac(credential);
      let newUser: User = {
        credential: credential,
        credentialHash: credentialHash,
        email: req.body.email
      }
      logger.info("New user info: " + JSON.stringify(newUser));
      let inserted = await UserService.add(newUser);
      logger.info("Inserted new user: " + JSON.stringify(inserted));
      return {token: inserted.credentialHash, user_id: inserted._id};
    }
  } catch (err) {
    if (err instanceof InvalidFbIdOrTokenError) {
      throw new ApiError(403, ErrorCode.WRONG_FB_TOKEN, "wrong fb token");
    }
    throw err;
  }
});

restPost(router, '/logout')(async function(contedt, req) {
  let userId = req.body.user_id;
  let registrationId = req.body.registration_id;
  let user = await UserService.getByMongooseId(userId);
  if (!user) {
    throw new ApiError(404, ErrorCode.USER_NOT_FOUND, "user not found");
  }
  await UserDeviceService.detachDevice(user, registrationId);
  return {message: "ok"};
});

export = router;
