/**                                                        
 * @jsx React.DOM
 */
'use strict';

var React = require('react/addons');
var _     = require("underscore");
var Button = require("react-bootstrap/Button");
var count = 0;

var ValueAccordian = React.createClass({

    getInitialState: function() {
        var itemMap = this.props.items.map(function( i ) {
            return {
                content:i.content
            };
        });
        this.setState({itemMap: itemMap});
 
        return {
            itemMap: itemMap
        }
 
    },

    getDefaultItemStyle: function(){
    	 return {
            borderRadius: "3px",
            height: "50px",
            marginBottom: "5px",
            overflow: "hidden",
            border: "1px solid #cecece"
        }
    },

    getItemHeaderStyle: function() {
        return {
            height: "200px",
            backgroundColor: "#f9f9f9",
            cursor: "pointer"
        };
    },


    render: function() { 

    var _this = this;
    var items = this.props.items;
   

    var contents = items.map(function( i ) {
 
            //calculate the current style
            var itemStyle = _this.getDefaultItemStyle();
            if ( _this.props.mapClick) {
                itemStyle = _this.getItemHeaderStyle();
            } else {
                itemStyle.height = "0px";
            }
            return <div style={itemStyle} className={_this.props.itemClassName} >
                    {i.content}
            </div>
        });

    return(
    	<div  className={this.props.className}>
                 {contents}
             </div>     
              
    )}
});

module.exports = ValueAccordian;