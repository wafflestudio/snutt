var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;

var User = require(path.join(__dirname, 'model/user'));

passport.use('local-signup', new LocalStrategy({
  usernameField : 'username',
  passwordField : 'password',
  passReqToCallback : true
},
function(req, username, password, done) {
  User.findOne({'name' : username}, function(err, user){
    if (err) return done(err);
      if (user) {
        return done(null, false, )
      }
  });
}));