import TimePlace from '@app/core/timetable/model/TimePlace';

export default interface Lecture {
    _id?: string,
    classification: string,                           // 교과 구분
    department: string,                               // 학부
    academic_year: string,                            // 학년
    course_title: string,   // 과목명
    credit: number,                                   // 학점
    class_time: string,
    real_class_time: string,
    class_time_json: TimePlace[],
    class_time_mask: number[],
    instructor: string,                               // 강사
    quota: number,                                    // 정원
    freshmanQuota?: number,                            // 신입생정원
    remark: string,                                   // 비고
    category: string,
}
