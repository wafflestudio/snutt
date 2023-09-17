import User from '@app/core/user/model/User';

export async function getPopups(user: User, osType?: string, osVersion?: string, appType?: string, appVersion?: string) {
  return {
    content: [
       {
         "key": "new-service-friend",
         "image_url": "https://snutt-asset.s3.ap-northeast-2.amazonaws.com/popup-images/friendPopup.png",
         "hidden_days": 1,
       },
     ],
  };
}
