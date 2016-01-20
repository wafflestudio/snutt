var mongoose = require('mongoose');
var bcrypt = require('bcrypt');

var UserSchema = new mongoose.Schema({
  local            : {
    id          : {type : String, unique : true},
    password    : String
  },
  facebook         : {
    id           : String,
    token        : String
  },
	isAdmin: {type: Boolean, default: false},
	regDate: {type: Date, default: Date.now()}
});

UserSchema.pre('save', function (callback) {
	var user = this;

	if (user.id.replace(/[a-z0-9]{3,32}/i, '') !== '')
		return callback(new Error("ID rule violated"));

	if (!user.isModified('local.password')) return callback();

	bcrypt.hash(user.local.password, 4, function (err, hash) {
		if (err) return callback(err);

		user.local.password = hash;
		callback();
	});
});

UserSchema.methods.verify_password = function(password, cb) {
	bcrypt.compare(password, this.local.password, function(err, isMatch) {
		if (err) return cb(err);
		cb(null, isMatch);
	});
};

module.exports = mongoose.model('User', UserSchema);