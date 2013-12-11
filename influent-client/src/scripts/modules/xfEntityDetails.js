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
define(['jquery', 'modules/xfWorkspace', 'lib/module', 'lib/channels', 'lib/constants'],
    function($, xfWorkspace, modules, chan, constants) {

        //--------------------------------------------------------------------------------------------------------------
        // Private Variables
        //--------------------------------------------------------------------------------------------------------------

        var MODULE_NAME = 'xfEntityDetails';

        var _UIObjectState = {
            UIType : MODULE_NAME,
            properties : {},
            parentId : '',
            subscriberTokens : null
        };

        //--------------------------------------------------------------------------------------------------------------
        // Private Methods
        //--------------------------------------------------------------------------------------------------------------

        var onSearch = function(channel, data) {
            $('#details').empty();
        };

        //--------------------------------------------------------------------------------------------------------------

        var onSelect = function(channel, data) {

            // Only retrieve details for xfCluster and xfCard objects.
            if (data != null &&
                (data.uiType == constants.MODULE_NAMES.ENTITY || data.uiSubtype == constants.SUBTYPES.ACCOUNT_OWNER)
            ) {
            	
            	// cache icon set
            	var iconList = xfWorkspace.getUIObjectByXfId(data.xfId).getVisualInfo().spec['icons'];
            	
                // get details for entity here
                aperture.io.rest(
                    '/entitydetails?queryId='+(new Date()).getTime()+'&entityId='+data.dataId,
                    'GET',
                    function(response){
                        $('#details').html(response.content);

                        var parent = $('<div class="detailsIconContainer"></div>').appendTo('#detailsHeaderInfo');
                        var url;
                        var icon;
                        var i;
                        
                        if (iconList) {
                            for (i = 0; i < iconList.length; i++) {
                                icon = iconList[i];
                                
                                // supersize it
                                url = icon.imgUrl
                                	.replace(/iconWidth=[0-9]+/, 'iconWidth=32')
                                	.replace(/iconHeight=[0-9]+/, 'iconHeight=32');
                                
                                parent.append('<img class= "detailsIcon" src="' + url
                                		+ '" title="' + icon.title
                                		+ '"/>');
                            }
                        }
                    }
                );
            } else {
                $('#details').html('');
            }
        };

        //--------------------------------------------------------------------------------------------------------------
        // Public
        //--------------------------------------------------------------------------------------------------------------

        var xfEntityDetailsModule = {};

        //--------------------------------------------------------------------------------------------------------------

        // Register the module with the system
        modules.register('xfEntityDetails', function() {
        	return {
	        	start : function() {
	                var subTokens = {};
	                subTokens[chan.SELECTION_CHANGE_EVENT] = aperture.pubsub.subscribe(chan.SELECTION_CHANGE_EVENT, onSelect);
	                subTokens[chan.SEARCH_REQUEST] = aperture.pubsub.subscribe(chan.SEARCH_REQUEST, onSearch);
	                _UIObjectState.subscriberTokens = subTokens;
	            },
	            end : function(){
	                for (var token in _UIObjectState.subscriberTokens) {
                        if (_UIObjectState.subscriberTokens.hasOwnProperty(token)) {
	                        aperture.pubsub.unsubscribe(_UIObjectState.subscriberTokens[token]);
                        }
	                }
	            }
        	};
        });

        return xfEntityDetailsModule;
    }
);