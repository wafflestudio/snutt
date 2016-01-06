import React from 'react';
import { render } from 'react-dom';
import { Router, Route, browserHistory, IndexRoute } from 'react-router'
require('../stylesheets/style.scss');

import App from './App.jsx'
import MakeTimetable from './components/make-timetable.jsx'
import MyTimetable from './components/my-timetable.jsx'
import ExportTimetable from './components/export-timetable.jsx'

render((
    <Router history={browserHistory}>
    <Route path="/" component={App}>
      <IndexRoute component={MakeTimetable} />
      <Route path="my" component={MyTimetable} />
      <Route path="export" component={ExportTimetable} />
    </Route>
  </Router>
), document.getElementById('root'))
