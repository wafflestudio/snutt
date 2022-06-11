import winston = require('winston');
import RedisUtil = require('@app/core/redis/RedisUtil');

let logger = winston.loggers.get('default');

RedisUtil.pollRedisClient().then(function() {
  logger.info('Flushing all redis data');
  RedisUtil.flushdb();
}).catch(function(err) {
  logger.error("Failed to flush redis");
});
