export default class LectureTimeOverlapError extends Error {
    confirmMessage: string

    constructor(confirmMessage: string = "") {
        super("Lecture time overlapped");
        this.confirmMessage = confirmMessage
    }
}
