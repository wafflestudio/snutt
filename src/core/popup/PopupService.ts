import User from '@app/core/user/model/User';

export async function getPopups(user: User, osType?: string, osVersion?: string, appType?: string, appVersion?: string) {
  return {
    content: [
      {
        "key": "snutt-ev-open",
        "image_url": "https://snutt-asset.s3.ap-northeast-2.amazonaws.com/popup-images/snutt_event_1.png",
        "hidden_days": 1,
      },
    ],
  };
}
