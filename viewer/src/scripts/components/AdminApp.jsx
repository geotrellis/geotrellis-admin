/**
 * @jsx React.DOM
 */

'use strict';

var React = require('react/addons');
var ReactTransitionGroup = React.addons.TransitionGroup;
var Input = require("react-bootstrap/Input");


// Export React so the devtools can find it
(window !== window.top ? window.top : window).React = React;

// CSS
require('../../styles/normalize.css');
require('../../styles/main.css');
require('bootstrap-webpack')

var RasterViewer = require("./RasterViewer.jsx")
React.renderComponent(<RasterViewer />, document.getElementById('content')); // jshint ignore:line

module.exports = {};
