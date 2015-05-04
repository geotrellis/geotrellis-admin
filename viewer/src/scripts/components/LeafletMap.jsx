/**
 * @jsx React.DOM
 */

'use strict';

var React = require('react');
var _     = require("underscore");

var L = require('leaflet')
require('style!leaflet/dist/leaflet.css')

var Layers = {
  stamen: { 
    toner:      'http://{s}.tile.stamen.com/toner/{z}/{x}/{y}.png',   
    watercolor: 'http://{s}.tile.stamen.com/watercolor/{z}/{x}/{y}.png',
    attrib:     'Map data &copy;2013 OpenStreetMap contributors, Tiles &copy;2013 Stamen Design'
  },
  mapBox: {
    azavea:     'http://{s}.tiles.mapbox.com/v3/azavea.map-zbompf85/{z}/{x}/{y}.png',
    worldBlank: 'http://{s}.tiles.mapbox.com/v3/mapbox.world-blank-light/{z}/{x}/{y}.png',
    attrib:     'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery &copy; <a href="http://mapbox.com">MapBox</a>'
  }
};

var getLayer = function(url,attrib) {
  return L.tileLayer(url, { maxZoom: 18, attribution: attrib });
};

var baseLayers = {

  "Azavea" :      getLayer(Layers.mapBox.azavea,Layers.mapBox.attrib),
  "Watercolor" :  getLayer(Layers.stamen.watercolor,Layers.stamen.attrib),
  "Toner" :       getLayer(Layers.stamen.toner,Layers.stamen.attrib),
  "Blank" :       getLayer(Layers.mapBox.worldBlank,Layers.mapBox.attrib)
};


var LeafletMap = React.createClass({
  map: null, 
  layer: null,

  componentDidMount: function () {    
    this.map = L.map(this.getDOMNode());

    baseLayers['Azavea'].addTo(this.map);    
    this.map.lc = L.control.layers(baseLayers).addTo(this.map);
    this.map.setView([30.25, -97.75], 4);

    this_map = this.map;
  },

  render: function() {      
    var active = this.props.active; 
    var entry = this.props.active.entry;

    if (this.isMounted() && active.entry) {      
      this.map.setView([entry.center[1], entry.center[0]], entry.layer.zoom);
      
      var removeLayer = function (map, oldLayer){
        if (oldLayer) { 
          map.removeLayer(oldLayer) 
          map.lc.removeLayer(oldLayer);
        }


      }
      
      var oldLayer = this.layer;    
      var args = active.band;      
      args['breaks'] = active.entry.breaks.join(',');      
      var url = this.props.tmsUrl + "/" + active.entry.layer.name  + "/{z}/{x}/{y}?" + $.param( args );
      var newLayer = L.tileLayer(url, {minZoom: 1, maxZoom: 12, tileSize: 256, tms: false, opacity: 0.95});
      newLayer.addTo(this.map);
      this.map.lc.addOverlay(newLayer, entry.layer.name);
      this.layer = newLayer;

      _.delay(removeLayer, 1000, this.map, oldLayer);

    } 
    
    return (
      <div className="leafletMap" id="map" />
    );
  }
});

module.exports = LeafletMap;
