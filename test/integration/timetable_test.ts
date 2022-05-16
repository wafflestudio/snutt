/**
 * test/api/timetable_test.js
 * These tests are for routes/api/timetable.js
 * supertest: https://github.com/visionmedia/supertest
 * mocha: http://mochajs.org/#usage
 */
"use strict";

import assert = require('assert');
import supertest = require('supertest');
import ErrorCode from '@app/api/enum/ErrorCode';

export = function(request: supertest.SuperTest<supertest.Test>) {
  var token;
  var table_id;
  var table2_id;
  var table_ws_id;
  var table_updated_at;
  var lecture_id;
  var ref_lecture;
  var lecture;
  var old_title;

  before(function(done) {
    request.post('/auth/login_local')
      .send({id:"snutt", password:"abc1234"})
      .expect(200)
      .end(function(err, res){
        token = res.body.token;
        done(err);
      });
  });

  before(function(done) {
    request.post('/tables/')
      .set('x-access-token', token)
      .send({year:2016, semester:3, title:"MyTimeTable"})
      .expect(200)
      .end(function(err, res){
        if (!res.body.length && !err) err = new Error("Timetable List Incorrect");
        else if (!err) table_id = res.body[1]._id;
        assert.equal(res.body[1].title, "MyTimeTable");
        done(err);
      });
  });

  it ('Get timetable list succeeds', function(done){
    request.get('/tables/')
      .set('x-access-token', token)
      .expect(200)
      .end(function(err, res) {
        if (err) done(err);
        assert.equal(res.body[1].title, "MyTimeTable");
        done();
      });
  });

  it ('Get timetable succeeds', function(done){
    request.get('/tables/'+table_id)
      .set('x-access-token', token)
      .expect(200)
      .end(function(err, res) {
        if (err) return done(err);
        assert.equal(res.body.title, "MyTimeTable");
        table_updated_at = res.body.updated_at;
        done(err);
      });
  });

  it ('Create timetable succeeds', function(done){
    request.post('/tables/')
      .set('x-access-token', token)
      .send({year:2016, semester:3, title:"MyTimeTable2"})
      .expect(200)
      .end(function(err, res) {
        if (err) return done(err);
        assert.equal(res.body[2].title, "MyTimeTable2");
        table2_id = res.body[2]._id;
        done(err);
      });
  });

  it ('Get timetable by semester succeeds', function(done){
    request.get('/tables/2016/3')
      .set('x-access-token', token)
      .expect(200)
      .end(function(err, res) {
        if (err) return done(err);
        assert.deepEqual(res.body.map(function(val) {return val.title;}), ["나의 시간표", "MyTimeTable", "MyTimeTable2"]);
        done(err);
      });
  });

  it ('New timetable is the most recent table', function(done) {
    request.get('/tables/recent')
      .set('x-access-token', token)
      .expect(200)
      .end(function(err, res) {
        if (err) return done(err);
        assert.equal(res.body.title, "MyTimeTable2");
        done(err);
      });
  });

  it ('Create timetable with the same title should fail', function(done){
    request.post('/tables/')
      .set('x-access-token', token)
      .send({year:2016, semester:3, title:"MyTimeTable"})
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.DUPLICATE_TIMETABLE_TITLE);
        done(err);
      });
  });

  it ('Create timetable with the same title but different semester will succeed', function(done){
    request.post('/tables/')
      .set('x-access-token', token)
      .send({year:2016, semester:1, title:"MyTimeTable"})
      .expect(200)
      .end(function(err, res) {
        if (err) return done(err);
        assert.equal(res.body[3].semester, 1);
        table_ws_id = res.body[3]._id;
        done(err);
      });
  });

  it ('Update timetable title succeeds', function(done){
    request.put('/tables/'+table_id)
      .set('x-access-token', token)
      .send({title:"MyTimeTable3"})
      .expect(200)
      .end(function(err, res) {
        if(err) done(err);
        request.get('/tables/'+table_id)
          .set('x-access-token', token)
          .expect(200)
          .end(function(err, res) {
            assert.equal(res.body.title, "MyTimeTable3");
            done(err);
          });
      });
  });

  it ('Updated timetable is the most recent table', function(done) {
    request.get('/tables/recent')
      .set('x-access-token', token)
      .expect(200)
      .end(function(err, res) {
        if (err) return done(err);
        assert.equal(res.body.title, "MyTimeTable3");
        done(err);
      });
  });

  it ('Updating timetable with the same title should fail', function(done){
    request.put('/tables/'+table_id)
      .set('x-access-token', token)
      .send({title:"MyTimeTable2"})
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.DUPLICATE_TIMETABLE_TITLE);
        done(err);
      });
  });

  it ('Search Lecture', function(done) {
    request.post('/search_query/')
      .set('x-access-token', token)
      .send({title:"공실1", year:2016, semester:3})
      .expect(200)
      .end(function(err, res) {
        if (err) done(err);
        assert.equal(res.body.length, 1);
        ref_lecture = res.body[0];
        done();
      })
  });

  it ('Create a lecture from ref', function(done) {
    request.post('/tables/'+table_id+'/lecture/'+ref_lecture._id)
      .set('x-access-token', token)
      .expect(200)
      .end(function(err, res) {
        if (err) {
          console.log(res.body);
          done(err);
        }
        lecture = res.body.lecture_list[0];
        lecture_id = lecture._id;
        old_title = lecture.course_title;
        assert.equal(lecture.course_number, "400.320");
        assert.equal(lecture.class_time_json[0].place, "302-308");
        done();
      });
  });

  it ('Create a lecture from ref with wrong semester will fail', function(done) {
    request.post('/tables/'+table_ws_id+'/lecture/'+ref_lecture._id)
      .set('x-access-token', token)
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.WRONG_SEMESTER);
        if (err) {
          console.log(res.body);
          done(err);
        }
        done();
      });
  });

  it ('Copy timetable', function(done) {
    request.post('/tables/'+table_id+'/copy/')
      .set('x-access-token', token)
      .expect(200)
      .end(function(err, res) {
        if (err) {
          console.log(res.body);
          return done(err);
        }
        request.get('/tables/'+table_id)
          .set('x-access-token', token)
          .expect(200)
          .end(function(err, res) {
            if (err) {
              console.log(res.body);
              return done(err);
            }
            assert.equal(res.body.lecture_list[0].course_number, "400.320");
            assert.equal(res.body.lecture_list[0].class_time_json[0].place, "302-308");
            done(err);
          });
      });
  });

  it ('Modify a lecture', function(done) {
    request.put('/tables/'+table_id+'/lecture/'+lecture_id)
      .set('x-access-token', token)
      .send({course_title:"abcd"})
      .expect(200)
      .end(function(err, res) {
        request.get('/tables/'+table_id)
          .set('x-access-token', token)
          .expect(200)
          .end(function(err, res) {
            if (err) done(err);
            if (res.body.lecture_list[0].course_title == "abcd") done();
            else done(new Error("lecture not updated"));
          });
      });
  });

  it ('Reset a lecture', function(done) {
    request.put('/tables/'+table_id+'/lecture/'+lecture_id+'/reset')
      .set('x-access-token', token)
      .expect(200)
      .end(function(err, res) {
        if (err) {
          console.log(res.body);
          done(err);
        }

        assert.equal(res.body.lecture_list[0].course_title, old_title, "Timetable applied");
        assert(!res.body.errcode, "No Errcode");
        assert.equal(lecture.course_title, old_title, "Lecture title reset");
        request.get('/tables/'+table_id)
          .set('x-access-token', token)
          .expect(200)
          .end(function(err, res) {
            if (err) {
              console.log(res.body);
            }
            assert.equal(res.body.lecture_list[0].course_title, old_title, "Timetable double-check");
            done(err);
          });
      });
  });

  it ('Modifying course/lecture number should fail', function(done) {
    request.put('/tables/'+table_id+'/lecture/'+lecture_id)
      .set('x-access-token', token)
      .send({course_number: "400.333", title:"abcd"})
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.ATTEMPT_TO_MODIFY_IDENTITY);
        if (err) {
          done(err);
        } else {
          request.put('/tables/' + table_id + '/lecture/' + lecture_id)
            .set('x-access-token', token)
            .send({lecture_number: "010", title: "abcd"})
            .expect(403)
            .end(function (err, res) {
              if (err) done(err);
            });
          done();
        }
      });
  });

  it ('Creating a same lecture should fail', function(done) {
    request.post('/tables/'+table_id+'/lecture/'+ref_lecture._id)
      .set('x-access-token', token)
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.DUPLICATE_LECTURE);
        if (err) console.log(res.body);
        done(err);
      });
  });

  it ('Delete a lecture', function(done) {
    request.delete('/tables/'+table_id+'/lecture/'+lecture_id)
      .set('x-access-token', token)
      .expect(200)
      .end(function(err, res) {
        if (err) {
          done(err);
          return;
        }
        if (res.body.lecture_list.length !== 0 &&
          res.body.lecture_list[0]._id == lecture_id) {
          err = new Error("lecture not deleted");
        }
        done(err);
      });
  });

  it ('Create a custom user lecture', function(done) {
    request.post('/tables/'+table_id+'/lecture/')
      .set('x-access-token', token)
      .send({
        "classification": "전선",
        "department": "컴퓨터공학부",
        "academic_year": "3학년",
        "course_title": "My Custom Lecture",
        "credit": 1,
        "class_time": "화(13-1)/목(13-1)",
        "instructor": "이제희",
        "quota": 15,
        "enrollment": 0,
        "remark": "컴퓨터공학부 및 제2전공생만 수강가능",
        "category": "",
        "created_at": "2016-03-31T07:56:44.137Z",
        "updated_at": "2016-03-31T07:56:44.137Z",
        /*
         * See to it that the server removes _id fields correctly
         */
        "_id": "56fcd83c041742971bd20a86",
        "colorIndex": 5,
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
            "place": "302-308",
            "_id": "56fcd83c041742971bd20a88"
          },
          {
            "day": 3,
            "start": 13,
            "len": 1,
            "place": "302-308",
            "_id": "56fcd83c041742971bd20a87"
          }
        ],
        "__v": 0
      })
      .expect(200)
      .end(function(err, res) {
        if (err) done(err);
        lecture = res.body.lecture_list[0];
        assert.equal(lecture.instructor, "이제희");
        assert.equal(lecture.class_time_json[0].place, "302-308");
        assert.equal(lecture.colorIndex, 5);
        done();
      });
  });

  it ('Create a custom user lecture overlapped in itself should fail', function(done) {
    request.post('/tables/'+table_id+'/lecture/')
      .set('x-access-token', token)
      .send({
        "classification": "전선",
        "department": "컴퓨터공학부",
        "academic_year": "3학년",
        "course_title": "My Custom Lecture",
        "credit": 1,
        "class_time": "화(13-1)/목(13-1)",
        "instructor": "이제희",
        "quota": 15,
        "enrollment": 0,
        "remark": "컴퓨터공학부 및 제2전공생만 수강가능",
        "category": "",
        "created_at": "2016-03-31T07:56:44.137Z",
        "updated_at": "2016-03-31T07:56:44.137Z",
        /*
         * See to it that the server removes _id fields correctly
         */
        "_id": "56fcd83c041742971bd20a86",
        "class_time_json": [
          {
            "day": 5,
            "start": 13.5,
            "len": 1.5,
            "place": "302-308"
          },
          {
            "day": 5,
            "start": 13,
            "len": 1,
            "place": "302-308"
          }
        ]
      })
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.LECTURE_TIME_OVERLAP);
        done(err);
      });
  });

  it ('Create a custom user lecture overlapped wtih other lecture should fail', function(done) {
    request.post('/tables/'+table_id+'/lecture/')
      .set('x-access-token', token)
      .send({
        "classification": "전선",
        "department": "컴퓨터공학부",
        "academic_year": "3학년",
        "course_title": "My Custom Lecture",
        "credit": 1,
        "class_time": "화(13-1)/목(13-1)",
        "instructor": "이제희",
        "quota": 15,
        "enrollment": 0,
        "remark": "컴퓨터공학부 및 제2전공생만 수강가능",
        "category": "",
        "created_at": "2016-03-31T07:56:44.137Z",
        "updated_at": "2016-03-31T07:56:44.137Z",
        /*
         * See to it that the server removes _id fields correctly
         */
        "_id": "56fcd83c041742971bd20a86",
        "class_time_json": [
          {
            "day": 1,
            "start": 13.5,
            "len": 1.5,
            "place": "302-308"
          },
          {
            "day": 3,
            "start": 13,
            "len": 1,
            "place": "302-308"
          }
        ]
      })
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.LECTURE_TIME_OVERLAP);
        done(err);
      });
  });

  it ('Create a custom user lecture with invalid color should fail', function(done) {
    request.post('/tables/'+table_id+'/lecture/')
      .set('x-access-token', token)
      .send({
        "classification": "전선",
        "department": "컴퓨터공학부",
        "academic_year": "3학년",
        "course_title": "My Custom Lecture2",
        "credit": 1,
        "class_time": "화(13-1)/목(13-1)",
        "instructor": "이제희",
        "quota": 15,
        "enrollment": 0,
        "remark": "컴퓨터공학부 및 제2전공생만 수강가능",
        "category": "",
        "created_at": "2016-03-31T07:56:44.137Z",
        "updated_at": "2016-03-31T07:56:44.137Z",
        "color": {
          "fg": "rgb(255, 255, 255)",
          "bg": "#555555"
        },
        /*
         * See to it that the server removes _id fields correctly
         */
        "_id": "56fcd83c041742971bd20a86",
        "class_time_json": [
          {
            "day": 2,
            "start": 1.5,
            "len": 3,
            "place": "302-308"
          }
        ],
        "__v": 0
      })
      .expect(400)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.INVALID_COLOR);
        done(err);
      });
  });

  it ('Reset a custom lecture should fail', function(done) {
    request.put('/tables/'+table_id+'/lecture/'+lecture._id+'/reset')
      .set('x-access-token', token)
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.IS_CUSTOM_LECTURE);
        done(err);
      });
  });

  it ('Create a custom user lecture again', function(done) {
    request.post('/tables/'+table_id+'/lecture/')
      .set('x-access-token', token)
      .send({
        "classification": "전선",
        "department": "컴퓨터공학부",
        "academic_year": "3학년",
        "course_title": "My Custom Lecture2",
        "credit": 1,
        "class_time": "화(13-1)/목(13-1)",
        "instructor": "이제희",
        "quota": 15,
        "enrollment": 0,
        "remark": "컴퓨터공학부 및 제2전공생만 수강가능",
        "category": "",
        "created_at": "2016-03-31T07:56:44.137Z",
        "updated_at": "2016-03-31T07:56:44.137Z",
        /*
         * See to it that the server removes _id fields correctly
         */
        "_id": "56fcd83c041742971bd20a86",
        "class_time_json": [
          {
            "day": 2,
            "start": 1.5,
            "len": 3,
            "place": "302-308"
          }
        ],
        "__v": 0
      })
      .expect(200)
      .end(function(err, res) {
        if (err) console.log(res.body);
        assert.equal(res.body.lecture_list.length, 2);
        done(err);
      });
  });

  it ('Modifying custom lecture with invalid timemask should fail', function(done) {
    request.put('/tables/'+table_id+'/lecture/'+lecture._id)
      .set('x-access-token', token)
      .send({"class_time_mask": [
          0,
          0,
          0,
          0,
          0,
          0,
          0
        ],"class_time_json": [
          {
            "day": 1,
            "start": 1.5,
            "len": 1.5,
            "place": "302-308"
          }]})
      .expect(400)
      .end(function(err, res) {
        if (err) {
          done(err);
        }
        assert.equal(res.body.errcode, ErrorCode.INVALID_TIMEMASK);
        done(err);
      });
  });

  it ('Modifying custom lecture with invalid color should fail', function(done) {
    request.put('/tables/'+table_id+'/lecture/'+lecture._id)
      .set('x-access-token', token)
      .send({'color': {
        'fg':'rgb(255, 255, 255)',
        'bg':'#555555'
      }})
      .expect(400)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.INVALID_COLOR);
        done(err);
      });
  });

  it ('Modifying custom lecture so that time is overlapped in itself should fail', function(done) {
    request.put('/tables/'+table_id+'/lecture/'+lecture._id)
      .set('x-access-token', token)
      .send({"class_time_json": [
          {
            "day": 5,
            "start": 1.5,
            "len": 2,
            "place": "302-308"
          },{
            "day": 5,
            "start": 1,
            "len": 2,
            "place": "302-308"
          }]})
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.LECTURE_TIME_OVERLAP);
        done(err);
      });
  });

  it ('Modifying custom lecture so that time is overlapped with other lecture should fail', function(done) {
    request.put('/tables/'+table_id+'/lecture/'+lecture._id)
      .set('x-access-token', token)
      .send({"class_time_json": [
          {
            "day": 2,
            "start": 1.5,
            "len": 2,
            "place": "302-308"
          }]})
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.LECTURE_TIME_OVERLAP);
        done(err);
      });
  });

  it ('Server handles string class time json', function(done) {
    request.put('/tables/'+table_id+'/lecture/'+lecture._id)
      .set('x-access-token', token)
      .send({"class_time_json": [
          {
            "day": 2,
            "start": 1.5,
            "len": 2,
            "place": "302-308"
          }]})
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.LECTURE_TIME_OVERLAP);
        done(err);
      });
  });

  it ('Create a custom user lecture with identity will fail', function(done) {
    request.post('/tables/'+table_id+'/lecture/')
      .set('x-access-token', token)
      .send({
        "course_number": "0000",
        "lecture_number": "0000",
        "classification": "전선",
        "department": "컴퓨터공학부",
        "academic_year": "3학년",
        "course_title": "My Custom Lecture Fail",
        "credit": 1,
        "class_time": "화(13-1)/목(13-1)",
        "instructor": "이제희",
        "quota": 15,
        "enrollment": 0,
        "remark": "컴퓨터공학부 및 제2전공생만 수강가능",
        "category": "",
        "created_at": "2016-03-31T07:56:44.137Z",
        "updated_at": "2016-03-31T07:56:44.137Z",
        /*
         * See to it that the server removes _id fields correctly
         */
        "_id": "56fcd83c041742971bd20a86",
        "class_time_json": [
          {
            "day": 2,
            "start": 13,
            "len": 1,
            "place": "302-308",
            "_id": "56fcd83c041742971bd20a88"
          },
          {
            "day": 4,
            "start": 13,
            "len": 1,
            "place": "302-308",
            "_id": "56fcd83c041742971bd20a87"
          }
        ],
        "__v": 0
      })
      .expect(403)
      .end(function(err, res) {
        assert.equal(res.body.errcode, ErrorCode.NOT_CUSTOM_LECTURE);
        done(err);
      });
  });
};
