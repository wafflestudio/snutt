import UserLecture from "../model/UserLecture";

export default class NotCustomLectureError extends Error {
    constructor(public lecture: UserLecture) {
        super("Not custom lecture '" + lecture + "'");
    }
}
