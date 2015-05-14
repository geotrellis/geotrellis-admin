/**                                                        
 * @jsx React.DOM
 */
'use strict';

var React = require('react');
var _     = require("underscore");
var Button = require("react-bootstrap/Button");
var Table = require("react-bootstrap/Table");
var Col = require("react-bootstrap/Col");
var Row = require("react-bootstrap/Row");

var ValueGrid = React.createClass({
	getInitialState: function() {
    return {
      values : [],
      numCols : null
    };
  },



	
	render: function() {

	var css = {
		'width' : '90%',
		'margin-left' : '5px',
		'margin-right' : '5px',
        'font-size' : '70%'
	};

	var minHeight = {//min-height not working so used height
		'height' : '25px'
	};

    var numCols = this.props.numCols;
    var rows = [];
    var vals = this.props.values;

    	if(vals != null){
        	for(var i = 0; i < vals.length; i ++){
        		var row = Math.floor(i / numCols);
                if(rows.length - 1 < row) { rows[row] = []; }
                rows[row].push(vals[i].replace(/"/g, ""));
        	}
    	}

    var gridTh = function(rowVals){
    	return _.map(rowVals, function(val){
    	return <th style= {minHeight}>{val}</th>;
    })
    };

    var gridTr = _.map(rows, function(row){
    	return <tr >{ gridTh(row) }</tr>
    });
    return (
    			<Table striped bordered condensed hover style = {css}>
    <tbody>
      { gridTr }
    </tbody>
  </Table>
    	    );
  }
});

module.exports = ValueGrid;