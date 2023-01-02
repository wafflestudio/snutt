import ExpressPromiseRouter from 'express-promise-router';
var router = ExpressPromiseRouter();

import User from '@app/core/user/model/User';
import UserService = require('@app/core/user/UserService');
import UserCredentialService = require('@app/core/user/UserCredentialService');
import UserDeviceService = require('@app/core/user/UserDeviceService');
import AppleService = require('@app/core/apple/AppleService')
import InvalidLocalIdError from '@app/core/user/error/InvalidLocalIdError';
import winston = require('winston');
import InvalidLocalPasswordError from '@app/core/user/error/InvalidLocalPasswordError';
import DuplicateLocalIdError from '@app/core/user/error/DuplicateLocalIdError';
import InvalidFbIdOrTokenError from '@app/core/facebook/error/InvalidFbIdOrTokenError';
import { restPost, restPut } from '../decorator/RestDecorator';
import ApiError from '../error/ApiError';
import ErrorCode from '../enum/ErrorCode';
import InvalidAppleTokenError from "@app/core/apple/error/InvalidAppleTokenError";
import UserCredential from "@app/core/user/model/UserCredential";
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

restPost(router, '/login_apple')(async function (context, req) {
  if (!req.body.apple_token)
    throw new ApiError(400, ErrorCode.NO_APPLE_ID_OR_TOKEN, 'apple_token required');

  try {
    const userInfo = await AppleService.verifyAndDecodeAppleToken(req.body.apple_token, context.appType);

    let credential: UserCredential;
    if (userInfo.transfer_sub != undefined) {
      logger.info("Apple transfer sub exists: " + userInfo.transfer_sub);
      const user = await UserService.getByAppleTransferSub(userInfo.transfer_sub);

      if (user) {
        if (user.credential.appleSub !== userInfo.sub) {
          logger.info("Apple transfer try: " + JSON.stringify(user.credential));
          UserCredentialService.transferAppleCredential(user, userInfo.sub, userInfo.email);
          logger.info("Apple transfer success: " + JSON.stringify(user.credential));
        }
        return {token: user.credentialHash, user_id: user._id};
      } else {
        credential = await UserCredentialService.makeAppleCredential(userInfo.email, userInfo.sub, userInfo.transfer_sub);
        logger.info("Made apple credential with transfer sub: " + JSON.stringify(credential));
      }
    } else {
      logger.info("Apple transfer sub doesn't exist");
      const user = await UserService.getByAppleSub(userInfo.sub);

      if (user) {
        return {token: user.credentialHash, user_id: user._id};
      } else {
        credential = await UserCredentialService.makeAppleCredential(userInfo.email, userInfo.sub);
        logger.info("Made apple credential: " + JSON.stringify(credential));
      }
    }
      
    const credentialHash: string = UserCredentialService.makeCredentialHmac(credential);
    const newUser: User = {
      credential: credential,
      credentialHash: credentialHash,
      email: userInfo.email
    }
    logger.info("New user info: " + JSON.stringify(newUser));
    const inserted: User = await UserService.add(newUser);
    logger.info("Inserted new user: " + JSON.stringify(inserted));
    return {token: inserted.credentialHash, user_id: inserted._id};
  } catch (err) {
    if (err instanceof InvalidAppleTokenError) {
      throw new ApiError(403, ErrorCode.WRONG_APPLE_TOKEN, "wrong apple token");
    }
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


restPost(router, '/password/reset/email/check')(async function (context, req) {
  let userId = req.body.user_id
  if (!userId) {
    throw new ApiError(400, ErrorCode.NO_LOCAL_ID, "아이디를 입력해주세요.")
  }

  let user = await UserService.getByLocalId(req.body.user_id);
  if (!user) {
    throw new ApiError(404, ErrorCode.USER_NOT_FOUND, "해당 아이디로 가입된 사용자가 없습니다.");
  }

  if (!user.email) {
    throw new ApiError(404, ErrorCode.EMAIL_NOT_FOUND, "등록된 이메일이 없습니다.")
  }

  return { email : user.email }
});

restPost(router, '/password/reset/email/send')(async function(context, req) {
  let email = req.body.user_email

  if (!email) {
    throw new ApiError(400, ErrorCode.NO_EMAIL, "이메일을 입력해주세요.")
  }

  let user = await UserService.getByEmail(email)

  if (!user) {
    throw new ApiError(404, ErrorCode.USER_NOT_FOUND, "해당 이메일로 가입된 사용자가 없습니다.");
  }

  await UserService.sendResetPasswordCode(user)

  return {
    message: "ok"
  }
});

restPost(router, '/password/reset/verification/code')(async function(context, req) {
  let userId = req.body.user_id
  let codeSubmitted = req.body.code
  if (!userId || !codeSubmitted) {
    throw new ApiError(400, ErrorCode.NO_LOCAL_ID_OR_CODE, "아이디와 인증코드를 모두 입력해주세요.")
  }

  let user = await UserService.getByLocalId(req.body.user_id);
  if (!user) {
    throw new ApiError(403, ErrorCode.WRONG_ID, "존재하지않는 사용자입니다.");
  }

  let codeVerified = await UserService.verifyResetPasswordCode(user, codeSubmitted)

  if (codeVerified) {
    return {message: "ok"}
  }
});

router.post('/password/reset', async function (req, context) {
 
  let userId = req.body.user_id
  let passwordSubmitted = req.body.password

  if (!userId) {
    throw new ApiError(400, ErrorCode.NO_LOCAL_ID, "아이디를 입력해주세요.")
  }

  if (!passwordSubmitted) {
    throw new ApiError(400, ErrorCode.NO_PASSWORD, "비밀번호를 입력해주세요.")
  }

  let user = await UserService.getByLocalId(req.body.user_id);
  if (!user) {
    throw new ApiError(403, ErrorCode.WRONG_ID, "존재하지않는 사용자입니다.");
  }

  await UserCredentialService.changeLocalPassword(user, passwordSubmitted);

  return {
    message: "ok"
  }
});

restPost(router, '/id/find')(async function(context, req) {
  
  const email = req.body.email

  if (!email) {
    throw new ApiError(400, ErrorCode.NO_EMAIL, "이메일을 입력해주세요.")
  }

  if (!UserService.checkValidEmail(email)) {
    throw new ApiError(400, ErrorCode.INVALID_EMAIL, "올바른 이메일을 입력해주세요.")
  }

  const user = await UserService.getByEmail(email)

  if (!user || !user.email || !user.credential.localId ) {
    throw new ApiError(404, ErrorCode.USER_NOT_FOUND, "해당 이메일로 가입된 사용자가 없습니다.");
  }

  UserService.sendLocalId(user)
  
  return { message : "ok" }
});

export = router;
