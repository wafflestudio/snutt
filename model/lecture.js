var mongoose = require('mongoose');

var LectureSchema = mongoose.Schema({
  year: { type: Number, required: true },
	semester: { type: String, required: true },
	classification: { type: String, required: true },
	department: String,
	academic_year: String,
	course_number: { type: String, required: true },
	lecture_number: String,
	course_title: { type: String, required: true },
	credit: Number,
	class_time: String,
	class_time_json: [
	{ day : Number, start: Number, len: Number, place : String }
	],
	instructor: String,
	quota: Number,
	enrollment: Number,
	remark: String,
	category: String
});

module.exports = mongoose.model('Lecture', LectureSchema);
