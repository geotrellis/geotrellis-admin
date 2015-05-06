/**
 * @jsx React.DOM
 */

'use strict';

var React = require('react');
var Table = require("react-bootstrap/Table");
var CatalogLayer = require('./CatalogLayer.jsx');
var _     = require("underscore");
              

var Catalog = React.createClass({
  handleSelectLayer: function(entry) {
    this.props.active.set(
      {
        'entry': entry,
        'band': {}         
      } 
    );
  },

  render: function () {
    var self = this;
 
    var layerGroups = _.groupBy(this.props.catalog, function(r) {return r.layer.name});

    var layerList = _.map(_.keys(layerGroups), function(layerName) {  
      var entries = _.sortBy(layerGroups[layerName], function(e) { return e.layer.zoom });
      return (<CatalogLayer url={self.props.url} active={self.props.active} layerName={layerName} entries={entries} />);
    });

    return (
    <Table responsive>
      <thead>
        <tr> <th>Layer</th> <th>Zoom</th><th>Band</th></tr>        
      </thead>
      <tbody> {layerList} </tbody>
    </Table>);

  }
});

module.exports = Catalog;
