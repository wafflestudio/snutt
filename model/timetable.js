var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var lecture = require('./lecture.js');

var TimetableSchema = mongoose.Schema({
	user_id : { type: Schema.Types.ObjectId, ref: 'User' },
  year : {type : Number, required : true },
  semester : {type : String, required : true },
  title : {type : String, required : true },
	lecture_list: [lecture.schema]
});

module.exports = mongoose.model('Timetable', TimetableSchema);
