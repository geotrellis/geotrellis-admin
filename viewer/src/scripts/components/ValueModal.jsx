/**                                                        
 * @jsx React.DOM
 */
'use strict';

var React = require('react');
var _     = require("underscore");
var Modal = require("react-bootstrap/Modal");
var Button = require("react-bootstrap/Button");
var ValueGrid = require('./ValueGrid.jsx');
var ReactTransitionGroup = React.addons.TransitionGroup;
var props = {};

var ValueModal = React.createClass({

  getInitialState: function() {
    return {
      values : [],
      numCols : ""
    };
  },

  componentDidMount: function() {
    $.get(this.props.source, function(props) {
      if (this.isMounted()) {
        this.setState({
          values : props.values,
          numCols : props.numCols
        });
      }
    }.bind(this));
  },

  componentWillReceiveProps: function(nextProps) {
  this.setState({
    values : nextProps.values,
    numCols : nextProps.numCols
  });
},


  render: function() {
    return (
      <Modal props={props} bsStyle='primary' title='Value Grid' animation={false}>
        <div className='modal-body'>
     
          <p>Geotrellis value grid</p>

          <div>
          <ValueGrid values = {this.props.values} numCols = {this.props.numCols}/>
          </div>

          <hr />

          </div>
        <div className='modal-footer'>
          <Button onClick={this.props.onRequestHide}>Close</Button>
        </div>
      </Modal>
    );
  }
});

module.exports = ValueModal;