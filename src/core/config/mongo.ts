import mongoose = require('mongoose');
import property = require('@app/core/config/property');
import winston = require('winston');

var logger = winston.loggers.get('default');

// nodejs Promise를 사용
mongoose.Promise = global.Promise;

mongoose.connect(property.get('core.mongo.uri'), function(err) {
  if(err) {
    logger.error(err);
    return;
  }
  logger.info('MongoDB connected');
});
