import bcrypt = require('bcrypt');
import crypto = require('crypto');

import property = require('@app/core/config/property');
import User from '@app/core/user/model/User';
import UserCredential from '@app/core/user/model/UserCredential';
import UserService = require('@app/core/user/UserService');
import FacebookService = require('@app/core/facebook/FacebookService');
import InvalidLocalPasswordError from '@app/core/user/error/InvalidLocalPasswordError';
import InvalidLocalIdError from '@app/core/user/error/InvalidLocalIdError';
import DuplicateLocalIdError from '@app/core/user/error/DuplicateLocalIdError';
import AlreadyRegisteredFbIdError from '@app/core/user/error/AlreadyRegisteredFbIdError';
import InvalidFbIdOrTokenError from '@app/core/facebook/error/InvalidFbIdOrTokenError';
import NotLocalAccountError from './error/NotLocalAccountError';
import winston = require('winston');
var logger = winston.loggers.get('default');

let secretKey = property.get('core.secretKey');

export async function isRightPassword(user: User, password: string): Promise<boolean> {
    let originalHash = user.credential.localPw;
    if (!password || !originalHash) return false;

    return await bcrypt.compare(password, originalHash);
}

export async function isRightFbToken(user: User, fbToken: string): Promise<boolean> {
    try {
        let fbInfo = await FacebookService.getFbInfo(user.credential.fbId, fbToken);
        return fbInfo.fbId === user.credential.fbId;
    } catch (err) {
        return false;
    }
}

export function compareCredentialHash(user: User, hash: string): boolean {
    return user.credentialHash === hash;
}

export function makeCredentialHmac(userCredential: UserCredential): string {
    var hmac = crypto.createHmac('sha256', secretKey);
    hmac.update(JSON.stringify(userCredential));
    return hmac.digest('hex');
}

async function modifyCredential(user: User): Promise<void> {
    user.credentialHash = makeCredentialHmac(user.credential);
    await UserService.modify(user);
}


function validatePassword(password: string): void {
    if (!password || !password.match(/^(?=.*\d)(?=.*[a-z])\S{6,20}$/i)) {
        throw new InvalidLocalPasswordError(password);
    }
}

function makePasswordHash(password: string): Promise<string> {
    return bcrypt.hash(password, 4);
}

export async function changeLocalPassword(user: User, password: string): Promise<void> {
    validatePassword(password);
    let passwordHash = await makePasswordHash(password);
    user.credential.localPw = passwordHash;
    await modifyCredential(user);
}

export function hasFb(user: User): boolean {
    return user.credential.fbId !== null && user.credential.fbId !== undefined;
}

export function hasLocal(user: User): boolean {
    return user.credential.localId !== null && user.credential.localId !== undefined;
}

export async function attachFb(user: User, fbId: string, fbToken: string): Promise<void> {
    if (!fbId) {
        throw new InvalidFbIdOrTokenError(fbId, fbToken);
    }

    let fbCredential = await makeFbCredential(fbId, fbToken);
    user.credential.fbName = fbCredential.fbName;
    user.credential.fbId = fbCredential.fbId;
    await modifyCredential(user);
}

export async function detachFb(user: User): Promise<void> {
    if (!hasLocal(user)) {
        return Promise.reject(new NotLocalAccountError(user._id));
    }
    user.credential.fbName = null;
    user.credential.fbId = null;
    await modifyCredential(user);
}

function validateLocalId(id: string): void {
    if (!id || !id.match(/^[a-z0-9]{4,32}$/i)) {
        throw new InvalidLocalIdError(id);
    }
}

export async function attachLocal(user: User, id: string, password: string): Promise<void> {
    let localCredential = await makeLocalCredential(id, password);

    user.credential.localId = localCredential.localId;
    user.credential.localPw = localCredential.localPw;
    await modifyCredential(user);
}

export async function attachTemp(user: User): Promise<void> {
    let tempCredential = await makeTempCredential();
    user.credential.tempDate = tempCredential.tempDate;
    user.credential.tempSeed = tempCredential.tempSeed;
    await modifyCredential(user);
}

export async function makeLocalCredential(id: string, password: string): Promise<UserCredential> {
    validateLocalId(id);
    validatePassword(password);

    if (await UserService.getByLocalId(id)) {
        throw new DuplicateLocalIdError(id);
    }

    let passwordHash = await makePasswordHash(password);

    return {
        localId: id,
        localPw: passwordHash
    }
}

export async function makeFbCredential(fbId: string, fbToken: string): Promise<UserCredential> {
    if (await UserService.getByFb(fbId)) {
        throw new AlreadyRegisteredFbIdError(fbId);
    }
    logger.info("Trying to get fb info: fbId - " + fbId + " / fbToken - " + fbToken);
    let fbInfo = await FacebookService.getFbInfo(fbId, fbToken);
    logger.info("Got fb info: " + JSON.stringify(fbInfo));
    return {
        fbId: fbInfo.fbId,
        fbName: fbInfo.fbName
    }
}

export async function makeTempCredential(): Promise<UserCredential> {
    return {
        tempDate: new Date(),
        tempSeed: Math.floor(Math.random() * 1000)
    }
}
