import crypto = require('crypto');

import Timetable from '@app/core/timetable/model/Timetable';

import User from '@app/core/user/model/User';
import UserInfo from '@app/core/user/model/UserInfo';
import SnuttevUserInfo from '@app/core/user/model/SnuttevUserInfo';
import * as RedisUtil from '@app/core/redis/RedisUtil';
import {sendMail} from '@app/core/mail/MailUtil';
import RedisVerificationValue from "@app/core/user/model/RedisVerificationValue";
import ApiError from "@app/api/error/ApiError";
import ErrorCode from "@app/api/enum/ErrorCode";
import TimetableService = require('@app/core/timetable/TimetableService');
import UserRepository = require('@app/core/user/UserRepository');
import CourseBookService = require('@app/core/coursebook/CourseBookService');

export function getVerifiedByEmail(email: string): Promise<User> {
  return UserRepository.findActiveByVerifiedEmail(email);
}

export function getByEmail(email: string): Promise<User> {
  return UserRepository.findActiveByEmail(email)
}


export function getByMongooseId(mongooseId: string): Promise<User> {
  return UserRepository.findActiveByMongooseId(mongooseId);
}

export function getByLocalId(localId: string): Promise<User> {
  return UserRepository.findActiveByLocalId(localId);
}

export function getByFb(fbId: string): Promise<User> {
  return UserRepository.findActiveByFb(fbId);
}

export function getByAppleSub(appleSub: string): Promise<User> {
  return UserRepository.findActiveByAppleSub(appleSub);
}

export function getByAppleTransferSub(appleTransferSub: string): Promise<User> {
  return UserRepository.findActiveByAppleTransferSub(appleTransferSub);
}

export function getByCredentialHash(credentialHash: string): Promise<User> {
  return UserRepository.findActiveByCredentialHash(credentialHash);
}

export function modify(user: User): Promise<void> {
  return UserRepository.update(user);
}

export function updateNotificationCheckDate(user: User): Promise<void> {
  return UserRepository.update({...user, notificationCheckedAt: new Date()});
}

export function getUserInfo(user: User): UserInfo {
  return {
    isAdmin: user.isAdmin,
    regDate: user.regDate,
    notificationCheckedAt: user.notificationCheckedAt,
    email: user.email,
    local_id: user.credential.localId,
    fb_name: user.credential.fbName
  }
}

export function getSnuttevUserInfo(user: User, userId: string): SnuttevUserInfo {
  return user ? {
    id: userId,
    email: user.email,
    local_id: user.credential.localId,
  } : null
}

export function isUserEmailVerified(user: User): boolean {
  return user.isEmailVerified ? user.isEmailVerified : false;
}

export async function resetEmailVerification(user: User): Promise<void> {
  await modify({...user, isEmailVerified: false});
}

export async function sendVerificationCode(user: User, email: string): Promise<void> {
  if (isUserEmailVerified(user)) {
    throw new ApiError(409, ErrorCode.USER_EMAIL_ALREADY_VERIFIED, "이미 메일인증이 완료된 유저입니다.")
  }
  if (await isEmailAlreadyVerifiedByAnotherUser(email)) {
    throw new ApiError(409, ErrorCode.EMAIL_ALREADY_VERIFIED_BY_ANOTHER_USER, "해당 메일로 인증된 다른 계정이 있습니다.")
  }
  const key = `verification-code-${user._id}`
  const existing: RedisVerificationValue = JSON.parse(await RedisUtil.get(key))
  const code = String(Math.floor(Math.random() * 1000000)).padStart(6, "0")
  if (existing && existing.count && existing.count > 4) {
    throw new ApiError(429, ErrorCode.TOO_MANY_VERIFICATION_REQUEST, "너무 요청이 많습니다. 나중에 다시 시도해주세요")
  }
  const value: RedisVerificationValue = {
    email: email,
    code: code,
    count: existing && existing.count ? existing.count + 1 : 1,
    createdAtTimestamp: Date.now()
  }
  const emailBody =
    `<h2>인증번호 안내</h2><br/>` +
    `안녕하세요. SNUTT입니다. <br/> ` +
    `<b>아래의 인증번호 6자리를 진행 중인 화면에 입력하여 3분내에 인증을 완료해주세요.</b><br/><br/>` +
    `<h3>인증번호</h3><h3>${code}</h3><br/><br/>` +
    `인증번호는 이메일 발송 시점으로부터 3분 동안 유효합니다.`;
  sendMail(email, `[SNUTT] 인증번호 [${code}] 를 입력해주세요`, emailBody);
  await RedisUtil.setex(key, 180, JSON.stringify(value))
}

