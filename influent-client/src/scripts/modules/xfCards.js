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

define(['jquery', 'lib/module', 'lib/channels', 'lib/util/currency', 'modules/xfWorkspace', 'lib/models/xfClusterBase', 'lib/models/xfCard', 'lib/util/xfUtil'],
    function($, modules, chan, currency, xfWorkspace, xfClusterBase, xfCard, xfUtil) {
        var module = {};

        //--------------------------------------------------------------------------------------------------------------

        var _getClusterIcons = function (cluster, clusterCount) {
            
            var icons = [];

            var iconClassOrder = aperture.config.get()['influent.config'].iconOrder;
            var iconClassMap = aperture.config.get()['influent.config'].iconMap;
            var iconClass = null;
            
            for(var i = 0; i < iconClassOrder.length; i++) {
            	iconClass = iconClassOrder[i];
            	if (iconClassMap[iconClass]) {
            		var dist = iconClassMap[iconClass].distName || (iconClass.toLowerCase() + '-dist');
            		
            		if (cluster.properties[dist]) {
		        		var iconMap = iconClassMap[iconClass].map;
		        		var iconDist = cluster.properties[dist].range;
		        		var iconLimit = iconClassMap[iconClass].limit || 1;
		        		
		        		if(iconMap != null) {
		        			var newDistIcons = _generateDistIcons(iconClass, iconMap, iconDist, iconLimit, clusterCount);
		        			for(var j = 0; j < newDistIcons.length; j++) {
		        				icons.push(newDistIcons[j]);
		        			}
		        		}
		        		else {
		        			aperture.log.error("Unable to render icons of type " + iconClass + "; misconfiguration in config.js.");
		        		}
            		}
            	} else {
            		aperture.log.error('icon order references a type that doesnt exist');
            	}
            }
            
            return icons;
        };

        //--------------------------------------------------------------------------------------------------------------

        var _generateDistIcons = function (iconClass, iconMap, iconDist, iconLimit, clusterCount) {
        	var toReturn = [];
        	var count = 0;
        	var nextIcon = null;
            var value = null;
            var d = iconDist.distribution;
            var i;
            var freq;
            
            for (i=0; i< d.length; i++) {
            	freq = d[i];
            	
            	value = freq.range;
        		count = freq.frequency;
        		
        		nextIcon = iconMap(iconClass, value);
        		
        		if (nextIcon) {
                    toReturn.push(
                        {
        					imgUrl: nextIcon.url || aperture.palette.icon(nextIcon.icon),
        					title: (nextIcon.title || '') + ' ('+ count+ ')',
                            score : count / clusterCount,
                        }
                    );
        		}
            }

            toReturn.sort(function(a, b) {return b['score'] - a['score'];});

            if (toReturn.length > iconLimit) {
                toReturn.splice(iconLimit);
            }
            
            return toReturn;
        };
        
        //----------------------------------------------------------------------------------------------------------

        var _processEntityCluster = function(elementData, spec) {

            var stubMembers = [];
            for (var i = 0; i < elementData.members.length; i++) {
                var memberSpec = xfCard.getSpecTemplate();
                memberSpec.dataId = elementData.members[i];
                memberSpec.graphUrl = 'blank';
                stubMembers.push(memberSpec);
            }
            
            for (var i = 0; i < elementData.subclusters.length; i++) {
                var memberSpec = xfClusterBase.getSpecTemplate();
                memberSpec.dataId = elementData.subclusters[i];
                memberSpec.graphUrl = 'blank';
                stubMembers.push(memberSpec);
            }

        	var entityCount = xfWorkspace.getEntityCount(elementData);
        	var label = xfWorkspace.getValueByTag(elementData, 'LABEL');

            spec.dataId = elementData.uid;
            spec.isCluster = true;
            spec.members = stubMembers;
            spec.icons = _getClusterIcons(elementData, entityCount);
            spec.count = entityCount;
            spec.label = label;
        };

        //----------------------------------------------------------------------------------------------------------

        var _processEntity = function(elementData, spec) {
        	
            spec.dataId = elementData.uid;
            spec.isCluster = false;
            spec.label = xfWorkspace.getValueByTag(elementData, 'LABEL');
            spec.icons = [];

            // do icons
            var iconMap = aperture.config.get()['influent.config'].iconMap;
            var tag = '';
            var tagMap;
            var property;
            var properties;
            var i, j;
            var count;
            var limit = 4;
            var values;
            var icon;
            
            for (tag in iconMap) {
            	if (iconMap.hasOwnProperty(tag)) {
            		properties = xfWorkspace.getPropertiesByTag(elementData, tag);
            		
            		if (properties.length) {
                		tagMap = iconMap[tag];
                		count = 0;

                		for (i=0; i< properties.length; i++) {
                			property = properties[i];

                			if (property.range && property.range.values) {
	                			values = property.range.values;
                			} else {
                				values = [property.value];
                			}

                			for (j=0; j< values.length; j++) {
	                			icon = tagMap.map(property.key, values[j]);
	
	                			if (icon) {
	                				spec.icons.push({
	                					imgUrl: icon.url || aperture.palette.icon(icon.icon),
	                					title: icon.title
	                				});
	                				
		                			if ((tagMap.limit && ++count === tagMap.limit) || spec.icons.length === limit) {
		                				break;
		                			}
	                			}
                			}
                		}
            		}
            	}
            	
            	if (spec.icons.length === limit) {
            		break;
            	}
            }
        };
        
        //--------------------------------------------------------------------------------------------------------------

        var _populateSpecWithData = function(elementData, spec) {

            if (elementData == null) {
                return;
            }

            if (spec.hasOwnProperty('confidenceInSrc') && elementData.uncertainty) {
                spec.confidenceInSrc = elementData.uncertainty.confidence;
                
                var doubleEncode = aperture.config.get()['influent.config'].doubleEncodeSourceUncertainty;
                
                if (doubleEncode != null && !doubleEncode) {
                    spec.confidenceInAge = elementData.uncertainty.currency != null? 
                    		elementData.uncertainty.currency : 1.0;
                } else {
            		spec.confidenceInAge = spec.confidenceInSrc < 1.0? 0.0: 1.0;
                }
            }
            
            if (elementData.isCluster) {
                _processEntityCluster(elementData, spec);
            } else {
                _processEntity(elementData, spec);
            }
        };

        //--------------------------------------------------------------------------------------------------------------

        var _invalidateCardCharts = function(specs) {

            for (var i = 0; i < specs.length; i++) {

                var spec = specs[i];
                if (!spec.graphUrl) {
                    continue;
                }

                var objects = xfWorkspace.getUIObjectsByDataId(spec.dataId);
                for (var j = 0; j < objects.length; j++) {
                    var object = objects[j];
                    object.update({
                        graphUrl : ''
                    });
                    aperture.pubsub.publish(chan.RENDER_UPDATE_REQUEST, {UIObject : object});
                }
            }
        };

        //--------------------------------------------------------------------------------------------------------------

        var _updateCardsWithCharts = function(specs) {

            _invalidateCardCharts(specs);

            var updates = [];
            var focus = xfWorkspace.getFocus();

            getMaxDebitCredit(
                focus,
                function(focusMDC) {
                    for (var i = 0; i < specs.length; i++) {
                    	 var specUIObj = xfWorkspace.getUIObjectsByDataId(specs[i].dataId)[0];            
                         var columnObj = xfUtil.getUITypeAncestor(specUIObj, 'xfColumn');

                         updates.push({
                            dataId : specs[i].dataId,
                            isCluster : specs[i].isCluster,
                            contextId : columnObj.getXfId()
                        });
                    }

                    updateCardTypeWithCharts(focus, focusMDC, updates);
                }
            );
        };

        //--------------------------------------------------------------------------------------------------------------

        function getMaxDebitCredit(focus, onReturn) {

            if (focus == null) {
            	aperture.log.error('no focus account in chart request. bailing out of request!');
                return;
            }
            
            var entity = {
                contextId : focus.contextId,
                dataId : focus.dataId
            };

            // Separate call for files?
            aperture.io.rest(
                    '/chart',
                    'POST',
                    function(response) {
                        // pull MDC out of response
                        var maxCreditDebit = (response[focus.dataId].maxDebit > response[focus.dataId].maxCredit) ? response[focus.dataId].maxDebit : response[focus.dataId].maxCredit;

                        onReturn(maxCreditDebit);
                    },
                    {
                        postData: {
                        	sessionId : xfWorkspace.getSessionId(),
                            entities : [entity],
                            startDate : xfWorkspace.getFilterDates().startDate,
                            endDate :  xfWorkspace.getFilterDates().endDate,
                            numBuckets : xfWorkspace.getFilterDates().numBuckets,
                            focusId : focus.memberIds,
                            focusMaxDebitCredit : "",
                            focuscontextid : focus.contextId
                        },
                        contentType: 'application/json'
                    }
                );
        }

        //--------------------------------------------------------------------------------------------------------------

        function updateCardTypeWithCharts(focus, focusMDC, entityIdArray) {

            if ( entityIdArray.length == 0 ) {
                return;
            }

            // Sort the files from the other uiObjects. We handle files
            // separately to account for the pseudo-ability to cluster.
            var nonFileClusterArray = entityIdArray;

            if (!_.isEmpty(nonFileClusterArray)){
                // Update the charts for non-collapsed files.
                aperture.io.rest(
                    '/chart',
                    'POST',
                    _updateCard,
                    {
                        postData: {
                            sessionId : xfWorkspace.getSessionId(),
                            entities : nonFileClusterArray,
                            startDate : xfWorkspace.getFilterDates().startDate,
                            endDate :  xfWorkspace.getFilterDates().endDate,
                            numBuckets : xfWorkspace.getFilterDates().numBuckets,
                            focusId : focus.memberIds,
                            focusMaxDebitCredit : focusMDC,
                            focuscontextid : focus.contextId
                        },
                        contentType: 'application/json'
                    }
                );
            }
        }

        //--------------------------------------------------------------------------------------------------------------

        function _updateCard(response) {

            var parents = {};
            for (var dataId in response) {
                if (response.hasOwnProperty(dataId)) {
                    var cards = xfWorkspace.getUIObjectsByDataId(dataId);
                    for (var i = 0; i < cards.length; i++) {

                        var card = cards[i];

                        // Store the parent in the map.
                        var parent = card.getParent();
                        parents[parent.getXfId()] = parent;

                        var maxDebit = currency.formatNumber(response[dataId].maxDebit);
                        var prefixDebit = (maxDebit.charAt(0) === '0') ? '' : '-';

                        var maxCredit = currency.formatNumber(response[dataId].maxCredit);
                        var prefixCredit = (maxCredit.charAt(0) === '0') ? '' : '+';

                        card.showDetails(xfWorkspace.getVisualInfo().showDetails);
                        card.update({
                                graphUrl : aperture.io.restUrl('/chart?hash=' + xfUtil.getSafeURI(response[dataId].imageHash)),
                                flow : {flowIn: response[dataId].totalCredit, flowOut: response[dataId].totalDebit},
                                detailsTextNodes :
                                    [
                                        // should these not just be an html string? why pass in components?
                                        {
                                            fontSize: '11px',
                                            color:'#6b5646',
                                            text: prefixDebit + maxDebit,
                                            padding: 2
                                        },
                                        {
                                            fontSize: '10px',
                                            color: '#969696',
                                            text:'to',
                                            padding: 2
                                        },
                                        {
                                            fontSize: '11px',
                                            color:'#6b5646',
                                            text: prefixCredit + maxCredit
                                        }
                                    ]}
                        );
                    }
                }
            }

            for (var parentId in parents) {
                if (parents.hasOwnProperty(parentId)) {
                    aperture.pubsub.publish(chan.RENDER_UPDATE_REQUEST, {UIObject : parents[parentId]});
                }
            }
        }

        //--------------------------------------------------------------------------------------------------------------
        module.updateCardsWithCharts = _updateCardsWithCharts;
        module.populateSpecWithData = _populateSpecWithData;
        //--------------------------------------------------------------------------------------------------------------

        // Register the module with the system
        modules.register('xfCards', function(sandbox) {
            xfWorkspace.registerChildModule(module);

        	return {
        		start: function() {
                    xfWorkspace.start(sandbox.spec.sessionId);
        		},
        		end : function() {
                    xfWorkspace.end();
        		}
        	};
        });
        
        return module;
    }
);

