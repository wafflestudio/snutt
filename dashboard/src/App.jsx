import React, { Component } from 'react';

import TopBar from './components/topbar.jsx'
import Footer from './components/footer.jsx'


export default class App extends Component {
  render() {
    return <div>
      <TopBar />
      {this.props.children}
      <Footer />
    </div>
  }
}
