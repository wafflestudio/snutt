"use strict";

import express = require('express');
import mongoose = require('mongoose');
var router = express.Router();

import {timeJsonToMask} from '../../lib/util';

import {TimetableModel, TimetableDocument} from '../../model/timetable';
import {UserLectureModel} from '../../model/lecture';
import {UserModel, UserDocument} from '../../model/user';
import util = require('../../lib/util');

router.get('/', function(req, res, next) { //timetable list
  var user:UserDocument = <UserDocument>req["user"];
  TimetableModel.getTimetables(user._id, {lean:true}, function(err, timetables) {
    if (err) return res.status(500).json({message:'fetch timetable list failed'});
    res.json(timetables);
  });
});

router.get('/recent', function(req, res, next) {
  var user:UserDocument = <UserDocument>req["user"];
  TimetableModel.getRecent(user._id, {lean:true}, function(err, timetable) {
    if (err) return res.status(500).json({message:'find table failed'});
    if (!timetable) return res.status(404).json({message:'no timetable'});
    res.json(timetable);
  });
});

router.get('/:id', function(req, res, next) { //get
  var user:UserDocument = <UserDocument>req["user"];
  TimetableModel.getTimetable(user._id, req.params.id, {lean:true}, function(err, timetable) {
    if(err) return res.status(500).json({message:"find table failed"});
    if(!timetable) return res.status(404).json({message:'timetable not found'});
    res.json(timetable);
  });
});

router.get('/:year/:semester', function(req, res, next) {
  var user:UserDocument = <UserDocument>req["user"];
  TimetableModel.getTimetablesBySemester(user._id, req.params.year, req.params.semester, {lean:true},
    function(err, timetable) {
      if(err) return res.status(500).json({message:"find table failed"});
      if(!timetable) return res.status(404).json({message:"No timetable for given semester"});
      res.json(timetable);
  });
});

router.post('/', function(req, res, next) { //create
  var user:UserDocument = <UserDocument>req["user"];
  if (!req.body.year || !req.body.semester || !req.body.title)
    return res.status(400).json({message:'not enough parameters'});

  TimetableModel.createTimetable({
    user_id : user._id,
    year : req.body.year,
    semester : req.body.semester,
    title : req.body.title})
    .then(function(doc) {
      TimetableModel.getTimetables(user._id, {lean:true}, function(err, timetables){
        if (err) return res.status(500).json({message:'get timetable list failed'});
        res.json(timetables);
      });
    })
    .catch(function(err) {
      if (err == 'duplicate title')
        return res.status(403).json({message: err});
      else
        return res.status(500).json({message: err});
    });
});

/**
 * POST /tables/:timetable_id/lecture/:lecture_id
 * add a lecture into a timetable
 * param ===================================
 * Lecture id from search query
 */
router.post('/:timetable_id/lecture/:lecture_id', function(req, res, next) {
  var user:UserDocument = <UserDocument>req["user"];
  TimetableModel.findOne({'user_id': user._id, '_id' : req.params.timetable_id}).exec()
    .then(function(timetable){
      if(!timetable) return res.status(404).json({message:"timetable not found"});
      UserLectureModel.findOne({'_id': req.params.lecture_id}).lean()
        .exec(function(err, ref_lecture){
          util.object_del_id(ref_lecture);
          var lecture = new UserLectureModel(ref_lecture);
          timetable.add_lecture(lecture, function(err, timetable){
            if(err) {
              return res.status(403).json({message:"insert lecture failed"});
            }
            res.json(timetable);
          });
        });
    })
    .catch(function(err) {
      return res.status(500).json({message:"find table failed"});
    });
});

/**
 * POST /tables/:id/lecture
 * add a lecture into a timetable
 * param ===================================
 * json object of lecture to add
 */
router.post('/:id/lecture', function(req, res, next) {
  var user:UserDocument = <UserDocument>req["user"];
  TimetableModel.findOne({'user_id': user._id, '_id' : req.params.id})
    .exec(function(err, timetable){
      if(err) return res.status(500).json({message:"find table failed"});
      if(!timetable) return res.status(404).json({message:"timetable not found"});
      var json = req.body;
      if (json.class_time_json) json.class_time_mask = timeJsonToMask(json.class_time_json);
      else if (json.class_time_mask) delete json.class_time_mask;
      /*
       * Sanitize json using object_del_id.
       * If you don't do it,
       * the existing lecture gets overwritten
       * which is potential security breach.
       */
      util.object_del_id(json);
      var lecture = new UserLectureModel(json);
      timetable.add_lecture(lecture, function(err, timetable){
        if(err) {
          return res.status(403).json({message:"insert lecture failed"});
        }
        res.json(timetable);
      });
    });
});

/**
 * POST /tables/:id/lectures
 * add lectures into a timetable
 * param ===================================
 * lectures : array of lectures to add
 */
