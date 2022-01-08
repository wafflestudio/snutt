import Timetable from '@app/core/timetable/model/Timetable';
import TimetableService = require('@app/core/timetable/TimetableService');
import UserRepository = require('@app/core/user/UserRepository');
import CourseBookService = require('@app/core/coursebook/CourseBookService');

import User from '@app/core/user/model/User';
import UserInfo from '@app/core/user/model/UserInfo';
import UserInfoAsSnakeCase from '@app/core/user/model/UserInfoAsSnakeCase';

export function getByMongooseId(mongooseId: string): Promise<User> {
  return UserRepository.findActiveByMongooseId(mongooseId);
}

export function getByLocalId(localId: string): Promise<User> {
  return UserRepository.findActiveByLocalId(localId);
}

export function getByFb(fbId: string): Promise<User> {
  return UserRepository.findActiveByFb(fbId)
}

export function getByApple(appleEmail: string): Promise<User> {
  return UserRepository.findActiveByApple(appleEmail);
}

export function getByCredentialHash(credentialHash: string): Promise<User> {
  return UserRepository.findActiveByCredentialHash(credentialHash);
}

export function modify(user: User): Promise<void> {
  return UserRepository.update(user);
}

export function updateNotificationCheckDate(user: User): Promise<void> {
  user.notificationCheckedAt = new Date();
  return UserRepository.update(user);
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

export function getUserInfoAsSnakeCase(user: User): UserInfoAsSnakeCase {
  return {
    is_admin: user.isAdmin,
    reg_date: user.regDate,
    notification_checked_at: user.notificationCheckedAt,
    email: user.email,
    local_id: user.credential.localId,
    fb_name: user.credential.fbName
  }
}

export function deactivate(user: User): Promise<void> {
  user.active = false;
  return UserRepository.update(user);
}

export function setUserInfo(user: User, email: string): Promise<void> {
  user.email = email;
  return UserRepository.update(user);
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
