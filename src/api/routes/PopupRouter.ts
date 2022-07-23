import ExpressPromiseRouter from 'express-promise-router';
var router = ExpressPromiseRouter();
import User from '@app/core/user/model/User';
import PopupService = require('@app/core/popup/PopupService');

import { restGet } from '../decorator/RestDecorator';
import UserAuthorizeMiddleware from '../middleware/UserAuthorizeMiddleware';

router.use(UserAuthorizeMiddleware);

restGet(router, '/')(async function(context, req){
  var user:User = context.user;

  let popups = await PopupService.getPopups(user, context.osType, context.osVersion, context.appType, context.appVersion);
  return popups;
});

export = router;
