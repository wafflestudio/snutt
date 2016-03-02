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

/*
 * Lecture.add_lecture(lecture)
 * 연도, 학기, 교과목 번호와 강좌 번호를 비교하여 같은 강좌인지 판단.
 * param =======================================
 * lecture : target for comparison
 */
LectureSchema.methods.is_equal = function(lecture) {
  return (this.year == lecture.year &&
      this.semester == lecture.semester &&
      this.course_number == lecture.course_number &&
      this.lecture_number == lecture.lecture_number);
};

LectureSchema.statics.is_equal = function(lecture1, lecture2) {
  return (lecture1.year == lecture2.year &&
  lecture1.semester == lecture2.semester &&
  lecture1.course_number == lecture2.course_number &&
  lecture1.lecture_number == lecture2.lecture_number);
};

LectureSchema.index({ year: 1, semester: 1, classification: 1 });
LectureSchema.index({ year: 1, semester: 1, department: 1 });
LectureSchema.index({ year: 1, semester: 1, course_title: 1 });

module.exports = mongoose.model('Lecture', LectureSchema);