export async function verifyEmail(user: User, codeSubmitted: string): Promise<boolean> {
  const key = `verification-code-${user._id}`
  const verificationValue: RedisVerificationValue = JSON.parse(await RedisUtil.get(key))
  if (verifyCode(verificationValue, codeSubmitted)) {
    await modify({...user, email: verificationValue.email, isEmailVerified: true});
    return true
  }
  throw new ApiError(400, ErrorCode.INVALID_VERIFICATION_CODE, "유효하지 않은 인증코드입니다.")
}

function verifyCode(verificationValue: RedisVerificationValue, codeSubmitted: string): boolean {
  try {
    return verificationValue.code == codeSubmitted
  } catch (e) {
    return false
  }
}

export function deactivate(user: User): Promise<void> {
  return UserRepository.update({...user, active: false});
}

export function updateLastLoginTimestamp(user: User): void {
  return UserRepository.updateLastLoginTimestamp(user);
}

async function createDefaultTimetable(user: User): Promise<Timetable> {
  let userId = user._id;
  let coursebook = await CourseBookService.getRecent();
  return await TimetableService.addFromParam({
    user_id: userId,
    year: coursebook.year,
    semester: coursebook.semester,
    title: "나의 시간표"
  });
}

export async function add(user: User): Promise<User> {
  if (user.isAdmin === undefined) user.isAdmin = false;
  if (!user.regDate) user.regDate = new Date();
  if (!user.lastLoginTimestamp) user.lastLoginTimestamp = Date.now();
  if (!user.notificationCheckedAt) user.notificationCheckedAt = new Date();
  let inserted = await UserRepository.insert(user);
  await createDefaultTimetable(inserted);
  return inserted;
}

async function isEmailAlreadyVerifiedByAnotherUser(email: string): Promise<boolean> {
  return (await getVerifiedByEmail(email)) !== null
}

export async function sendResetPasswordCode(user: User): Promise<void> {

  const key = `reset-password-code-${user._id}`
  const existing: RedisVerificationValue = JSON.parse(await RedisUtil.get(key))
  const code = crypto.randomBytes(6).toString('base64')

  if (existing && existing.count && existing.count > 4) {
    throw new ApiError(429, ErrorCode.TOO_MANY_VERIFICATION_REQUEST, "너무 요청이 많습니다. 나중에 다시 시도해주세요")
  }

  const value: RedisVerificationValue = {
    email: user.email,
    code: code,
    count: existing && existing.count ? existing.count + 1 : 1, 
    createdAtTimestamp: Date.now()
  }

  const emailBody =
    `<h2>비밀번호 재설정 안내</h2><br/>` +
    `안녕하세요. SNUTT입니다. <br/> ` +
    `<b>아래의 인증코드를 진행 중인 화면에 입력하여 비밀번호 재설정을 완료해주세요.</b><br/><br/>` +
    `<h3>인증코드</h3><h3>${code}</h3><br/><br/>` +
    `인증코드는 이메일 발송 시점으로부터 3분 동안 유효합니다.`;

  sendMail(user.email, `[SNUTT] 인증코드 [${code}] 를 입력해주세요`, emailBody);

  await RedisUtil.setex(key, 60 * 60, JSON.stringify(value))
}

export async function verifyResetPasswordCode(user: User, codeSubmitted: string): Promise<boolean> {
  
  const key = `reset-password-code-${user._id}`
  const verificationValue: RedisVerificationValue = JSON.parse(await RedisUtil.get(key))

  if (!verificationValue) {
    throw new ApiError(409, ErrorCode.RESET_PASSWORD_NO_REQUEST, "비밀번호 재설정을 다시 시도해주세요.")
  }

  if (Date.now() - verificationValue.createdAtTimestamp > 60 * 3 * 1000) {
    throw new ApiError(401, ErrorCode.RESET_PASSWORD_CODE_EXPIRED, "만료된 인증코드입니다.")
  }

  if (!verifyCode(verificationValue, codeSubmitted)) {
    throw new ApiError(401, ErrorCode.RESET_PASSWORD_WRONG_CODE, "잘못된 인증코드입니다.")
  } 

  return true
}

export function checkValidEmail(email: string): boolean {
  const regExp = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
  return email.match(regExp) != null
}

export async function sendLocalId(user: User): Promise<void> {

  const emailBody =
    `<h2>아이디 찾기 안내</h2><br/>` +
    `안녕하세요. SNUTT입니다. <br/> ` +
    `<b>${user.email}로 가입된 아이디는 다음과 같습니다.</b><br/><br/>` +
    `<h3>아이디</h3><h3>${user.credential.localId}</h3><br/><br/>`;

  sendMail(user.email, `[SNUTT] 아이디를 찾았습니다`, emailBody);
}
