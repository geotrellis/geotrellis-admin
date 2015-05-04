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

//   componentWillReceiveProps: function(nextProps) {
//     console.log('recieved props &&&', nextProps);
//   this.setState({
//     values : nextProps
//   });
// },
	
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
    var vals = this.props.values;

    var getMat = function(numCols, vals){

    	var mat = [];
    	if(vals != null){
    	for(var i = 0; i < numCols; i ++){
    		var temp = [];
    		for (var j = 0; j < numCols; j ++){
    			if (vals[i] === "\"\""){
    				temp.push( );
    			}else{
    				temp.push(vals[i]);    				
    			}
    		}
    		mat.push(temp);
    	}
    	}
    	return mat;
    }

    var mat = getMat(numCols, vals);

    var gridTh = function(){
    	return _.map(mat, function(val){
    	return <th style= {minHeight}>{val}</th>;
    })
    };

    var gridTr = _.map(mat, function(vals){
    	return <tr >{ gridTh(vals.length) }</tr>
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