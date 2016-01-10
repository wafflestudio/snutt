var mongoose = require('mongoose');

// connect mongoose
module.exports = mongoose.connect('mongodb://localhost/snutt', function(err) {
  if(err) {
    console.log(err);
    throw err;
  }
  console.log('mongodb connected');
});
