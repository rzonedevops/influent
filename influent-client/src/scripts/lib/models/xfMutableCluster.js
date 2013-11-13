/**
 * Copyright (c) 2013 Oculus Info Inc.
 * http://www.oculusinfo.com/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
define(['jquery', 'lib/interfaces/xfUIObject', 'lib/channels', 'lib/util/GUID', 'lib/util/xfUtil', 'lib/models/xfCard', 'lib/models/xfClusterBase', 'lib/extern/underscore'],
    function($, xfUIObject, chan, guid, xfUtil, xfCard, xfClusterBase) {

        //--------------------------------------------------------------------------------------------------------------
        // Private Variables
        //--------------------------------------------------------------------------------------------------------------

        var MODULE_NAME = 'xfMutableCluster';

        //--------------------------------------------------------------------------------------------------------------
        // Public
        //--------------------------------------------------------------------------------------------------------------

        var xfMutableCluster = {};

        //--------------------------------------------------------------------------------------------------------------

        xfMutableCluster.createInstance = function(spec){

            var guidId = guid.generateGuid();

            var _UIObjectState = {
                xfId                : 'mutable_' + guid.generateGuid(),
                UIType              : MODULE_NAME,
                spec                : _.clone(spec),
                toolbarSpec         : xfClusterBase.getToolbarSpecTemplate(),
                children            : [],
                isExpanded          : false,
                isSelected          : false,
                isHighlighted       : false,
                isHovered			: false,
                showToolbar         : false,
                showDetails         : false,
                showSpinner         : false,
                isPinned            : false,
                links               : {}
            };

            // Create a data id for this object.
            _UIObjectState.spec.dataId =  'data_mutable_' + guidId;

            // create new object instance
            var xfClusterInstance = xfClusterBase.createInstance(_UIObjectState);

            //----------
            // Overrides
            //----------

            xfClusterInstance.clone = function() {

                // create cloned object
                var clonedObject = xfMutableCluster.createInstance(_UIObjectState.spec);

                // add cloned children
                for (var i = 0; i < _UIObjectState.children.length; i++) {
                    clonedObject.insert(_UIObjectState.children[i].clone());
                }

                // add necessary UI state
                clonedObject.showDetails(_UIObjectState.showDetails);
                (_UIObjectState.isExpanded) ? clonedObject.expand() : clonedObject.collapse();

                // make the cloned object an orphan
                clonedObject.setParent(null);

                // return cloned object
                return clonedObject;
            };

            //----------------------------------------------------------------------------------------------------------

            xfClusterInstance.removeChild = function(xfId, disposeObject, preserveLinks) {

                var i, linkId=null, link;

                for (i = 0; i < _UIObjectState.children.length; i++) {
                    if (_UIObjectState.children[i].getXfId() == xfId) {

                        var links = [];
                        if (preserveLinks) {
                            links = _UIObjectState.children[i].getLinks();
                        }

                        _UIObjectState.children[i].setParent(null);

                        if (disposeObject) {
                            _UIObjectState.children[i].dispose();
                            _UIObjectState.children[i] = null;

                            for (linkId in links) {
                                if (links.hasOwnProperty(id)) {
                                    link = links[id];
                                    if (link.getSource().getXfId() != xfId) {
                                        link.getSource().addLink(link);
                                    } else if (link.getDestination().getXfId() != xfId) {
                                        link.getDestination().addLink(link);
                                    }
                                }
                            }
                        }

                        _UIObjectState.children.splice(i, 1);
                        _UIObjectState.spec.members.splice(i, 1);

                        break;
                    }
                }

                if (_UIObjectState.children.length == 0) {
                    aperture.pubsub.publish(chan.REMOVE_REQUEST, {xfId : _UIObjectState.xfId});
                }
            };

            //----------------------------------------------------------------------------------------------------------

            xfClusterInstance.removeAllChildren = function() {

                if (_UIObjectState.children.length == 0) {
                    return;
                }

                for (var i = 0; i < _UIObjectState.children.length; i++) {
                    _UIObjectState.children[i].dispose();
                    _UIObjectState.children[i] = null;
                }

                _UIObjectState.children.length = 0;
                _UIObjectState.spec.members.length = 0;
            };

            //----------------------------------------------------------------------------------------------------------

            xfClusterInstance.insert = function(xfUIObj, beforeXfUIObj00) {

                var memberSpec = _.clone(xfUIObj.getVisualInfo().spec);
                memberSpec.parent = this;

                if (beforeXfUIObj00 == null) {
                    _UIObjectState.children.push(xfUIObj);
                    // Update the member spec list.
                    _UIObjectState.spec.members.push(memberSpec);
                } else {
                    var inserted = false;
                    var childCount = _UIObjectState.children.length;
                    for (var i = 0; i < childCount; i++) {
                        if (_UIObjectState.children[i].getXfId() == beforeXfUIObj00.getXfId()) {
                            _UIObjectState.children.splice(i, 0, xfUIObj);
                            // Update the member spec list.
                            _UIObjectState.spec.members.splice(i, 0, memberSpec);

                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) {
                        _UIObjectState.children.push(xfUIObj);
                    }
                }

                xfUIObj.setParent(this);

                // we set the children's toolbar state base on our toolbar state. However,
                // we need to specifically set our children to allow close
                xfUIObj.updateToolbar(_UIObjectState.toolbarSpec);
                xfUIObj.updateToolbar({'allowClose': true});
            };

            //----------------------------------------------------------------------------------------------------------

            xfClusterInstance.restoreVisualState = function(state) {

                _UIObjectState.xfId = state.xfId;
                _UIObjectState.UIType = state.UIType;

                _UIObjectState.isExpanded = state.isExpanded;
                _UIObjectState.isSelected = state.isSelected;
                _UIObjectState.isHighlighted = state.isHighlighted;
                _UIObjectState.showToolbar = state.showToolbar;
                _UIObjectState.showDetails = state.showDetails;
                _UIObjectState.showSpinner = false;
                _UIObjectState.isPinned = state.isPinned;
                _UIObjectState.toolbarSpec = state.toolbarSpec;

                _UIObjectState.spec.dataId = state.spec.dataId;
                _UIObjectState.spec.isCluster = state.spec.isCluster;
                _UIObjectState.spec.count = state.spec.count;
                _UIObjectState.spec.icons = state.spec.icons;
                _UIObjectState.spec.detailsTextNodes = state.spec.detailsTextNodes;
                _UIObjectState.spec.graphUrl = state.spec.graphUrl;
                _UIObjectState.spec.duplicateCount = state.spec.duplicateCount;
                _UIObjectState.spec.label = state.spec.label;
                _UIObjectState.spec.confidenceInSrc = state.spec.confidenceInSrc;
                _UIObjectState.spec.confidenceInAge = state.spec.confidenceInAge;
                _UIObjectState.spec.leftOperation = state.spec.leftOperation;
                _UIObjectState.spec.rightOperation = state.spec.rightOperation;
                _UIObjectState.spec.flow = state.spec.flow;
                _UIObjectState.spec.members = state.spec.members;

                _UIObjectState.children = [];
                var childCount = state.children.length;
                for (var i = 0; i < childCount; i++) {
                    if (state.children[i].UIType == xfCard.getModuleName()) {
                        var cardSpec = xfCard.getSpecTemplate();
                        var cardUIObj = xfCard.createInstance(cardSpec);
                        cardUIObj.cleanState();
                        cardUIObj.restoreVisualState(state.children[i]);
                        this.insert(cardUIObj, null);
                    } else if (state.children[i].UIType == 'xfMutableCluster') {
                        var clusterSpec = xfMutableCluster.getSpecTemplate();
                        var clusterUIObj = xfMutableCluster.createInstance(clusterSpec);
                        clusterUIObj.cleanState();
                        clusterUIObj.restoreVisualState(state.children[i]);
                        this.insert(clusterUIObj, null);
                    } else {
                        console.error("cluster children should only be of type " + xfCard.getModuleName() + " or " + xfMutableCluster.getModuleName() + ".");
                    }
                }
            };

            //----------------------------------------
            // Mutable Cluster Specific Implementation
            //----------------------------------------

            //----------------------------------------------------------------------------------------------------------

            xfClusterInstance.getContainedCardDataIds = function() {
                var containedIds = [];

                for (var i = 0; i < _UIObjectState.children.length; i++) {
                    var child = _UIObjectState.children[i];
                    if (child.getUIType() == 'xfCard' && child.getDataId() != null) {
                        containedIds.push(child.getDataId());
                    } else if (child.getUIType() == 'xfMutableCluster') {
                        containedIds = containedIds.concat(child.getContainedCardDataIds());
                    }
                }

                return containedIds;
            };

            //----------------------------------------------------------------------------------------------------------

            return xfClusterInstance;
        };

        //--------------------------------------------------------------------------------------------------------------

        xfMutableCluster.getSpecTemplate = function() {
            return xfClusterBase.getSpecTemplate();
        };

        //--------------------------------------------------------------------------------------------------------------

        xfMutableCluster.getModuleName = function() {
            return MODULE_NAME;
        };

        //--------------------------------------------------------------------------------------------------------------

        return xfMutableCluster;
    }
);