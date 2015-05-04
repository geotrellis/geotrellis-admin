/**
 * @jsx React.DOM
 */

'use strict';

var React = require('react');
var LeafletMap = require('./LeafletMap.jsx');
var Catalog = require('./Catalog.jsx');
var ValueViewer = require('./ValueViewer.jsx');
var Layers = require('./Layers.jsx');
var ValueModal = require('./ValueModal.jsx');
var ValueGrid = require('./ValueGrid.jsx');
var Input = require("react-bootstrap/Input");
var TabbedArea = require("react-bootstrap/TabbedArea");
var TabPane = require("react-bootstrap/TabPane");
var Cursor = require('react-cursor').Cursor;
var ModalTrigger = require("react-bootstrap/ModalTrigger");
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
      }      
    }
  },

  componentDidMount: function() {
    this.handleChangeCatalogUrl();
  },

  handleLayersUrl: function(){
      var url = this.refs.url.getValue();
      //gtUrl
      console.log('IN HANDLE LAYERS URL')
      $.get(url + "/gt/colors/",
        function(result){
          console.log('result from colors is ', result);
        }.bind(this)
        );
  },
  
  handleChangeCatalogUrl: function() {  
    var url = this.refs.url.getValue();
    console.log('in handleChangeCatalogUrl:', url)
    $.get(url + "/catalog/", 
      function(result) {
        console.log('result: ', result)
        if (this.isMounted()) { this.setState({ catalog: result, url: url }) }
      }.bind(this)
    );
  },

  render: function() { 
    var self = this;
    var cursor = Cursor.build(this);
    console.log('state is ', this.state)
    console.log('props is ', this.props)
    return (
      <div className="row">

        <div className="col-md-9">
          <LeafletMap Url={this.state.url} tmsUrl={this.state.url + "/tms"} active={this.state.active} randomVar ={self.handleLayersUrl} /> 
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
