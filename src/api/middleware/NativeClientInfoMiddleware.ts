import RequestContext from "../model/RequestContext";
import ErrorCode from "../enum/ErrorCode";
import ApiError from "../error/ApiError";

export default async function (req, res) {
  const versionRegex = /^(\d+\.)?(\d+\.)?(\*|\d+)$/;

  const osType = <string>req.headers['x-os-type'];
  const osVersion = <string>req.headers['x-os-version'];
  const appType = <string>req.headers['x-app-type'];
  const appVersion = <string>req.headers['x-app-version'];

  const context: RequestContext = req['context'];

  if (osType != undefined) {
    if (osType === 'ios' || osType === 'android') {
      context.osType = osType;
    } else {
      throw new ApiError(400, ErrorCode.INVALID_OS_TYPE, `Invalid os type: ${osType}`);
    }
  }

  if (osVersion != undefined) {
    if (osVersion.match(versionRegex)) {
      context.osVersion = osVersion;
    } else {
      throw new ApiError(400, ErrorCode.INVALID_OS_VERSION, `Invalid os version: ${osVersion}`);
    }
  }

  if (appType != undefined) {
    if (appType === 'release' || appType === 'debug') {
      context.appType = appType;
    } else {
      throw new ApiError(400, ErrorCode.INVALID_APP_TYPE, `Invalid app type: ${appType}`);
    }
  } else {
    context.appType = 'release';
  }

  if (appVersion != undefined) {
    if (osType === 'android') {
      if (!appVersion.split('-')[0].match(versionRegex)) {
        throw new ApiError(400, ErrorCode.INVALID_APP_VERSION, `Invalid app version: ${appVersion}`);
      }
    }
    if (!appVersion.match(versionRegex)) {
      throw new ApiError(400, ErrorCode.INVALID_APP_VERSION, `Invalid app version: ${appVersion}`);
    }
    context.appVersion = appVersion;
  }

  return Promise.resolve('next');
}
