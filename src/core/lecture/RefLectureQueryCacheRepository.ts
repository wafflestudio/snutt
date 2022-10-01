import crypto = require('crypto');
import RedisKeyUtil = require('@app/core/redis/RedisKeyUtil');
import RedisUtil = require('@app/core/redis/RedisUtil');
import RefLecture from './model/RefLecture';

export async function getListOfLectureListCacheFromPageList(query: any, pageList: number[]): Promise<RefLecture[][] | null> {
    let queryHash = makeMd5HashFromObject(query);
    let keyList = pageList.map(page => RedisKeyUtil.getLectureQueryKey(queryHash, page))
    let lectureListStringList = await RedisUtil.mget(keyList);
    let lectureListList: RefLecture[][] = lectureListStringList.map(parseLectureListString);
    return lectureListList;
}

function parseLectureListString(str: string): RefLecture[] | null {
    let lectureList: RefLecture[] = JSON.parse(str);
    if (!lectureList || typeof lectureList.length !== 'number') {
        return null;
    } else {
        return lectureList;
    }
}

export async function setLectureListCache(query: any, page: number, lectureList: RefLecture[]): Promise<void> {
    let queryHash = makeMd5HashFromObject(query);
    let key = RedisKeyUtil.getLectureQueryKey(queryHash, page);
    let lectureListString = JSON.stringify(lectureList);
    await RedisUtil.setex(key, 60 * 60 * 24, lectureListString);
}

function makeMd5HashFromObject(object: any) {
    return crypto.createHash('md5').update(JSON.stringify(object)).digest('hex');
}
