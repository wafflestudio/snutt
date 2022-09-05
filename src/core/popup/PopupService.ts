import User from '@app/core/user/model/User';

export async function getPopups(user: User, osType?: string, osVersion?: string, appType?: string, appVersion?: string) {
  return {
    content: [
      {
        "key": "snutt-ev-open",
        "image_url": "https://snutt-asset.s3.ap-northeast-2.amazonaws.com/popup-images/C30C86B0-5905-4A57-B1A4-218C6D1169B6.jpeg",
        "hidden_days": 1,
      },
    ],
  };
}
