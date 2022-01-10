import fs = require('fs');
import assert = require('assert');
import ExcelUtil = require('@app/batch/coursebook/excel/ExcelUtil');
import SugangSnuLectureService = require('@app/batch/coursebook/sugangsnu/SugangSnuLectureService');

describe("SugangSnuLectureServiceIntegrationTest", function() {
    it("getRefLectureListFromExcelSheet__integration__success", async function() {
        let file = fs.readFileSync("test/batch/coursebook/sugangsnu/SugangSnuTestExcel.xlsx");
        let sheet = ExcelUtil.getFirstSheetFromBuffer(file);
        let lectureList = SugangSnuLectureService.getRefLectureListFromExcelSheet(sheet, 2018, 2, 54);
        assert.equal(lectureList.length, 9);

        assert.deepEqual(lectureList[0], {
            year: 2018,
            semester: 2,
            classification: '교양',
            department: '기초교육원',
            academic_year: '1학년',
            course_number: '053.003',
            lecture_number: '001',
            course_title: '사회봉사 1',
            credit: 1,
            class_time: '',
            class_time_json: [],
            class_time_mask: [ 0, 0, 0, 0, 0, 0, 0 ],
            instructor: '김의영',
            quota: 20,
            remark: '아동/청소년, 장애인, 노인, 시민단체 등 연계기관에서 활동',
            category: '대학과 리더쉽' });
       
        assert.deepEqual(lectureList[8], {
            year: 2018,
            semester: 2,
            classification: '교양',
            department: '기초교육원',
            academic_year: '1학년',
            course_number: '053.011',
            lecture_number: '001',
            course_title: '그린리더십 인턴십',
            credit: 3,
            class_time: '금(6-3)',
            class_time_json: [ { day: 4, start: 6, len: 3, place: '220-202' } ],
            class_time_mask: [ 0, 0, 0, 0, 258048, 0, 0 ],
            instructor: '류재명',
            quota: 40,
            remark: '1. 그린리더십 교과목 2과목 선이수 학생대상-2018학년도 1학기 수강중인 과목도 선이수 과목에 포함됨/2. 지원서와 자기 소개서 제출 http://aiees.snu.ac.kr/greenleadership/home /문의: 880-2665',
            category: '대학과 리더쉽' });
    })
})
