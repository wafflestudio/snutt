import User from '@app/core/user/model/User';

export async function getPopups(user: User, osType?: string, osVersion?: string, appType?: string, appVersion?: string) {
  // just for dev tests
  return {
    content: [
      {
        "key": `test-popup-key-null (osType:${osType}/osVersion:${osVersion}/appType:${appType}/appVersion:${appVersion})`,
        "image_url": "https://avatars.githubusercontent.com/u/35535636",
        "hidden_days": null,
      },
      {
        "key": `test-popup-key-1day (osType:${osType}/osVersion:${osVersion}/appType:${appType}/appVersion:${appVersion})`,
        "image_url": "https://avatars.githubusercontent.com/u/48513130",
        "hidden_days": 1,
      },
    ],
  };
}
