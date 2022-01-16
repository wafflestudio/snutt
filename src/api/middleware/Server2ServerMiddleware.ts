import RequestContext from "@app/api/model/RequestContext";
// import UserService = require('@app/core/user/UserService');
import * as UserService from "@app/core/user/UserService";

export async function replaceAllUserId2UserInfo(body) {
    async function recursivelyChangeUserId2UserInfo(o) {
        if (o === null) {
            return Promise.resolve('next')
        }
        return Promise.all(
            Object.keys(o).map(function (key) {
                if (typeof o[key] === 'object') {
                    return recursivelyChangeUserId2UserInfo(o[key]);
                } else {
                    if (key === 'user_id') {
                        return new Promise(
                            async (resolve, reject) => {
                                o['user'] = UserService.getUserInfoAsSnakeCase(await UserService.getByMongooseId(o[key]))
                                delete o[key]
                                resolve('next')
                            }
                        );
                    }
                    return Promise.resolve('next')
                }
            })
        )
    }
    await recursivelyChangeUserId2UserInfo(body);

    return Promise.resolve('next');
}
