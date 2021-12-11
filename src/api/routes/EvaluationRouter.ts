import ExpressPromiseRouter from "express-promise-router";
import ApiError from "@app/api/error/ApiError";
import {replaceAllUserId2UserInfo} from "@app/api/middleware/Server2ServerMiddleware";
import * as request from "request-promise-native";
import * as property from '@app/core/config/property';
import UserAuthorizeMiddleware from "@app/api/middleware/UserAuthorizeMiddleware";

let router = ExpressPromiseRouter();

router.use(UserAuthorizeMiddleware)

router.all('/*', async function (req, res, next) {
    const snuttevDefaultRoutingUrl = property.get('api.snuttev.url') + '/v1/'
    const evaluationServerHeader = {
        'Snutt-User-Id': req['context'].user._id
    }
    try {
        return request({
            method: req.method,
            uri: snuttevDefaultRoutingUrl + req.originalUrl,
            headers: evaluationServerHeader,
            body: req.body,
            json: true
        }).then(async function (body) {
            await replaceAllUserId2UserInfo(body)
            return res.json(body);
        }).catch(function (err) {
            throw new ApiError(err.response.statusCode, err.response.errorCode, err.response.statusMessage)
        });
    } catch (err) {
        next(err)
    }
})

export = router;
