var express = require('express');
var router = express.Router();
var jwt = require('jsonwebtoken');

var CourseBook = require('../../model/courseBook');
var Lecture = require('../../model/lecture');
var secretKey = require('../../config/secretKey');

var authRouter = require('./auth');
var timetableRouter = require('./timetable');

router.get('/course_books', function(req, res, next) {
  CourseBook.find({},'year semester', {sort : {year : -1, semester : -1 }}, function (err, courseBooks) {
    res.send(200, courseBooks)
  });
});

router.get('/search_query', function(req, res, next) {
  Lecture.find({
    year : req.params.year,
    semester : req.params.semester
  }).where('classification').in(req.params.classification)
  .$where(function(){
      
  }).exec(function(err, lectures) {
      
  });
});

router.get('/app_version', function(req, res, next) {
   //FIXME : check for app_version and return the version 
   res.send({version : 0.1});
});

router.use('/auth', authRouter);

router.use(function(req, res, next) {
  if(req.user) return next();
  var token = req.body.token || req.query.token || req.headers['x-access-token'];
  
  if (token) {

    // verifies secret and checks exp
    jwt.verify(token, secretKey.jwtSecret, function(err, decoded) {      
      if (err) {
        return res.json({ success: false, message: 'Failed to authenticate token.' });    
      } else {
        // if everything is good, save to request for use in other routes
        req.user = decoded;    
        console.log(decoded);
        next();
      }
    });

  } else {

    // if there is no token
    // return an error
    return res.send({ 
        success: false, 
        message: 'No token provided.' 
    });
    
  }
});

router.use('/tables', timetableRouter);

module.exports = router;