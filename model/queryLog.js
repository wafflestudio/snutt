var mongoose = require('mongoose');

var QueryLogSchema = mongoose.Schema ({
  time: { type: Date, default: Date.now },
  year: { type: Number, min: 2000, max: 2999 },
  semester: { type: String },
  type: { type: String },
  body: String
});

module.exports = mongoose.model('QueryLog', QueryLogSchema);
