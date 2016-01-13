var express = require('express');
var router = express.Router();

var apiRouter = require('./api/api.js');
var initRouter = require('./init'); //FOR DEBUG ONLY, REMOVE THIS LINE BEFORE DEPLOY

router.use('/api', apiRouter);
router.use('/init', initRouter); //FOR DEBUG ONLY, REMOVE THIS LINE BEFORE DEPLOY

module.exports = router;