import redis = require('redis');
import winston = require('winston');
import property = require('@app/core/config/property');
import RedisUtil = require('@app/core/redis/RedisUtil');
let logger = winston.loggers.get('default');

let client = redis.createClient({
    url: property.get('core.redis.url')
});

client.on('connect', async function() {
    logger.info('Redis client connected');
    global['redisClient'] = client;

    let maxmemory = await RedisUtil.configGet('maxmemory');
    let maxmemoryPolicy = await RedisUtil.configGet('maxmemory-policy');
    logger.info("Redis maxmemory: " + maxmemory);
    logger.info("Redis maxmemory-policy: " + maxmemoryPolicy);
    if (Number(maxmemory) === 0) {
        logger.error("Redis maxmemory infinite. It's highly recommended to set maxmemory");
    }
    if (maxmemoryPolicy !== "allkeys-lru") {
        logger.error("Redis eviction policy is not allkeys-lru");
    }
});

client.on('error', function (err) {
    logger.error('Redis client error: ' + err);
});
