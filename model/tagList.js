/**
 * Created by north on 16. 2. 24.
 */
var mongoose = require('mongoose');

var TagListSchema = mongoose.Schema({
  year: { type: Number, required: true },
  semester: { type: Number, required: true },
  updated_at : {type: Date, default: Date.now()},
  tags : {
    classification: {type: [String]},
    department: {type: [String]},
    academic_year: {type: [String]},
    credit: {type: [String]},
    instructor: {type: [String]}
  }
});

module.exports = mongoose.model('TagList', TagListSchema);
