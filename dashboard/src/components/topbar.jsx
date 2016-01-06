import React, { Component } from 'react';
import { Router, Link} from 'react-router'

export default class TopBar extends Component {
  render() {
    return <div id="bar-top" className="navbar navbar-default navbar-fixed-top">
      <div className="container">
        <div className="navbar-header">
          <a className="navbar-brand" href="#" id="brand_button"><b>SNUTT</b></a>
        </div>
        <div className="navbar-collapse collapse">
          <ul className="nav navbar-nav" id="main_navigation">
            <li><a className="btn dropdown-toggle btn-inverse" data-toggle="dropdown" href="#"><span id="semester_label">학기</span><span className="caret"></span></a></li>
            <li><Link to="/">강의 찾기</Link></li>
            <li><Link to="/my">내 강의 / <b className="badge-info">0학점</b></Link></li>
            <li><Link to="/export">저장하기</Link></li>
          </ul>
        </div>
      </div>
    </div>
  }
}
