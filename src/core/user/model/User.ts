import UserCredential from '@app/core/user/model/UserCredential';
import TempPasswordResetCode from './TempPasswordResetCode';

export default interface User {
  _id?: string;
  credential: UserCredential;
  credentialHash: string;
  isAdmin?: boolean;
  regDate?: Date;
  notificationCheckedAt?: Date;
  email?: string;
  isEmailVerified?: boolean;
  fcmKey?: string;
  active?: boolean;
  lastLoginTimestamp?: number;
  tempPasswordResetCode?: TempPasswordResetCode;
}
