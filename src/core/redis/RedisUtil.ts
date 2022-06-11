import redis = require('redis');
import winston = require('winston');
import RedisClientError from './error/RedisClientError';

let redisClient: redis.RedisClient = global['redisClient'];

function checkRedisClient() {
    if (!redisClient) {
        if (global['redisClient']) {
            redisClient = global['redisClient'];
        } else {
            throw new RedisClientError("Redis not connected");
        }
    }
}

export async function pollRedisClient(): Promise<redis.RedisClient> {
    if (redisClient) return redisClient;

    function checkRedisClientTimeoutWithoutReject(timeout: number) {
        return new Promise<redis.RedisClient>(function(resolve, reject) {
            setTimeout(function() {
                try {
                    checkRedisClient();
                    resolve(redisClient);
                } catch (err) {
                }
            }, timeout);
        })
    }

    function checkRedisClientTimeout(timeout: number) {
        return new Promise<redis.RedisClient>(function(resolve, reject) {
            setTimeout(function() {
                try {
                    checkRedisClient();
                    resolve(redisClient);
                } catch (err) {
                    reject(err);
                }
            }, timeout);
        })
    }

    return await Promise.race([
        checkRedisClientTimeoutWithoutReject(100),
        checkRedisClientTimeoutWithoutReject(200),
        checkRedisClientTimeoutWithoutReject(500),
        checkRedisClientTimeout(1000)]);
}

export function configGet(key: string): Promise<string> {
    checkRedisClient();
    return new Promise(function (resolve, reject) {
        redisClient.sendCommand("config", ["get", key], function(err, values: string[]) {
            if (err) {
                reject(err);
            } else {
                resolve(values[1]);
            }
        })
    });
}

export function flushdb(): Promise<string> {
    checkRedisClient();
    return new Promise(function (resolve, reject) {
        redisClient.flushdb(function(err, value: string) {
            if (err) {
                reject(err);
            } else {
                resolve(value);
            }
        })
    });
}

export function get(key: string): Promise<string> {
    checkRedisClient();
    return new Promise(function (resolve, reject) {
        redisClient.get(key, function(err, value: string) {
            if (err) {
                reject(err);
            } else {
                resolve(value);
            }
        })
    })
}

export function set(key: string, value: string): Promise<void> {
    checkRedisClient();
    return new Promise(function (resolve, reject) {
        redisClient.set(key, value, function(err) {
            if (err) {
                reject(err);
            } else {
                resolve();
            }
        })
    })
}

export function setex(key: string, seconds: number, value: string) {
  checkRedisClient();
  return new Promise(function (resolve, reject) {
    redisClient.setex(key, seconds, value, function (err) {
      if (err) {
        reject(err);
      } else {
        resolve();
      }
    })
  })
}

export function mget(keyList: string[]): Promise<string[]> {
    checkRedisClient();
    return new Promise(function (resolve, reject) {
        redisClient.mget(...keyList, function(err, value: string[]) {
            if (err) {
                reject(err);
            } else {
                resolve(value);
            }
        })
    })
}
