export default class FcmError extends Error {
    constructor(public statusMessage: string, public detail: any) {
        super("Fcm error occured: '" + statusMessage + "'" + " with message: '" + JSON.stringify(detail) + "'");
    }
}