/*
router.post('/:id/lectures', function(req, res, next) {
  Timetable.findOne({'user_id': req.user_id, '_id' : req.params.id})
    .exec(function(err, timetable){
      if(err) return res.status(500).json({message:"find table failed"});
      if(!timetable) return res.status(404).json({message:"timetable not found"});
      var lectures = [];
      var lectures_raw = req.body['lectures'];
      for (var lecture_raw in lectures_raw) {
        lecture_raw.class_time_mask = timeJsonToMask(lecture_raw.class_time_json);
        var lecture = new Lecture(lecture_raw);
        lectures.push(lecture);
      }
      timetable.add_lectures(lectures, function(err){
        if(err) return res.status(500).json({message:"insert lecture failed"});
        res.json({message:"ok"});
      });
  })
});
*/

/**
 * PUT /tables/:table_id/lecture/:lecture_id
 * update a lecture of a timetable
 * param ===================================
 * json object of lecture to update
 */

// TODO : duplicate timetable query fix
router.put('/:table_id/lecture/:lecture_id', function(req, res, next) {
  var user:UserDocument = <UserDocument>req["user"];
  var lecture_raw = req.body;
  if(!lecture_raw || Object.keys(lecture_raw).length < 1) return res.status(400).json({message:"empty body"});

  if (!req.params.lecture_id)
    return res.status(400).json({message:"need lecture_id"});

  TimetableModel.findOne({'user_id': user._id, '_id' : req.params.table_id})
    .exec(function(err, timetable){
      if(err) return res.status(500).json({message:"find table failed"});
      if(!timetable) return res.status(404).json({message:"timetable not found"});
      if (lecture_raw.class_time_json)
        lecture_raw.class_time_mask = timeJsonToMask(lecture_raw.class_time_json);
      timetable.update_lecture(req.params.lecture_id, lecture_raw, function(err, doc) {
        if(err) {
          if (err.message == "modifying identities forbidden" ||
            err.message == "lecture not found")
            return res.status(403).json({message:err.message});
          console.log(err);
          return res.status(500).json({message:"update lecture failed"});
        }
        res.json(doc);
      });
    });
});

/**
 * DELETE /tables/:table_id/lecture/:lecture_id
 * delete a lecture from a timetable
 */
router.delete('/:table_id/lecture/:lecture_id', function(req, res, next) {
  var user:UserDocument = <UserDocument>req["user"];
  TimetableModel.findOneAndUpdate(
    {'user_id': user._id, '_id' : req.params.table_id},
    { $pull: {lecture_list : {_id: req.params.lecture_id} } }, {new: true})
    .exec(function (err, doc) {
      if (err) {
        console.log(err);
        return res.status(500).json({message:"delete lecture failed"});
      }
      if (!doc) return res.status(404).json({message:"timetable not found"});
      res.json(doc);
    });
});

/**
 * DELETE /tables/:id
 * delete a timetable
 */
router.delete('/:id', function(req, res, next) { // delete
  var user:UserDocument = <UserDocument>req["user"];
  TimetableModel.findOneAndRemove({'user_id': user._id, '_id' : req.params.id}).lean()
  .exec(function(err, timetable) {
    if(err) return res.status(500).json({message:"delete timetable failed"});
    if (!timetable) return res.status(404).json({message:"timetable not found"});
    TimetableModel.getTimetables(user._id, {lean:true}, function(err, timetables) {
      if (err) return res.status(500).json({message:"failed to get list"});
      res.json(timetables);
    });
  });
});

/**
 * POST /tables/:id/copy
 * copy a timetable
 */
router.post('/:id/copy', function(req, res, next) {
  var user:UserDocument = <UserDocument>req["user"];
  TimetableModel.findOne({'user_id': user._id, '_id' : req.params.id})
    .exec(function(err, timetable){
      if(err) return res.status(500).json({message:"find table failed"});
      if(!timetable) return res.status(404).json({message:"timetable not found"});
      timetable.copy(timetable.title, function(err, doc) {
        if(err) return res.status(500).json({message:"timetable copy failed"});
        TimetableModel.getTimetables(user._id, {lean:true}, function(err, timetables) {
          if (err) return res.status(500).json({message:"failed to get list"});
          res.json(timetables);
        });
      });
    });
});

router.put('/:id', function(req, res, next) {
  var user:UserDocument = <UserDocument>req["user"];
  if (!req.body.title) return res.status(400).json({message:"should provide title"});
  TimetableModel.findOne({'user_id': user._id, '_id' : req.params.id})
    .exec(function(err, timetable) {
      if(err) return res.status(500).json({message:"update timetable title failed"});
      timetable.title = req.body.title;
      timetable.checkDuplicate(function(err) {
        if (err) return res.status(403).json({message:"duplicate title"});
        timetable.save(function (err, doc) {
          if (err) return res.status(500).json({message:"update timetable title failed"});
          TimetableModel.getTimetables(user._id, {lean:true}, function(err, timetables) {
            if (err) return res.status(500).json({message:"failed to get list"});
            res.json(timetables);
          });
        });
      });
    });
});

export = router;