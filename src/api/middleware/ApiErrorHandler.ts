import winston = require('winston');
import RequestContext from '@app/api/model/RequestContext';
import ApiError from '../error/ApiError';
import ApiServerFaultError from '../error/ApiServerFaultError';

let logger = winston.loggers.get('default');

export default function(err, req, res, next) {
    let context: RequestContext = req["context"];
    if (err instanceof ApiError) {
        res.status(err.statusCode).json({
            errcode: err.errorCode,
            message: err.message,
            ext: err.ext
        });
    } else {
        logger.error({
            method: context.method,
            url: context.url,
            platform: context.platform,
            cause: err
        });
        let serverFaultError = new ApiServerFaultError();
        res.status(serverFaultError.statusCode).json({
            errcode: serverFaultError.errorCode,
            message: serverFaultError.message
        });
    }
    logger.error(err.stack);
}
