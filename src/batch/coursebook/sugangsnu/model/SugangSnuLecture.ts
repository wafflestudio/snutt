export default interface SugangSnuLecture {
    // 교과구분
    classification: string;
    // 개설대학
    college: string;
    // 개설학과
    department: string;
    // 이수과정
    academic_course: string;
    // 학년
    academic_year: string;
    // 교과목 번호
    course_number: string;
    // 강좌 번호
    lecture_number: string;
    // 교과목명
    course_title: string;
    // 부제명
    course_subtitle: string;
    // 학점
    credit: string;
    // 강의
    num_lecture: string;
    // 실습
    num_practice: string;
    // 수업교시
    class_time: string;
    // 수업형태
    class_type: string;
    // 강의실
    location: string;
    // 주담당교수
    instructor: string;
    // 장바구니 신청
    pre_booking: string;
    // 정원
    quota: string;
    // 수강신청인원
    enrollment: string;
    // 비고
    remark: string;
    // 강의 언어
    lecture_language: string;
    // 개설 상태
    lecture_status: string;
}