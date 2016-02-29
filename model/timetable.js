var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var Lecture = require('./lecture');
var Util = require('../util/util');

var TimetableSchema = mongoose.Schema({
	user_id : { type: Schema.Types.ObjectId, ref: 'User' },
  year : {type : Number, required : true },
  semester : {type : Number, required : true },
  title : {type : String, required : true },
	lecture_list: [Lecture]
});

/*
 * No timetable with same title in the same semester
 */
TimetableSchema.pre('save', function(next) {
  this.model('Timetable').find(
    {
      user_id : this.user_id,
      year : this.year,
      semester: this.semester,
      title: this.title
    }, function (err, docs) {
      if (err || docs != []) {
        var new_err = new Error('A timetable with the same title already exists');
        next(new_err);
      } else {
        next();
      }
    });
});

/*
 * Timetable.add_lecture(lecture, callback)
 * param =======================================
 * new_title : New title.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.copy = function(new_title, callback) {
  Util.object_new_id(this);
  if (new_title == this.title) this.title += "(copy)";
  else this.title = new_title;
  this.save(callback);
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
