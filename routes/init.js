var express = require('express');
var router = express.Router();

var User = require('../model/user');
var Lecture = require('../model/lecture');
var Timetable = require('../model/timetable');

//DEBUG ONLY
router.get('/', function(req, res, next) {
  var me = new User({
    local : {
      id : 'snutt',
      password : 'abcd'
    }
  });
  me.save(function(err) {
    if(err) return next(err);
    console.log('User saved successfully');
    res.json({success : true});
  });
});