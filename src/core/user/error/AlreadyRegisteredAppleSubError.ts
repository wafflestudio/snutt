export default class AlreadyRegisteredAppleSubError extends Error {
    constructor(public appleSub: string) {
        super("Already registered apple sub '" + appleSub + "'");
    }
}
