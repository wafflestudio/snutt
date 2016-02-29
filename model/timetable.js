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
      if (err) next(err);
      if (docs != []) {
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
  // TODO : 인텔리하게 이름짓기 - 현재는 같은 테이블 두번 복사하면 에러
  if (new_title == this.title) this.title += "(copy)";
  else this.title = new_title;
  this.save(callback);
};

/*
 * Timetable.add_lecture(lecture, callback)
 * param =======================================
 * lecture : a Lecture to merge.
 *            If a same lecture already exist, error.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.add_lecture = function(lecture, callback) {
  for (var i = 0; i<this.lecture_list.length; i++){
    if (lecture.is_equal(this.lecture_list[i])) {
      var err = new Error("Same lecture already exists in the timetable.");
      next(err);
      return;
    }
  }
  this.lecture_list.push(lecture);
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
  for (var i = 0; i<lectures.length; i++){
    var is_exist = false;
    for (var j = 0; j<this.lecture_list.length; j++){
      if (lectures[i].is_equal(this.lecture_list[j])) {
        is_exist = true;
        break;
      }
    }
    if (!is_exist) this.lecture_list.push(lectures[i]);
  }
  this.save(callback);
};

/*
 * Timetable.update_lecture(lecture, callback)
 * param =======================================
 * lecture : a Lecture to merge.
 *            If a same lecture doesn't exist, error.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.update_lecture = function(lecture, callback) {
  for (var i = 0; i<this.lecture_list.length; i++){
    if (lecture.is_equal(this.lecture_list[i])) {
      // TODO : 이렇게 그냥 대입해도 되나??
      this.lecture_list[i] = lecture;
      this.save(callback);
      return;
    }
  }
  var err = new Error("The lecture doesn't exist in the timetable.");
  next(err);
};

/*
 * Timetable.delete_lecture(lecture, callback)
 * param =======================================
 * lecture : a Lecture to delete.
 *            If a same lecture doesn't exist, error.
 * callback : callback for timetable.save()
 */
TimetableSchema.methods.delete_lecture = function(lecture, callback) {
  for (var i = 0; i<this.lecture_list.length; i++){
    if (lecture.is_equal(this.lecture_list[i])) {
      this.lecture_list.splice(i,1);
      this.save(callback);
      return;
    }
  }
  var err = new Error("The lecture doesn't exist in the timetable.");
  next(err);
};

module.exports = mongoose.model('Timetable', TimetableSchema);
