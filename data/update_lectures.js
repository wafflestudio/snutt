var _ = require('lodash')
var assert = require('assert')
var async = require('async');
var Lecture = require('../model/lecture');

function timeAndPlaceToJson(timesString, locationsString) {
  if (timesString == '')
    return []

  var locations = locationsString.split('/')
  var times = timesString.split('/')
  assert.equal(locations.length, times.length, "locations does not match with times")

  var classes = times.map(function(time, idx) {
    return {
      day: ['월', '화', '수', '목', '금', '토'].indexOf(time.charAt(0)),
      start: Number(time.split('-')[0].slice(2)),
      len: Number(time.split('-')[1].slice(0, -1)),
      place: (locationsString == '/' ? '' : locations[idx])
    }
  })

  //merge if splitted (eg: 목(9-2)/목(11-2) && 220-317/220-317 => 목(9-4) && 220-317)
  for (var i = 1; i < classes.length; i++) {
    var prev = classes[i-1]
    var curr = classes[i]
    if (prev.day === curr.day && prev.place == curr.place && curr.start === (prev.start + prev.len)) {
      prev.len += curr.len
      classes.splice(i--, 1)
    }
  }
  return classes
}

function timeJsonToMask(timeJson) {
  var bitTable2D = []
  for (var i = 0; i < 6; i++)
    bitTable2D.push(_.fill(new Array(26), 0))

  timeJson.forEach(function(lecture, lectureIdx) {
    var dayIdx = lecture.day
    for (var i = lecture.start * 2; i < (lecture.start + lecture.len)*2; i++)
      bitTable2D[dayIdx][i] = 1
  })

  var timeMasks = [];
  for (var i = 0; i < 6; i++) {
    var mask = 0
    for (var j = 0; j < 25; j++) {
      if (bitTable2D[i][j] === 1)
        mask = mask + 1
      mask = mask << 1
    }
    timeMasks.push(mask)
  }
  return timeMasks
}

function insert_course(lines, year, semesterIndex, next)
{
  var cnt = 0, err_cnt = 0;
  /*For those who are not familiar with async.each
  async.each(elements,
    funcForEachElement,
    funcEverythingIsDone
    )
  */
  async.each(lines, function(line, callback) {
    var components = line.split(";");
    if (components.length == 1) {
      callback();
      return;
    }
    var lecture = new Lecture({
      year: Number(year),
      semester: semesterIndex,
      classification: components[0],
      department: components[1],
      academic_year: components[2],
      course_number: components[3],
      lecture_number: components[4],
      course_title: components[5],
      credit: Number(components[6]),
      class_time: components[7],
      class_time_json: timeAndPlaceToJson(components[7], components[8]),
      instructor: components[9],
      quota: Number(components[10]),
      enrollment: Number(components[11]),
      remark: components[12],
      category: components[13],
      created_at: Date.now(),
      updated_at: Date.now()
    })
    lecture.save(function (err, lecture) {
      cnt++
      if (err) {
        console.log("Error with " + components)
        console.log(err)
        err_cnt++
      }
      process.stdout.write("Inserting " + cnt + "th course\r");
      callback();
    })
  }, function(err) {
    console.log("INSERT COMPLETE with " + eval(cnt-err_cnt) + " success and "+ err_cnt + " errors")
    next();
  })
}

module.exports = { insert_course, timeAndPlaceToJson, timeJsonToMask }
