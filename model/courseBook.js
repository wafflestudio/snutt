var mongoose = require('mongoose');

var CourseBookSchema = mongoose.Schema({
  year: { type: Number, required: true },
	semester: { type: Number, required: true },
  update_date : {type: Date, default: Date.now()},
  start_date : {type: Date },
  end_date : {type: Date }
});

module.exports = mongoose.model('CourseBook', CourseBookSchema);
