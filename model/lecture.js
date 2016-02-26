var mongoose = require('../db');

var LectureSchema = mongoose.Schema({
  year: { type: Number, required: true },           // 연도
  semester: { type: Number, required: true },       // 학기
  classification: { type: String, required: true }, // 교과 구분
  department: String,                               // 학부
  academic_year: String,                            // 학년
  course_number: { type: String, required: true },  // 교과목 번호
  lecture_number: String,                           // 강좌 번호
  course_title: { type: String, required: true },   // 과목명
  credit: Number,                                   // 학점
  class_time: String,
  class_time_json: [
  { day : Number, start: Number, len: Number, place : String }
  ],
  class_time_mask: { type: [ Number ], required: true },
  instructor: String,                               // 강사
  quota: Number,                                    // 정원
  enrollment: Number,                               // 신청인원
  remark: String,                                   // 비고
  category: String,
  created_at: Date,
  updated_at: Date
});

LectureSchema.index({ year: 1, semester: 1, classification: 1 });
LectureSchema.index({ year: 1, semester: 1, department: 1 });
LectureSchema.index({ year: 1, semester: 1, course_title: 1 });

module.exports = mongoose.model('Lecture', LectureSchema);
