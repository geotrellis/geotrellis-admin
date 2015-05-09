/**
 * @jsx React.DOM
 */

'use strict';

var React = require('react');
var _     = require("underscore");


var Layers = React.createClass({

	layerJson: function(){
	$.getJSON(gtUrl('catalog'), function(catalog) {
    $("#page-title").text("GeoTrellis Viewer - " + catalog.name);

    treeNodes = _.map(catalog.stores,function(store) {
        return {
            title: store.name, 
            isFolder: true,
            key: store.name,
            children: _.map(store.layers, function(layer) {
                return { title: layer, store: store.name }
            })
        }
    });

    if(treeNodes.length == 0) {
        $("#catalog-tree").append($("<span class='emptymsg'>Catalog is empty!</span>"));
    } else {

        $("#catalog-tree").dynatree({
            onActivate: function(node) {
                if(!node.data.isFolder) {
                    $.getJSON(gtUrl("layer/info?layer="+node.data.title+"&store="+node.data.store), function(layer) {
                        $.getJSON(gtUrl("layer/bbox?layer="+node.data.title+"&store="+node.data.store), function(bbox) {
                            layer.store = node.data.store;
                            layer.bbox = bbox;
                            layerViewer.setLayer(layer);
                            layerInfo.setLayer(layer);
                        });
                    });
                }
            },
            children: treeNodes
        });
    }
});
},

	render: function(){


	}

	});

module.exports = Layers;
