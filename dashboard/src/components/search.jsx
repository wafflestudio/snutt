import React, { Component } from 'react';
import SearchBar from './search-bar.jsx'
import SearchResult from './search-result.jsx'

export default class Search extends Component {
  render() {
    return <div>
    <h1> Hello, I'm search </h1>
    <SearchBar />
    <SearchResult />
    </div>
  }
}
