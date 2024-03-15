import User from '@app/core/user/model/User';

export async function getPopups(user: User, osType?: string, osVersion?: string, appType?: string, appVersion?: string) {
  return {
    content: [],
  };
}
