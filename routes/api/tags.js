/**
 * Created by north on 16. 2. 24.
 */
var express = require('express');
var router = express.Router();
var TagList = require('../../model/tagList');

router.get('/:year/:semester/update_time', function(req, res, next) {
  TagList.findOne({'year' : req.params.year, 'semester' : req.params.semester},'updated_at', function (err, docs) {
    if (err) next(err);
    if (docs == null) res.status(404).send('not found');
    else res.send(docs.updated_at);
  });
});

router.get('/:year/:semester/', function(req, res, next) {
  TagList.findOne({'year' : req.params.year, 'semester' : req.params.semester},'tags', function (err, docs) {
    if (err) next(err);
    if (docs == null) res.status(404).send('not found');
    else res.send(docs.tags);
  });
});

module.exports = router;