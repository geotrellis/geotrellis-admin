/**
 * @jsx React.DOM
 */

'use strict';

var React = require('react');
var _     = require("underscore");

var L = require('leaflet')
var BootstrapSlider = require("bootstrap-slider")
var Slider = require("./Slider.jsx");

require('style!bootstrap-slider/dist/css/bootstrap-slider.css')
require('style!leaflet/dist/leaflet.css')

var Layers = {
  stamen: { 
    toner:  'http://{s}.tile.stamen.com/toner/{z}/{x}/{y}.png',   
    terrain: 'http://{s}.tile.stamen.com/terrain/{z}/{x}/{y}.png',
    watercolor: 'http://{s}.tile.stamen.com/watercolor/{z}/{x}/{y}.png',
    attrib: 'Map data &copy;2013 OpenStreetMap contributors, Tiles &copy;2013 Stamen Design'
  },
  mapBox: {
    azavea:     'http://{s}.tiles.mapbox.com/v3/azavea.map-zbompf85/{z}/{x}/{y}.png',
    worldGlass:     'http://{s}.tiles.mapbox.com/v3/mapbox.world-glass/{z}/{x}/{y}.png',
    worldBlank:  'http://{s}.tiles.mapbox.com/v3/mapbox.world-blank-light/{z}/{x}/{y}.png',
    worldLight: 'http://{s}.tiles.mapbox.com/v3/mapbox.world-light/{z}/{x}/{y}.png',
    attrib: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery &copy; <a href="http://mapbox.com">MapBox</a>'
  }
};

var getLayer = function(url,attrib) {
  return L.tileLayer(url, { maxZoom: 18, attribution: attrib });
};

var baseLayers = {

  "Azavea" : getLayer(Layers.mapBox.azavea,Layers.mapBox.attrib),
  "World Light" : getLayer(Layers.mapBox.worldLight,Layers.mapBox.attrib),
  "Terrain" : getLayer(Layers.stamen.terrain,Layers.stamen.attrib),
  "Watercolor" : getLayer(Layers.stamen.watercolor,Layers.stamen.attrib),
  "Toner" : getLayer(Layers.stamen.toner,Layers.stamen.attrib),
  "Glass" : getLayer(Layers.mapBox.worldGlass,Layers.mapBox.attrib),
  "Blank" : getLayer(Layers.mapBox.worldBlank,Layers.mapBox.attrib)
};


var LeafletMap = React.createClass({
  map: null, 
  layer: null,

  getInitialState: function () {
    return {'value' : 0.5}
  },

  componentDidMount: function () {    
    this.map = L.map(this.getDOMNode());

    baseLayers['Azavea'].addTo(this.map);    
    this.map.lc = L.control.layers(baseLayers).addTo(this.map);
    this.map.setView([30.25, -97.75], 4);
    this_map = this.map;
  },

  shouldComponentUpdate: function(nextProps, nextState) {
    if(nextState.opacity) {  //if changing opacity
      if(nextState.value !== this.state.value && this.layer) {
        this.layer.setOpacity(nextState.value);
      }
      nextState.opacity = false;  //only allow handleOpacityUpdate to change opacity through Slider
      return false;
    } 
    return true;
  },

  handleOpacityUpdate: function(event) {
    this.setState({
      value: event,
      opacity: true
    });
  },

  handleUpdateLayer: function() {
    var active = this.props.active; 
    var entry = this.props.active.entry;
    this.map.setView([entry.center[1], entry.center[0]], entry.layer.zoom);
      var removeLayer = function (map, oldLayer){
        if (oldLayer) { 
          map.removeLayer(oldLayer) 
          map.lc.removeLayer(oldLayer);
        }
      }
     
      var args = active.band; 
      args['breaks'] = active.entry.breaks.join(',');

      if(this.layer) {
        var tmsUrl = this.layer._url
        var findColorRegex = /&colorRamp=/;
        tmsUrlSplit = tmsUrl.split(findColorRegex);
        if(tmsUrlSplit[1]) {
          args['colorRamp'] = tmsUrlSplit[1];
        }   
      }
 
      var oldLayer = this.layer; 
      var url = this.props.tmsUrl + "/" + active.entry.layer.name  + "/{z}/{x}/{y}?" + $.param( args );
      var newLayer = L.tileLayer(url, {minZoom: 1, maxZoom: 12, tileSize: 256, tms: false, opacity: this.state.value});
      newLayer.addTo(this.map);
      this.map.lc.addOverlay(newLayer, entry.layer.name);
      this.layer = newLayer;
      _.delay(removeLayer, 1000, this.map, oldLayer);
  },

  handleUpdateColorRamp: function() {
    var active = this.props.active; 
    var tmsUrl = this.layer._url
    var findColorRegex = /&colorRamp/;
    var tmsUrlSplit = tmsUrl.split(findColorRegex);
    if(tmsUrlSplit) {
      tmsUrl = tmsUrlSplit[0];
    }
    var args = {'colorRamp' : active.colorRamp};
    this.layer.setUrl(tmsUrl + "&" + $.param(args));
  },

  render: function() {     
    if (this.isMounted() && this.props.active.entry) {      
      this.handleUpdateLayer();
    } 

    if(this.isMounted() && this.props.active.colorRamp && this.layer) {
      this.handleUpdateColorRamp();
    }

    var css = {'padding-left' : '50px',
               'padding-top' : '5px'};
    
    return (
      <div>
        <div style={css}>
          <Slider id="opacity-slider" min={0} max={1} step={0.02} value={this.state.value} toolTip={false} onSlide={this.handleOpacityUpdate}/>
        </div>
        <div>
          <div className="leafletMap" id="map"/>
        </div>
      </div>
    );
  }
});

module.exports = LeafletMap;
