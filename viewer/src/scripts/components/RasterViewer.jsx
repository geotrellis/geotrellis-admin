/**
 * @jsx React.DOM
 */

'use strict';

var React = require('react');
var LeafletMap = require('./LeafletMap.jsx');
var MapServices = require('./MapServices.jsx')
var Catalog = require('./Catalog.jsx');
var Input = require("react-bootstrap/Input");
var TabbedArea = require("react-bootstrap/TabbedArea");
var TabPane = require("react-bootstrap/TabPane");
var Cursor = require('react-cursor').Cursor;

var $ = require('jquery');

var RasterViewer = React.createClass({
  getInitialState: function() {
    return { 
      url: "http://localhost:8088",
      catalog: [],      
      active: {
        entry:  null,
        band:   null
      }      
    }
  },

  componentDidMount: function() {
    this.handleChangeCatalogUrl();
  },
  
  handleChangeCatalogUrl: function() {  
    var url = this.refs.url.getValue();
    $.get(url + "/catalog/", 
      function(result) {
        if (this.isMounted()) { this.setState({ catalog: result, url: url }) }
      }.bind(this)
    );
  },

  render: function() { 
    var self = this;
    var cursor = Cursor.build(this);
    return (
      <div className="row">      
        <div className="col-md-9">
          <MapServices catalog={this.state.catalog} url={this.state.url} active={cursor.refine('active')} />
          <LeafletMap tmsUrl={this.state.url + "/tms"} active={this.state.active} /> 
        </div>

        <div className="col-md-3">
          <Input type="text" 
            defaultValue={this.state.url} 
            ref="url"
            groupClassName="group-class"
            wrapperClassName="wrapper-class"
            labelClassName="label-class" 
            onChange={self.handleChangeCatalogUrl} />
        
          <Catalog catalog={this.state.catalog} url={this.state.url} active={cursor.refine('active')} /> 
        </div>
      </div>
    )}
});


module.exports = RasterViewer;
