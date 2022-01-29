import ExpressPromiseRouter from "express-promise-router";
import {replaceAllUserId2UserInfo} from "@app/api/middleware/Server2ServerMiddleware";
import * as request from "request-promise-native";
import * as property from '@app/core/config/property';
import UserAuthorizeMiddleware from "@app/api/middleware/UserAuthorizeMiddleware";
import {restGet} from "@app/api/decorator/RestDecorator";
import User from "@app/core/user/model/User";
import ApiError from "@app/api/error/ApiError";
import * as TimetableService from "@app/core/timetable/TimetableService";

let router = ExpressPromiseRouter();

const snuttevDefaultRoutingUrl = property.get('api.snuttev.url')

router.use(UserAuthorizeMiddleware)

restGet(router, '/v1/users/me/lectures/latest')(async function (context, req) {
    const user: User = context.user;
    const evaluationServerHeader = {
        'Snutt-User-Id': user._id
    }
    let snuttLectureInfo: string = JSON.stringify(await TimetableService.getLecturesTakenByUserInLastSemesters(user._id))

    return request({
        method: req.method,
        uri: `${snuttevDefaultRoutingUrl}${req.path}?snutt_lecture_info=${encodeURI(snuttLectureInfo)}`,
        headers: evaluationServerHeader,
        body: req.body,
        json: true
    }).then(async function (body) {
        return body;
    }).catch(function (err) {
        throw new ApiError(err.response.statusCode, err.response.body.code, err.response.body.message);
    });
})

router.all('/*', async function (req, res, next) {
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
