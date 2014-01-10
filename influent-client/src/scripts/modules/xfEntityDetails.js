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
            subscriberTokens : null,
            lastRequestedObject : null
        };

        var rowType = {
            'URL': 'url',
            'SCOREBAR': 'score-bar',
            'SCORE': 'score'
        };

        var _renderDefaults = {
            SCORE_BAR_WIDTH     : 24,
            SCORE_BAR_HEIGHT    : 4
        };

        //--------------------------------------------------------------------------------------------------------------
        // Private Methods
        //--------------------------------------------------------------------------------------------------------------

        var _onSearch = function(channel, data) {
            $('#details').empty();
        };

        //--------------------------------------------------------------------------------------------------------------

        var _onSelect = function(channel, data) {

            // Only retrieve details for xfCluster and xfCard objects.
            if (data != null) {

                _UIObjectState.lastRequestedObject = data.xfId;

                if (data.uiType == constants.MODULE_NAMES.ENTITY ||
                    data.uiSubtype == constants.SUBTYPES.ACCOUNT_OWNER
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

                                    parent.append(
                                        '<img class= "detailsIcon" src="' +
                                        url +
                                        '" title="' +
                                        icon.title +
                                        '"/>'
                                    );
                                }
                            }
                        }
                    );
                } else {
                    $('#details').empty();
                    aperture.pubsub.publish(chan.REQUEST_OBJECT_BY_XFID, {xfId:data.xfId});
                }
            } else {
                _UIObjectState.lastRequestedObject = null;
                $('#details').html('');
            }
        };

        //--------------------------------------------------------------------------------------------------------------

        var _createClusterDetails = function(eventChannel, data) {

            var obj = data.clonedObject;

            if (eventChannel != chan.OBJECT_FROM_XFID ||
                _UIObjectState.lastRequestedObject == null ||
                obj.getXfId() != _UIObjectState.lastRequestedObject
            ) {
                return;
            }

            var spec = obj.getVisualInfo().spec;

            var div = $('#details');

            var detailsHeader = $('<div></div>');
            detailsHeader.attr('id', 'detailsHead');
            detailsHeader.css('height', '30px');
            div.append(detailsHeader);

            var countLabel = (spec.count != null && spec.count > 1) ? ' (+' + (spec.count - 1) + ')' : '';
            var label = spec.label + countLabel;

            var textWrapNodeContainer = $('<div></div>');
            textWrapNodeContainer.addClass('textWrapNodeContainer');
            textWrapNodeContainer.html('<b>' + label + '</b><br>');
            detailsHeader.append(textWrapNodeContainer);

            var detailsBody = $('<div></div>');
            detailsBody.attr('id', 'detailsBody');
            div.append(detailsBody);

            var iconClassOrder = aperture.config.get()['influent.config'].iconOrder;
            var iconClassMap = aperture.config.get()['influent.config'].iconMap;

            for(var i = 0; i < iconClassOrder.length; i++) {
                var detailsTable = $('<table><tbody></tbody></table>');
                detailsTable.addClass('propertyTable');
                detailsTable.css('bottom', '2px');
                detailsTable.css('left', '0px');
                detailsTable.css('padding-bottom', '14px');
                detailsBody.append(detailsTable);
                _addRow(detailsTable, iconClassOrder[i], iconClassMap, spec, rowType.URL);
                _addRow(detailsTable, iconClassOrder[i], iconClassMap, spec, rowType.SCOREBAR);
                _addRow(detailsTable, iconClassOrder[i], iconClassMap, spec, rowType.SCORE);
            }
        };

        //--------------------------------------------------------------------------------------------------------------

        var _addRow = function(detailsTable, iconClass, iconClassMap, spec, type) {

            var row = null;

            if (iconClassMap[iconClass]) {
                for (var j = 0; j < spec.icons.length; j++) {
                    var icon = spec.icons[j];
                    if (icon.type == iconClass) {

                        if (row == null) {
                            row = $('<tr></tr>');
                        }

                        if (type == rowType.URL) {

                            var image = $('<img>');
                            image.attr('src', icon.imgUrl);
                            image.attr('title', icon.title);

                            var td = $('<td></td>');
                            td.append(image);
                            row.append(td);

                        } if (type == rowType.SCOREBAR) {

                            var scoreBarContainer = $('<div></div>');

                            var score = Math.round(icon.score * 100) + '%';

                            scoreBarContainer.attr('title', score);
                            scoreBarContainer.css({
                                'position': 'relative',
                                'display': 'inline-block',
                                'height': _renderDefaults.SCORE_BAR_HEIGHT + 1
                            });


                            var scoreBarBackground = $('<div></div>');
                            scoreBarContainer.append(scoreBarBackground);
                            scoreBarBackground.addClass('scoreBarBackground');
                            scoreBarBackground.width(_renderDefaults.SCORE_BAR_WIDTH);
                            scoreBarBackground.height(_renderDefaults.SCORE_BAR_HEIGHT);

                            if (icon.score > 0) {
                                var scoreBarForeground = $('<div></div>');
                                scoreBarContainer.append(scoreBarForeground);
                                scoreBarForeground.addClass('scoreBarForeground');
                                scoreBarForeground.width((_renderDefaults.SCORE_BAR_WIDTH -1) * icon.score);
                                scoreBarForeground.height(_renderDefaults.SCORE_BAR_HEIGHT);
                            }

                            var td = $('<td></td>');
                            td.append(scoreBarContainer);
                            row.append(td);

                        } if (type == rowType.SCORE) {

                            var score = Math.round(icon.score * 100) + '%';
                            var td = $('<td></td>');
                            td.text(score);
                            row.append(td);
                        }
                    }
                }
            }

            if (row != null) {
                detailsTable.append(row);
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
	                subTokens[chan.SELECTION_CHANGE_EVENT] = aperture.pubsub.subscribe(chan.SELECTION_CHANGE_EVENT, _onSelect);
	                subTokens[chan.SEARCH_REQUEST] = aperture.pubsub.subscribe(chan.SEARCH_REQUEST, _onSearch);
                    subTokens[chan.OBJECT_FROM_XFID] = aperture.pubsub.subscribe(chan.OBJECT_FROM_XFID, _createClusterDetails);
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