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
var Layers = require('./Layers.jsx');
var ValueAccordian = require('./ValueAccordian.jsx');
var ValueGrid = require('./ValueGrid.jsx');
var Accordion = require("react-bootstrap/Accordion");
var Button = require("react-bootstrap/Button");

var $ = require('jquery');

var RasterViewer = React.createClass({
  getInitialState: function() {
    return { 
      url: "http://localhost:8088",
      catalog: [],      
      active: {
        entry:  null,
        band:   null
      },
      values : null,
    numCols : null         
    }
  },

  componentDidMount: function() {
    this.handleChangeCatalogUrl();
  },

  handleLayersUrl: function(){
      var url = this.refs.url.getValue();
      //gtUrl
      $.get(url + "/gt/colors/",
        function(result){
        }.bind(this)
        );
  },
  
  handleChangeCatalogUrl: function() {  
    var url = this.refs.url.getValue();
    $.get(url + "/catalog/", 
      function(result) {
        if (this.isMounted()) { this.setState({ mapClick: false, catalog: result, url: url }) }
      }.bind(this)
    );
  },

  handleMouseClick: function(childComponent) {  
    this.setState({ mapClick: true,
      values : childComponent.state.values,
    numCols : childComponent.state.numCols
    });
  },

  render: function() { 
    var self = this;
    var cursor = Cursor.build(this);
    var items = [{ content: <ValueGrid values = {this.state.values} numCols = {this.state.numCols}/>}];
    
    return (
      <div className="row">      
        <div className="col-md-9">
          <MapServices catalog={this.state.catalog} url={this.state.url} active={cursor.refine('active')} />
          <LeafletMap onClick={self.handleMouseClick} Url={this.state.url} tmsUrl={this.state.url + "/tms"} values = {this.state.values} numCols = {this.state.numCols} active={this.state.active} /> 
        </div>

        <div className="col-md-3">
          <Input type="text" 
            defaultValue={this.state.url} 
            ref="url"
            groupClassName="group-class"
            wrapperClassName="wrapper-class"
            labelClassName="label-class" 
            onChange={self.handleChangeCatalogUrl} />

            <div>
                <ValueAccordian mapClick={this.state.mapClick} items={items} className="test" itemClassName="test-item" />
            </div>
        
          <Catalog catalog={this.state.catalog} url={this.state.url} active={cursor.refine('active')} /> 
        </div>
      </div>
    )}
});


module.exports = RasterViewer;
