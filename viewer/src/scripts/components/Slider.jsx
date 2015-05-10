/**
 * @jsx React.DOM
 */

'use strict';

var React = require('react');
    jQuery = require('jquery'),
    BootstrapSlider = require('bootstrap-slider');

var Slider = React.createClass({

  propTypes: {
    id: React.PropTypes.string,
    min: React.PropTypes.number,
    max: React.PropTypes.number,
    step: React.PropTypes.number,
    value: React.PropTypes.number.isRequired,
    toolTip: React.PropTypes.bool,
    onSlide: React.PropTypes.func
  },

  getDefaultProps: function() {
    return {
      min: 0,
      max: 100,
      step: 1,
      value: 50,
      toolTip: false,
      onSlide: function() {}
    };
  },

  componentWillUpdate: function(nextProps, nextState) {
    nextState.slider.setValue(nextProps.value);
  },

  componentDidMount: function() {
    var toolTip = this.props.toolTip ? 'show' : 'hide';
    var slider = new BootstrapSlider(this.getDOMNode(), {
      id: this.props.id,
      min: this.props.min,
      max: this.props.max,
      step: this.props.step,
      value: this.props.value,
      tooltip: toolTip
    });
    
    slider.on('slide', function(event) {
      this.props.onSlide(event);
      this.state.slider.setValue(event);
    }.bind(this));

    slider.on('slideStop', function(event) {
      this.props.onSlide(event);
      this.state.slider.setValue(event);
    }.bind(this));

    this.setState({
      slider: slider
    });
  },

  render: function() {
    return (
      <div />
    );
  }
});

module.exports = Slider;
