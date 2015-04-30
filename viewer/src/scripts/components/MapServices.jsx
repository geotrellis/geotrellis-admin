/**
 * @jsx React.DOM
 */

'use strict';

var React = require('react');
var Button = require("react-bootstrap/Button");
var DropdownButton = require("react-bootstrap/DropdownButton");
var MenuItem = require("react-bootstrap/MenuItem");
var _     = require("underscore");

var MapServices = React.createClass({
   getInitialState: function() {
    $.get(this.props.url + "/colors/",
      function(colors) { 
        if (this.isMounted()) { 
          this.setState({'colorMap': colors.colors.colors})
          this.setState({'currentRamp': colors.colors.colors[0].image})
        }
      }.bind(this)
    );

    return {'colorMap': []}
  },

  handleSelectColorRamp: function(event, reactId) {
    var colorRampDOM = event.target.children[0] || event.target;       //selected ramp img or space around image
    var currentRampIndex = colorRampDOM.dataset["key"];
    var currentRampImage = this.state.colorMap[currentRampIndex].image;
    if(this.state.currentRamp !== currentRampImage) {    //only render on new selection
      this.setState({'currentRamp': currentRampImage});  
      this.props.active.set(
         {
           'colorRamp' : this.state.colorMap[currentRampIndex].key,
           'layerName' : this.props.catalog[0].layer.name
         } 
      );
    }
  },

  render: function(){
    var self = this;
    var colorOptions = _.map(this.state.colorMap, function(colorRampKey, key) {
      return (<MenuItem onClick={self.handleSelectColorRamp} >
                <img width="120" img src={colorRampKey.image} data-key={key} />
              </MenuItem>
            );
    });

    var inline = {'display' : "inline-block"};
    var inlinePadding = {'display' : "inline-block",
                         'padding-left' : "115px"};

	  return (
      <div className ="row">
        <div className ="col-md-7">
          <img src={this.state.currentRamp} height="12px" width="170px" /> <br></br>
            <li style={inline}> Low </li> 
            <li style={inlinePadding}> High </li> 
        </div>
  	  	<div className ="col-md-2">
        </div>
	  	  <DropdownButton title="Color Ramps" bsStyle="primary" bsSize="medium">
          {colorOptions}
        </DropdownButton>
  		</div>
	  );
  }
});

module.exports = MapServices;
