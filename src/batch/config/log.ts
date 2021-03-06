import winston = require('winston');
import DailyRotateFile = require('winston-daily-rotate-file')
import property = require('@app/core/config/property');
import * as Transport from 'winston-transport';

let logPath = property.get("batch.winston.path");
let logDatePattern = property.get("batch.winston.datePattern");
let logLevel = property.get("batch.winston.logLevel");
let daysToKeep = property.get("batch.winston.daysToKeep");

var consoleTransport = new winston.transports.Console();
const transports:Transport[] = [consoleTransport];

if (process.env.EXECUTE_ENV !== 'lambda') {
    var transport = new (DailyRotateFile)({
        filename: logPath,
        datePattern: logDatePattern,
        zippedArchive: true,
        maxFiles: daysToKeep
    });
    transports.push(transport);
}

if (process.env.NODE_ENV !== 'mocha') {
    winston.loggers.add('default', {
        level: logLevel,
        transports: transports,
        format: winston.format.combine(
            winston.format.timestamp({
                format: 'YYYY-MM-DD HH:mm:ss.SSS'
            }),
            winston.format.printf(info => `[${info.timestamp}] [${info.level}] ${(typeof info.message === 'string') ? info.message : JSON.stringify(info.message)}`)
        )
    });
}
