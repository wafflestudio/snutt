var express = require('express');
var router = express.Router();

var Timetable = require('../../model/timetable');

router.get('/', function(req, res, next) { //timetable list
  Timetable.find({'user_id' : req.user._id}).select('year semester title')
  .exec(function(err, timetables) {
    if(err) return next(err);
    res.json(timetables);
  });
});

router.get('/:id', function(req, res, next) { //get
  Timetable.findOne({'user_id': req.user._id, '_id' : req.params.id})
  .exec(function(err, timetable) {
    if(err) return next(err);
    if(!timetable) return res.json({success : false});
    res.json(timetable);
  });
});

router.post('/', function(req, res, next) { //create
  var timetable = new Timetable({
    user_id : req.user._id, 
    year : req.body.year,
    semester : req.body.semester,
    title : req.body.title,
    lecture_list : []
  });
  timetable.save(function(err) {
    if(err) return res.json({success: false, message : 'Timetable save failed'});
    res.json({success : true, timetable : timetable});
  });
});

router.delete('/:id', function(req, res, next) { // delete
  Timetable.findOneAndRemove({'user_id': req.user._id, '_id' : req.params.id})
  .exec(function(err) {
    if(err) return next(err);
    res.json({success : true });
  });
});

router.put('/:id', function(req, res, next) {
  Timetable.findOneAndUpdate({'user_id': req.user._id, '_id' : req.params.id}
    , {
      title : req.body.title
    }, function(err, timetable) {
      if(err) return res.json({success : false, message : 'FIXME'});
      res.json({success : true, timetable : timetable});
    });
  
});




module.exports = router;