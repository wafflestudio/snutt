import ExpressPromiseRouter from "express-promise-router";
import {replaceAllUserId2UserInfo} from "@app/api/middleware/Server2ServerMiddleware";
import * as request from "request-promise-native";
import * as property from '@app/core/config/property';
import UserAuthorizeMiddleware from "@app/api/middleware/UserAuthorizeMiddleware";

let router = ExpressPromiseRouter();

router.use(UserAuthorizeMiddleware)

router.all('/*', async function (req, res, next) {
    const snuttevDefaultRoutingUrl = property.get('api.snuttev.url')
    const evaluationServerHeader = {
        'Snutt-User-Id': req['context'].user._id
    }
    try {
        return request({
            method: req.method,
            uri: snuttevDefaultRoutingUrl + req.url,
            headers: evaluationServerHeader,
            body: req.body,
            json: true
        }).then(async function (body) {
            if (body !== undefined) {
                await replaceAllUserId2UserInfo(body)
            }
            return res.json(body);
        }).catch(function (err) {
            return res.status(err.response.statusCode).json(err.response.body);
        });
    } catch (err) {
        next(err)
    }
})

export = router;
