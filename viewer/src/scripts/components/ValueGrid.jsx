/**                                                        
 * @jsx React.DOM
 */
'use strict';

var React = require('react');
var _     = require("underscore");
var Modal = require("react-bootstrap/Modal");
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
		'width' : '100%',
		'margin-left' : '5px',
		'margin-right' : '5px'
	};

	var minHeight = {//min-height not working so used height
		'height' : '25px'
	};

    var numCols = this.props.numCols;
    var rows = [];
    var vals = this.props.values;
    console.log('vals is ', vals);

    // var getMat = function(numCols, vals){

    // 	var mat = [];
    	if(vals != null){
    	for(var i = 0; i < vals.length; i ++){
    		var row = Math.floor(i / numCols);
            if(rows.length - 1 < row) { rows[row] = []; }
            rows[row].push(vals[i].replace(/"/g, ""));

            // //var temp = [];
    		// //for (var j = 0; j < numCols; j ++){
    		// 	if (vals[i] === "\"\""){
    		// 		temp.push( );
    		// 	}else{
      //               console.log(vals[i]);
    		// 		temp.push(vals[i]);    				
    		// 	}
    		// //}
    		// mat.push(temp);
    	}
    	}
    // 	return mat;
    // }

    //var mat = getMat(numCols, vals);

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