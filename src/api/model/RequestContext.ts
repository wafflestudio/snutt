import User from '@app/core/user/model/User';

export default interface RequestContext {
    method?: string,
    url?: string,
    user?: User,
    platform?: string,
    osType?: string,
    osVersion?: string,
    appType?: string,
    appVersion?: string,
}
