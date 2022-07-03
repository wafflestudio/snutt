import RequestContext from "../model/RequestContext";
import ErrorCode from "../enum/ErrorCode";
import ApiError from "../error/ApiError";

export default async function(req, res) {
    const osType = <string>req.headers['x-os-type'];
    const osVersion = <string>req.headers['x-os-version'];
    const appType = <string>req.headers['x-app-type'];
    const appVersion = <string>req.headers['x-app-version'];

    let context: RequestContext = req['context'];

    if (osType !== undefined) {
        if (osType === 'ios' || osType === 'android') {
            context.osType = osType;
        } else {
            throw new ApiError(400, ErrorCode.INVALID_OS_TYPE, `Invalid os type: ${osType}`);
        }
    }

    if (osVersion !== undefined) {
        context.osVersion = osVersion;
    }

    if (appType !== undefined) {
        if (appType === 'release' || appType === 'debug') {
            context.appType = appType;
        } else {
            throw new ApiError(400, ErrorCode.INVALID_APP_TYPE, `Invalid app type: ${appType}`);
        }
    }

    if (appVersion !== undefined) {
        context.appVersion = appVersion;
    }

    return Promise.resolve('next');
}
