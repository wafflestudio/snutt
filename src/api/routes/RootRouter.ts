import ExpressPromiseRouter from 'express-promise-router';
import MonitorRouter = require('./MonitorRouter');
import StaticPageRouter = require('./StaticPageRouter');
import ApiRouter = require('./ApiRouter');
import mongoose = require('mongoose');

let router = ExpressPromiseRouter();

router.get('/health-check', (req, res) => {
  if (mongoose.connection.readyState !== 1) {
    res.status(500).json({ message: 'MongoDB connection failed' });
    return;
  }

  mongoose.connection.db.admin().ping((err, result) => {
    if (err) {
      res.status(500).json({ message: 'MongoDB connection failed' });
      return;
    }
    res.status(200).json({ message: 'ok' });
  });
});

router.use('/monitor', MonitorRouter);

router.use(function(req, res) {
  req['context'] = {};
  return Promise.resolve('next');
})
router.use('/', StaticPageRouter);
router.use('/v1', StaticPageRouter);
router.use('/', ApiRouter);
router.use('/v1', ApiRouter);

export = router;
