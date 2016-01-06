var express = require('express');
var router = express.Router();

var apiRouter = require('./api/api.js');

router.use('/api', apiRouter);

module.exports = router;