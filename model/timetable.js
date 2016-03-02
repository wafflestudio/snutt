var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var Lecture = require('./lecture');
var Util = require('../util/util');

var TimetableSchema = mongoose.Schema({
	user_id : { type: Schema.Types.ObjectId, ref: 'User' },
  year : {type : Number, required : true },
  semester : {type : Number, required : true },
  title : {type : String, required : true },
	lecture_list: [Lecture.schema]
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
      if (err) next(err);
      if (docs != [] && docs._id != this._id) {
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
TimetableSchema.methods.copy = function(new_title, next) {
  Util.object_new_id(this);
  // TODO : 인텔리하게 이름짓기 - 현재는 같은 테이블 두번 복사하면 에러
  if (new_title == this.title) this.title += "(copy)";
  else this.title = new_title;
  this.save(next);
};

/*
 * Timetable.add_lecture(lecture, callback)
 * param =======================================
 * lecture : a Lecture to merge.
 *            If a same lecture already exist, error.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.add_lecture = function(lecture, next) {
  for (var i = 0; i<this.lecture_list.length; i++){
    if (lecture.is_equal(this.lecture_list[i])) {
      var err = new Error("Same lecture already exists in the timetable.");
      next(err);
      return;
    }
  }
  this.lecture_list.push(lecture);
  this.save(next);
};

/*
 * Timetable.add_lectures(lectures, callback)
 * param =======================================
 * lectures : an array of lectures to merge.
 *            If a same lecture already exist, skip it.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.add_lectures = function(lectures, next) {
  for (var i = 0; i<lectures.length; i++){
    var is_exist = false;
    for (var j = 0; j<this.lecture_list.length; j++){
      if (lectures[i].is_equal(this.lecture_list[j])) {
        is_exist = true;
        break;
      }
    }
    if (!is_exist) this.lecture_list.push(lectures);
  }
  this.save(next);
};

/*
 * Timetable.update_lecture(lecture_raw, callback)
 * param =======================================
 * lecture : a partial update for lecture.
 *            If a same lecture doesn't exist, error.
 * callback : callback for timetable.update(err, numAffected)
 */
TimetableSchema.methods.update_lecture = function(lecture_raw, next) {
  for (var i = 0; i<this.lecture_list.length; i++){
    if (Lecture.is_equal(this.lecture_list[i], lecture_raw)) {
      var update_set = {};
      for (var field in lecture_raw) {
        update_set['lecture_list.'+i+'.' + field] = lecture_raw[field];
      }
      this.update({$set: update_set}, next);
      return;
    }
  }
  var err = new Error("The lecture doesn't exist in the timetable.");
  next(err);
};

/*
 * Timetable.delete_lecture(lecture_id, callback)
 * param =======================================
 * lecture : a Lecture._id to delete.
 *            If a lecture doesn't exist, skip.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.delete_lecture = function(lecture_id, next) {
  this.lecture_list.pull({_id : lecture_id});
  this.save(next);
};

module.exports = mongoose.model('Timetable', TimetableSchema);
