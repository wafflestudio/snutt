/**
 * test/api/api_init.js
 * This script is parent script for api tests.
 * usage : $ npm test
 */
process.env.NODE_ENV = 'mocha';

require('@test/config/log');
import request = require('@test/config/supertest');

import winston = require('winston');
import assert = require('assert');
import property = require('@app/core/config/property');
import mongoose = require('mongoose');

import CourseBookService = require('@app/core/coursebook/CourseBookService');
import RefLectureService = require('@app/core/lecture/RefLectureService');

let logger = winston.loggers.get('default');
logger.info("Loaded");

describe('Integration Test', function() {
  before('valid snutt.yml', function(done) {
    if (property.get('core.secretKey') && property.get('api.host') && property.get('api.port'))
      return done();
    else
      return done(new Error("Invalid config. Please set conf.yml"));
  });

  // Change connection into test DB in order not to corrupt production DB
  before('open snutt_test', function(done) {
    mongoose.connect('mongodb://localhost/snutt_test', function(err){
      return done(err);
    });
  });

  // Clean Test DB
  // mongoose.connection.db.dropDatabase()
  // dose not actually drop the db, but actually clears it
  before('clear snutt_test db', function(done) {
    mongoose.connection.db.dropDatabase(function(err) {
      done(err);
    });
  });

  // Add 2 coursebooks, 2016-2 and 2015-W
  before('add initial coursebooks for test', function(done) {
    let promise1 = CourseBookService.add({ year: 2015, semester: 4, updated_at: new Date()});
    let promise2 = CourseBookService.add({ year: 2016, semester: 3, updated_at: new Date()});
    Promise.all([promise1, promise2]).catch(function(err) {
      done(err);
    }).then(function(result) {
      done();
    });
  });

  before('insert initial lecture for test', async function() {
    var myLecture = {
        "year": 2016,
        "semester": 3,
        "classification": "전선",
        "department": "컴퓨터공학부",
        "academic_year": "3학년",
        "course_number": "400.320",
        "lecture_number": "002",
        "course_title": "공학연구의 실습 1",
        "credit": 1,
        "class_time": "화(13-1)/목(13-1)",
        "real_class_time": "화(21:00~21:50)/목(21:00~21:50)",
        "instructor": "이제희",
        "quota": 15,
        "enrollment": 0,
        "remark": "컴퓨터공학부 및 제2전공생만 수강가능",
        "category": "",
        /*
         * See to it that the server removes _id fields correctly
         */
        "_id": "56fcd83c041742971bd20a86",
        "class_time_mask": [
          0,
          12,
          0,
          12,
          0,
          0,
          0
        ],
        "class_time_json": [
          {
            "day": 1,
            "start": 13,
            "len": 1,
            "start_time": "21:00",
            "end_time": "22:00",
            "place": "302-308",
            "_id": "56fcd83c041742971bd20a88"
          },
          {
            "day": 3,
            "start": 13,
            "len": 1,
            "start_time": "21:00",
            "end_time": "22:00",
            "place": "302-308",
            "_id": "56fcd83c041742971bd20a87"
          }
        ],
    };
    await RefLectureService.addAll([myLecture]);
  });

  // Register test user
  before('register initial test user', function(done) {
    request.post('/auth/register_local')
      .send({id:"snutt", password:"abc1234"})
      .expect(200)
      .end(function(err, res){
        assert.equal(res.body.message, 'ok');
        done(err);
      });
  });

  it('MongoDB >= 2.4', function(done) {
    var admin = mongoose.connection.db.admin();
    admin.buildInfo(function (err, info) {
      if (err)
        return done(err);
      if (parseFloat(info.version) < 2.4)
        return done(new Error("MongoDB version("+info.version+") is outdated(< 2.4). Service might not work properly"));
      done();
    });
  });

  it('Recent Coursebook', function(done) {
    request.get('/course_books/recent')
      .expect(200)
      .end(function(err, res){
        assert.equal(res.body.semester, 3);
        done(err);
      });
  });

  describe('etc', function () {
    require('./etc')(request);
  });

  describe('User', function () {
    require('./user_test')(request);
  });

  describe('Timetable', function () {
    require('./timetable_test')(request);
  });

  describe('TagList', function () {
    require('./tag_list_test')(request);
  });
});
