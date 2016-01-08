var express = require('express');
var router = express.Router();
var jwt = require('jsonwebtoken');
var secretKey = require('../../config/secretKey.sample');

var User = require('../../model/user');

router.post('/login_local', function(req, res, next) {
  User.findOne({'local.id' : req.body.id },
    function(err, user) {
      if(err) return next(err);
      if(!user) {
        res.json({success : false, message : 'Authentication failed. User not found.'});
      } else if (user) {
        user.verifyPassword(req.body.password, function(err, isMatch) {
          if(!isMatch) {
            res.json({success : false, message : 'Authentication failed. Wrong password.'})
          } else {
            var token = jwt.sign(user, secretKey.jwtSecret, {
              expiresIn : '180d' //FIXME : expire time
            });

            res.json({
              success : true,
              message : 'Authentication success.',
              token : token
            });
          }
        })
      }
    }
  );
});




module.exports = router;
