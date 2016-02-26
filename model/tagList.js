/**
 * Created by north on 16. 2. 24.
 */
var mongoose = require('mongoose');

var TagListSchema = mongoose.Schema({
  year: { type: Number, required: true },
  semester: { type: Number, required: true },
  updated_at : {type: Date, default: Date.now()},
  tags : {type: [String], required: true }
});

module.exports = mongoose.model('TagList', TagListSchema);
