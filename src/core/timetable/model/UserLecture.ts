import Lecture from '@app/core/lecture/model/Lecture';
import LectureColor from './LectureColor';

export default interface UserLecture extends Lecture {
    created_at: Date,
    updated_at: Date,
    color?: LectureColor,
    colorIndex: number,
    course_number?: string,   // 교과목 번호
    lecture_number?: string,  // 강좌 번호
    lecture_id?: string,
}
