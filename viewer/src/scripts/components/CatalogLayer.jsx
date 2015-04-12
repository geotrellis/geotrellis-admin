/**
 * @jsx React.DOM
 */

'use strict';

var React = require('react');
var Table = require("react-bootstrap/Table");
var Button = require("react-bootstrap/Button");
var _     = require("underscore");


/**
 * Props:
 * layerName
 * entries[]
 * url
 */
var CatalogLayer = React.createClass({
    getInitialState: function() {
                         var layer = this.props.entries[0].layer;

                         $.get(this.props.url + "/catalog/" + layer.name + "/" + layer.zoom + "/bands", 
                             function(bands) {
                                 if (this.isMounted()) { this.setState({'bands': bands }) }
                             }.bind(this)
                             );

                         return {'bands': []}; // list of bands in this layer
                     },

    handleSelectLayer: function() {

                           var zoom = this.refs.zoom.getDOMNode().value.trim();
                           if (this.refs.band == null){//if no node for empty band, provide an empty value
                               var band = "";
                           }else{
                               var band = this.refs.band.getDOMNode().value.trim();
                           }
                           var bands;

                           if (band == "")
                               bands = {};
                           else
                               bands = {'time': band };
                           var entry = _.find(this.props.entries, function(e) {return e.layer.zoom == zoom});

                           this.props.active.set(
                                   {
                                       'entry': entry,
                               'band': bands 
                                   } 
                                   );
                       },


    render: function () {
                var self = this;
                var entries = this.props.entries;
                var entry = this.props.entries[0];
                var layer = entry.layer;

                var zoomOptions = _.map(entries, function(e) {
                    return <option value={e.layer.zoom}>{e.layer.zoom}</option>;
                })

                var bandOptions = _.map(this.state.bands.time, function(time) {    
                    return <option value={time}>{time}</option>;        
                });

                if (bandOptions.length > 0){ //provide a selector for bands with values, none for those without
                    return (        
                            <tr>
                            <td>
                            <Button onClick={this.handleSelectLayer} bsStyle="primary" bsSize="xsmall">{ this.props.layerName }</Button>        
                            </td>
                            <td>
                            <select ref="zoom">{ zoomOptions }</select>
                            </td>
                            <td>
                            <select ref="band">{ bandOptions }</select>
                            </td>
                            </tr>);
                }
                else{
                    return(
                            <tr>
                            <td>
                            <Button onClick={this.handleSelectLayer} bsStyle="primary" bsSize="xsmall">{ this.props.layerName }</Button>        
                            </td>
                            <td>
                            <select ref="zoom">{ zoomOptions }</select>
                            </td>
                            </tr>);
                }
            }  
});

module.exports = CatalogLayer;
