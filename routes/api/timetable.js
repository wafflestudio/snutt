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
  // TODO : Check if a timetable with same title exists!
  timetable.save(function(err) {
    if(err) return res.json({success: false, message : 'Timetable save failed'});
    res.json({success : true, timetable : timetable});
  });
});

/*
 * POST /timetable/:id/lecture
 * add a lecture into a timetable
 * param ===================================
 * lecture : json object of lecture to add
 */
router.post('/:id/lecture', function(req, res, next) {
  Timetable.findOne({'user_id': req.user_id, '_id' : req.params.id})
    .exec(function(err, timetable){
      if(err) return next(err);
      if(!timetable) return res.json({success : false});
      timetable.add_lecture(req.body.lecture, function(err){
        if(err) return next(err);
        res.json({success : true });
      });
    })
});

/*
 * POST /timetable/:id/lectures
 * add lectures into a timetable
 * param ===================================
 * lectures : array of lectures to add
 */
router.post('/:id/lectures', function(req, res, next) {
  Timetable.findOne({'user_id': req.user_id, '_id' : req.params.id})
    .exec(function(err, timetable){
      if(err) return next(err);
      if(!timetable) return res.json({success : false});
      timetable.add_lectures(req.body.lectures, function(err){
        if(err) return next(err);
        res.json({success : true });
      });
  })
});

/*
 * PUT /timetable/:id/lecture
 * update a lecture of a timetable
 * param ===================================
 * lecture : json object of lecture to update
 */
router.put('/:id/lecture', function(req, res, next) {
  Timetable.findOne({'user_id': req.user_id, '_id' : req.params.id})
    .exec(function(err, timetable){
      if(err) return next(err);
      if(!timetable) return res.json({success : false});
      timetable.update_lecture(req.body.lecture, function(err){
        if(err) return next(err);
        res.json({success : true });
      });
    })
});

/*
 * PUT /timetable/:id/lecture
 * delete a lecture from a timetable
 * param ===================================
 * lecture : json object of lecture to delete
 */
router.put('/:id/lecture', function(req, res, next) {
  Timetable.findOne({'user_id': req.user_id, '_id' : req.params.id})
    .exec(function(err, timetable){
      if(err) return next(err);
      if(!timetable) return res.json({success : false});
      timetable.delete_lecture(req.body.lecture, function(err){
        if(err) return next(err);
        res.json({success : true });
      });
    })
});

/*
 * DELETE /timetable/:id
 * delete a timetable
 */
router.delete('/:id', function(req, res, next) { // delete
  Timetable.findOneAndRemove({'user_id': req.user._id, '_id' : req.params.id})
  .exec(function(err) {
    if(err) return next(err);
    res.json({success : true });
  });
});

/*
 * POST /timetable/:id/copy
 * copy a timetable
 */
router.put('/:id/copy', function(req, res, next) {
  Timetable.findOne({'user_id': req.user_id, '_id' : req.params.id})
    .exec(function(err, timetable){
      if(err) return next(err);
      if(!timetable) return res.json({success : false});
      var copied = timetable.copy();
      res.json({success : true, timetable : copied});
    })
});

router.put('/:id', function(req, res, next) {
  Timetable.findOneAndUpdate({'user_id': req.user._id, '_id' : req.params.id},
    {
      title : req.body.title
    }
    , function(err, timetable) {
      if(err) return res.json({success : false, message : 'FIXME'});
      res.json({success : true, timetable : timetable});
    });
  
});

module.exports = router;