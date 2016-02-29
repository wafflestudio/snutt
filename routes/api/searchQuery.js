var router = require('express').Router();
var Lecture = require('../../model/lecture')
var timeJsonToMask = require('../../data/update_lectures').timeJsonToMask

//something similar to LIKE query in SQL
function like(str, option) {
  if (option === undefined)
    var option = { fromFirstChar: false }
  //replace every character(eg. 'c') to '.+c', except for first character
  var reg = str.replace(/(?!^)(.)/g, '.*$1');
  if (option.fromFirstChar)
    reg = '^' + reg;
  return reg
}

function timeRangesToBinaryConditions(timeJson) {
  return timeJsonToMask(timeJson).map(function(bit, idx) {
    var condition = {}
    if (bit != 0)
      condition['$bitsAnySet'] = bit
    condition['$bitsAllClear'] = (~(bit << 6))>>>6
    return condition
  })
}

module.exports = router.post('/', function(req, res, next) {
  var query = {}
  query.year = req.body.year
  query.semester = req.body.semester
  if (req.body.title && req.body.title != [])
    query.course_title = { $regex: like(req.body.title), $options: 'i' }
  if (req.body.credit && req.body.credit != [])
    query.credit = { $in: req.body.credit }
  if (req.body.professor && req.body.professor != [])
    query.instructor = like(req.body.professor)
  if (req.body.department && req.body.department != []) { // in this case result should be sorted by departments
    var orRegex = '(' +
      req.body.department.map(function(dep, idx) {
        return like(dep)
      }).join('|')
      + ')'
    query.department = { $regex: orRegex, $options: 'i'}
  }
  if (req.body.time && req.body.time != []) {
    var conditions = timeRangesToBinaryConditions(req.body.time)
    conditions.forEach(function(condition, idx) {
      query['class_time_mask.' + idx] = condition
    })
  }

  Lecture.find(query).sort('course_number').lean().exec(function (err, lectures) {
    if (err) next(err);
    return res.json(lectures);
  })
})
