import mongoose = require('mongoose');
import winston = require('winston');

import User from '@app/core/user/model/User';

var logger = winston.loggers.get('default');

let UserSchema = new mongoose.Schema({
  credential : {
    localId: {type: String, default: null},
    localPw: {type: String, default: null},
    fbName: {type: String, default: null},
    fbId: {type: String, default: null},
    appleEmail: {type: String, default: null},
    appleSub: {type: String, default: null},
    appleTransferSub: {type: String, default: null},

    // 위 항목이 없어도 unique credentialHash을 생성할 수 있도록
    tempDate: {type: Date, default: null},          // 임시 가입 날짜
    tempSeed: {type: Number, default: null}         // 랜덤 seed

  },
  credentialHash : {type: String, default: null},   // credential이 변경될 때 마다 SHA 해싱 (model/user.ts 참조)
  isAdmin: {type: Boolean, default: false},         // admin 항목 접근 권한
  regDate: Date,                                    // 회원가입 날짜
  lastLoginTimestamp: Number,                       // routes/api/api.ts의 토큰 인증에서 업데이트
  notificationCheckedAt: Date,                      // 새로운 알림이 있는지 확인하는 용도
  email: String,
  isEmailVerified: Boolean,
  fcmKey: String,                                   // Firebase Message Key

  // if the user remove its account, active status becomes false
  // Should not remove user object, because we must preserve the user data and its related objects
  active: {type: Boolean, default: true}
});

UserSchema.index({ credentialHash : 1 })            // 토큰 인증 시
UserSchema.index({ "credential.localId": 1 })       // ID로 로그인 시
UserSchema.index({ "credential.fbId": 1 })          // 페이스북으로 로그인 시

let MongooseUserModel = mongoose.model('User', UserSchema ,'users');

function fromMongoose(mongooseDocument: mongoose.MongooseDocument): User {
  if (mongooseDocument === null) {
    return null;
  }

  let wrapper = <any>mongooseDocument;
  return {
    _id: wrapper._id,
    credential: wrapper.credential,
    credentialHash: wrapper.credentialHash,
    isAdmin: wrapper.isAdmin,
    regDate: wrapper.regDate,
    notificationCheckedAt: wrapper.notificationCheckedAt,
    email: wrapper.email,
    isEmailVerified: wrapper.isEmailVerified,
    fcmKey: wrapper.fcmKey,
    active: wrapper.active,
    lastLoginTimestamp: wrapper.lastLoginTimestamp,
  }
}
export async function findActiveByVerifiedEmail(email: string) : Promise<User> {
  const mongooseDocument = await MongooseUserModel.findOne({'email' : email, 'active' : true , 'isEmailVerified': true}).exec();
  return fromMongoose(mongooseDocument);
}

export async function findActiveByEmail(email: string) : Promise<User> {
const mongooseDocument = await MongooseUserModel.findOne({'email' : email, 'active' : true }).exec();
  return fromMongoose(mongooseDocument);
}

export async function findActiveByFb(fbId: string) : Promise<User> {
  const mongooseDocument = await MongooseUserModel.findOne({'credential.fbId' : fbId, 'active' : true }).exec();
  return fromMongoose(mongooseDocument);
}

export async function findActiveByAppleSub(appleSub: string) : Promise<User> {
  const mongooseDocument = await MongooseUserModel.findOne({'credential.appleSub' : appleSub, 'active' : true}).exec();
  return fromMongoose(mongooseDocument);
}

export async function findActiveByAppleTransferSub(appleTransferSub: string) : Promise<User> {
  const mongooseDocument = await MongooseUserModel.findOne({'credential.appleTransferSub' : appleTransferSub, 'active' : true}).exec();
  return fromMongoose(mongooseDocument);
}

export async function findActiveByCredentialHash(hash: string): Promise<User> {
  const mongooseDocument = await MongooseUserModel.findOne({'credentialHash' : hash, 'active' : true }).exec();
  return fromMongoose(mongooseDocument);
}

export function findActiveByMongooseId(mid: string): Promise<User> {
  return MongooseUserModel.findOne({ '_id': mid, 'active': true })
    .exec().then(function (userDocument) {
      return fromMongoose(userDocument);
    });
}

export function findActiveByLocalId(id: string): Promise<User> {
  return MongooseUserModel.findOne({ 'credential.localId': id, 'active': true })
    .exec().then(function (userDocument) {
      return fromMongoose(userDocument);
    });
}

export function update(user: User): Promise<void> {
  return MongooseUserModel.findOne({ '_id': user._id })
    .exec().then(function (userDocument: any) {
      userDocument.credential = user.credential;
      userDocument.credentialHash = user.credentialHash;
      userDocument.isAdmin = user.isAdmin;
      userDocument.regDate = user.regDate;
      userDocument.notificationCheckedAt = user.notificationCheckedAt;
      userDocument.email = user.email;
      userDocument.fcmKey = user.fcmKey;
      userDocument.active = user.active;
      userDocument.lastLoginTimestamp = user.lastLoginTimestamp;
      userDocument.isEmailVerified = user.isEmailVerified;
      userDocument.save();
    })
}

export async function insert(user: User): Promise<User> {
  let mongooseUserModel = new MongooseUserModel(user);
  await mongooseUserModel.save();
  return fromMongoose(mongooseUserModel);
}

export function updateLastLoginTimestamp(user: User): void {
  let timestamp = Date.now();
  // Mongoose를 사용하면 성능이 저하되므로, raw mongodb를 사용한다.
  mongoose.connection.db.collection('users').updateOne({ _id: user._id }, { $set: { lastLoginTimestamp: timestamp } })
    .catch(function (err) {
      logger.error("Failed to update timestamp");
      logger.error(err);
    });
  user.lastLoginTimestamp = timestamp;
}
