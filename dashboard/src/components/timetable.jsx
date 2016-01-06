import React, { Component } from 'react'

class CourseCell extends Component {
  render() {
    var divStyle = {
      height: `${this.props.length * 100}%`
    }
    return <div className="course-div" style={divStyle}>
      {this.props.title}
    </div>
  }
}

export default class Timetable extends Component {
  constructor() {
    super()
    this.state = {
      cells: this.emptyCells()
    }
  }

  emptyCells() {
    var empty = new Array(6)
    for (var d = 0; d < 6; d++) {
      empty[d] = new Array(20)
    }
    return empty
  }

  componentWillMount() {
    this.fillCells()
  }

  componentWillReceiveProps() {
    this.fillCells()
  }

  fillCells() {
    var newCells = this.emptyCells()
    for (var course of this.props.courses) {
      for (var day of course.days) {
        var time = course.time[course.days.indexOf(day)]
        newCells[day][time.start] = <CourseCell title={course.title} length={time.dur}/>
      }
    }
    this.setState({
      cells: newCells
    })
  }

  render() {
    return <table className="table table-bordered timetable">
      <thead>
        <tr>
          <th>Mon</th>
          <th>Tue</th>
          <th>Wed</th>
          <th>Thu</th>
          <th>Fri</th>
          <th>Sat</th>
        </tr>
      </thead>
      {this.drawTable()}
    </table>
  }

  drawTable() {
    var rows = [];
    for (var t = 0; t < 20; t++) {
      rows.push(<tr key={t}>
        <td>{this.state.cells[0][t]}</td>
        <td>{this.state.cells[1][t]}</td>
        <td>{this.state.cells[2][t]}</td>
        <td>{this.state.cells[3][t]}</td>
        <td>{this.state.cells[4][t]}</td>
        <td>{this.state.cells[5][t]}</td>
      </tr>)
    }
    return <tbody>{rows}</tbody>
  }
}
