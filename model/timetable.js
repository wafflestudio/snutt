var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var Lecture = require('./lecture.js');

var TimetableSchema = mongoose.Schema({
	user_id : { type: Schema.Types.ObjectId, ref: 'User' },
  year : {type : Number, required : true },
  semester : {type : String, required : true },
  title : {type : String, required : true },
	lecture_list: [Lecture]
});

/*
 * Timetable.add_lecture(lecture, callback)
 * param =======================================
 * title : Optional. New title.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.copy = function(title, callback) {
  // TODO : Copy
  var copied;
  copied.save(callback);
  return copied;
};

/*
 * Timetable.add_lecture(lecture, callback)
 * param =======================================
 * lecture : a Lecture to merge.
 *            If a same lecture already exist, skip.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.add_lecture = function(lecture, callback) {
  // TODO : Check duplicate and merge!
  this.save(callback);
};

/*
 * Timetable.add_lectures(lectures, callback)
 * param =======================================
 * lectures : an array of lectures to merge.
 *            If a same lecture already exist, skip it.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.add_lectures = function(lectures, callback) {
  // TODO : Check duplicates and merge!
  this.save(callback);
};

/*
 * Timetable.update_lecture(lecture, callback)
 * param =======================================
 * lecture : a Lecture to merge.
 *            If a same lecture doesn't exist, skip.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.update_lecture = function(lecture, callback) {
  // TODO : Check and update!
  this.save(callback);
};

/*
 * Timetable.delete_lecture(lecture, callback)
 * param =======================================
 * lecture : a Lecture to delete.
 *            If a same lecture doesn't exist, skip.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.delete_lecture = function(lecture, callback) {
  // TODO : Check and update!
  this.save(callback);
};

module.exports = mongoose.model('Timetable', TimetableSchema);
