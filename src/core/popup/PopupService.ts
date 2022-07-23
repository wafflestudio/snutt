import User from '@app/core/user/model/User';

export async function getPopups(user: User, osType?: string, osVersion?: string, appType?: string, appVersion?: string) {
  // just for dev tests
  return {
    content: [
      {
        "key": "snutt-ev-open",
        "image_url": "https://snutt-asset.s3.ap-northeast-2.amazonaws.com/popup-images/snutt-ev-open.png",
        "hidden_days": 7,
      },
    ],
  };
}
