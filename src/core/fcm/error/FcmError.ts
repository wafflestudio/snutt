export default class FcmError extends Error {
    constructor(public statusMessage: string, public detail: string) {
        super("Fcm error occured: '" + statusMessage + "'" + " with message: '" + detail + "'");
    }
}
