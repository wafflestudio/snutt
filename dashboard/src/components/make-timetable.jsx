import React, { Component } from 'react';

import Search from './search.jsx'
import Timetable from './timetable.jsx'

var samples = [
  {
    title: '논리와 비판적 사고',
    days: [0, 2, 4],
    time: [
      {start: 4, dur: 2},
      {start: 4, dur: 2},
      {start: 4, dur: 1}
    ]
  },
  {
    title: '수및연',
    days: [1, 3],
    time: [
      {start: 2, dur: 3},
      {start: 2, dur: 3}
    ]
  }
]

export default class MakeTimeTable extends Component {
  constructor() {
    super()
    this.state = {
      courses: samples
    }
  }
  render() {
    return <div className="container">
      <Search />
      <Timetable courses={this.state.courses}/>
    </div>
  }
}
