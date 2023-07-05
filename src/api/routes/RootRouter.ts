import ExpressPromiseRouter from 'express-promise-router';
import MonitorRouter = require('./MonitorRouter');
import StaticPageRouter = require('./StaticPageRouter');
import ApiRouter = require('./ApiRouter');

let router = ExpressPromiseRouter();

router.get('/health-check', (req, res) => res.sendStatus(200));
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
