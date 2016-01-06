var express = require('express');
var router = express.Router();

var path = require('path');
var Lecture = require(path.join(__dirname, 'model/lecture'));
var Timetable = require(path.join(__dirname, 'model/timetable'));


router.get('/course_books', function(req, res, next) {
  CourseBook.find({},'year semester', {sort : {year : -1, semester : -1 }}, function (err, courseBooks) {
    res.send(200, courseBooks)
  });
});

router.get('/:id')


module.exports = router;