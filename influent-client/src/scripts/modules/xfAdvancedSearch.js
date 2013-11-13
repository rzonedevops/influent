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
define(['jquery', 'lib/module', 'lib/channels'],
    function($, modules, chan) {

        //--------------------------------------------------------------------------------------------------------------
        // Private Variables
        //--------------------------------------------------------------------------------------------------------------

        var MODULE_NAME = 'xfAdvancedSearch';

        var _UIObjectState = {
            UIType : MODULE_NAME,
            childModule : undefined,
            properties : {},
            parentId : '',
            subscriberTokens : null
        };

        //--------------------------------------------------------------------------------------------------------------
        // Private Methods
        //--------------------------------------------------------------------------------------------------------------

        var _resolveFuzzy = function (fuzzyDiv) {

            if (fuzzyDiv == null) {
                return;
            }

            var that = $(fuzzyDiv);
            var id = that.attr('id').charAt(that.attr('id').length-1);

            if (that.is(':checked')) {
                $('#not'+id)
                    .attr('checked', false)
                    .attr('disabled','disabled');
            }
            else {
                $('#not'+id).removeAttr('disabled');
            }
        };

        //--------------------------------------------------------------------------------------------------------------

        var _onAdvancedSearchDialogRequest = function(eventChannel, data) {
            _UIObjectState.parentId = data.xfId;
            $('#advancedDialog').dialog('open');
        };

        //--------------------------------------------------------------------------------------------------------------

        var _modalBoxCreator = function() {
            var currentProps = [];

            aperture.io.rest('/searchparams?queryId='+(new Date()).getTime(),
                'GET',
                function(response) {
                    var entityColumns = [];
                    if (response.data.length != 0) {

                        var maxFields = 15;

                        for (var x = 0; x < response.data.length; x++) {
                            var key = response.data[x];
                            var type = key['type'];
                            var description = key.propertyDescriptors;
                            var desc = [];

                            if (maxFields < description.length-1)
                                description.splice (maxFields, description.length-1);

                            for (var v = 0; v < description.length; v++) {
                            	var range = description[v].range;
                            	var constraints = {};
                            	
                            	// TODO may still need to iron out incompatability issues w v1.5:
                            	// This is designed to take in ALL valid constraints for a given FL_PropertyDescriptor so that the user only sees an appropriate subset of constraints for each property.
                            	// Thus, for this to work properly, one either needs to add a 'getValidConstraints' to FL_PropertyDescriptor OR pass back different object with this information in getDescriptors().
                            	// Either way, an API change will be needed to accommodate this :(
                            	
                        		constraints.EQUALS = true;
                        		constraints.FUZZY  = true;
                        		constraints.NOT    = true;
                            	constraints[range] = true;
                            	
                                desc.push({
                                	key: description[v]['key'], 
                                	suggestedTerms: description[v]['suggestedTerms'], 
                                	friendlyText: description[v]['friendlyText'],
                                	constraints: constraints
                                });
                            }

                            entityColumns.push({type: type, data: desc});
                        }
                    }

                    _initializeModalComponents(entityColumns);
                    currentProps = entityColumns[0]['data'];

                    $('#entities').change(
                        function() {
                            var propertyContainer = $('#propertyContainer');
                            var propertyLine;
                            var oldFriendlyNames = new Array();
                            var oldFieldValues = new Array();
                            var oldNotValues = new Array();
                            var oldFuzzyValues = new Array();

                            var oldInd = -1;
                            for (var i = 0; i < entityColumns[0]['data'].length; i++) {
                                if(entityColumns[i]['data'] == _UIObjectState.properties) {
                                    oldInd = i;
                                    break;
                                }
                            }

                            if(oldInd != -1) {
                            	for (var i = 0; i < entityColumns[oldInd]['data'].length; i++) {
                            		oldFriendlyNames[i] = entityColumns[oldInd]['data'][i]['friendlyText'];
                            	}
                            	
	                            propertyContainer.children().each(function() {
	                            	$(this).children().each(function() {
		                            	var idName = $(this).attr('id');
		                            	var idx = -1;
		                            	if(idName != null) {
			                                if(idName.indexOf('textProperty') == 0) {
			                                	idx = idName.substring('textProperty'.length);
			                                	oldFieldValues[idx] = $(this).val();
			                                }
			                                else if(idName.indexOf('not') == 0) {
			                                	idx = idName.substring('not'.length);
			                                	oldNotValues[idx] = $(this).attr('checked');
			                                }
			                                else if(idName.indexOf('fuzzy') == 0) {
			                                	idx = idName.substring('fuzzy'.length);
			                                	oldFuzzyValues[idx] = $(this).attr('checked');
			                                }
		                            	}
	                            	});
	                            });
                            }

                            propertyContainer.empty();
                            var col = $(this).val();

                            var ind = 0;
                            for (var x = 0; x < entityColumns.length; x++) {
                                if (entityColumns[x]['type'] == col) {
                                    ind = x;
                                    break;
                                }
                            }

                            for (var i = 0; i < entityColumns[ind]['data'].length; i++) {
                            	propertyLine = $('<div/>');
                                var inputType = entityColumns[ind]['data'][i]['suggestedTerms'].length == 0 ?  'input' : 'select';

                                var element = $('<label/>');
                                element.attr('for', ('textProperty' + i.toString()));
                                element.css('width', '30%');
                                element.css('float', 'left');
                                element.css('text-align', 'right');
                                element.html(entityColumns[ind]['data'][i]['friendlyText'] + ':&nbsp;&nbsp;');
                                propertyLine.append(element);

                                element = $('<' + inputType + '/>');
                                element.attr('id', ('textProperty' + i.toString()));
                                element.addClass('textPropertyClass');
                                element.css('width', 140);
                                
                                var oldFriendlyNameIdx = jQuery.inArray(entityColumns[ind]['data'][i]['friendlyText'], oldFriendlyNames);
                                if(oldFriendlyNameIdx != -1) {
                                	element.val(oldFieldValues[oldFriendlyNameIdx]);
                                }

                                if (inputType == 'select') {
                                    var option = $('<option/>');
                                    element.append(option);
                                    for (var j = 0; j < entityColumns[ind]['data'][i]['suggestedTerms'].length; j++) {
                                        option = $('<option/>');
                                        option.html(entityColumns[ind]['data'][i]['suggestedTerms'][j]);
                                        element.append(option);
                                    }
                                }

                                propertyLine.append(element);

                                
                                element = $('<label/>');
                                element.attr('for', ('not' + i.toString()));
                                element.html('  &nbsp;&nbsp;Not?: ');
                                propertyLine.append(element);
                                
                                element = $('<input/>');
                                element.attr('id', ('not' + i.toString()));
                                element.attr('type', 'checkbox');
                                element.css('position', 'relative');
                                element.css('top', 3);
                                if(oldFriendlyNameIdx != -1) {
                                	if(oldFuzzyValues[oldFriendlyNameIdx] == 1) {
                                		element.attr('disabled', 'disabled');	
                                	}
                                	else {
                                		element.attr('checked', oldNotValues[oldFriendlyNameIdx]);
                                	}
                                }
                                propertyLine.append(element);

                                element = $('<label/>');
                                element.attr('for', ('fuzzy' + i.toString()));
                                element.html('  &nbsp;&nbsp;Fuzzy?: ');
                                propertyLine.append(element);

                                element = $('<input/>');
                                element.attr('id', ('fuzzy' + i.toString()));
                                element.attr('type', 'checkbox');
                                element.css('position', 'relative');
                                element.css('top', 3);
                                if(oldFriendlyNameIdx != -1) {
                                	element.attr('checked', oldFuzzyValues[oldFriendlyNameIdx]);
                                }
                                propertyLine.append(element);
                                element.change(
                                    function() {
                                        _resolveFuzzy(this);
                                    }
                                );
                                
                                propertyContainer.append(propertyLine);
                            }

                            currentProps = entityColumns[ind]['data'];
                            propertyContainer.append('<br><br>');

                            _UIObjectState.properties = currentProps;
                            _constructDialog(_UIObjectState);
                        }
                    );

                    _UIObjectState.properties = currentProps;
                    _constructDialog(_UIObjectState);
                },
                {}
            );
        };

        //--------------------------------------------------------------------------------------------------------------

        var _initializeModalComponents = function (entityCols) {
            var first = entityCols[0]['data'];
            var i;

            var dialog = $('#advancedDialog');
            dialog.css('white-space', 'nowrap');
            dialog.append('<br>');

            var element = $('<label/>');
            element.attr('for', 'entities');
            element.html('Type: ');
            dialog.append(element);

            element = $('<select/>');
            element.attr('id', 'entities');

            for (i = 0; i < entityCols.length; i++) {
                var option = $('<option/>');
                option.attr('value', entityCols[i]['type']);
                option.html(entityCols[i]['type']);
                element.append(option);
            }

            dialog.append(element);
            dialog.append('<br>');

            dialog.append('<br>');
            dialog.append('<br>');

            var propertyContainer = $('<div/>');
            var propertyLine;
            propertyContainer.attr('id', 'propertyContainer');

            for (i = 0; i < first.length; i++) {
            	propertyLine = $('<div/>');
            	propertyLine.attr('id', 'propertyLine-' + first[i]['friendlyText']);
            	
                var inputType = first[i]['suggestedTerms'].length == 0 ?  'input' : 'select';

                element = $('<label/>');
                element.attr('for', ('textProperty' + i.toString()));
                element.css('width', '30%');
                element.css('float', 'left');
                element.css('text-align', 'right');
                element.html(first[i]['friendlyText'] + ':&nbsp;&nbsp;');
                propertyLine.append(element);

                element = $('<' + inputType + '/>');
                element.attr('id', ('textProperty' + i.toString()));
                element.addClass('textPropertyClass');
                element.css('width', 140);

                if (inputType == 'select') {
                    for (var j = 0; j < first[i]['suggestedTerms'].length; j++) {
                        option = $('<option/>');
                        option.html(first[i]['suggestedTerms'][j]);
                        element.append(option);
                    }
                }

                propertyLine.append(element);
                
                
                element = $('<label/>');
                element.attr('for', ('not' + i.toString()));
                element.html('  &nbsp;&nbsp;Not?: ');
                propertyLine.append(element);
                
                if (!first[i].constraints.NOT) {
                	element.css('display', 'none');
                }
                
                element = $('<input/>');
                element.attr('id', ('not' + i.toString()));
                element.attr('type', 'checkbox');
                element.css('position', 'relative');
                element.css('top', 3);
                propertyLine.append(element);

                if (!first[i].constraints.NOT) {
                	element.css('display', 'none');
                }
                
                
                element = $('<label/>');
                element.attr('for', ('fuzzy' + i.toString()));
                element.html('  &nbsp;&nbsp;Fuzzy?: ');
                propertyLine.append(element);

                if (!first[i].constraints.EQUALS || !first[i].constraints.FUZZY) {
                	element.css('display', 'none');
                }
                
                element = $('<input/>');
                element.attr('id', ('fuzzy' + i.toString()));
                element.attr('type', 'checkbox');
                element.css('position', 'relative');
                element.css('top', 3);
                propertyLine.append(element);
                
                
                if (!first[i].constraints.EQUALS || !first[i].constraints.FUZZY) {
                	element.css('display', 'none');
                }
                
                element.change(
                    function() {
                        _resolveFuzzy(this);
                    }
                );
                
                propertyContainer.append(propertyLine);
            }

            $('#propertyContainer').append('<br><br>');
            dialog.append(propertyContainer);

            return entityCols[0]['type'];
        };

        //--------------------------------------------------------------------------------------------------------------

        var _constructDialog = function(state){
            $('#advancedDialog').dialog({
                autoOpen: false,
                modal: true,
                buttons: {
                    'Apply': function () {
                        var searchString = "";

                        for (var x = 0; x < state.properties.length; x++) {
                            if ($('#textProperty'+x).val() != "") {

                                var val = $('#textProperty'+x).val();
                                if ($('#not'+x).is(':checked')) {
                                    val = '-' + val;
                                } else if (!$('#fuzzy'+x).is(':checked')) {
                                    val = '"' + val + '"';
                                }

                                searchString += state.properties[x]['key'] + ':' + val + ' ';
                            }
                        }

                        searchString += 'datatype:"'+ $('#entities').val()+ '"';
                        
                        var searchData = {
                            xfId : _UIObjectState.parentId,
                            searchTerm : searchString,
                            noRender : true
                        };

                        aperture.pubsub.publish(chan.SEARCH_REQUEST, searchData);

                        $(this).dialog('close');
                    },
                    'Cancel': function() {
                        $(this).dialog('close');
                    }
                },
                show: {
                    effect: 'clip',
                    duration: 100
                },
                hide: {
                    effect: 'clip',
                    duration: 100
                },
                width:500
            });
        };

        //--------------------------------------------------------------------------------------------------------------
        // Public
        //--------------------------------------------------------------------------------------------------------------

        var xfAdvancedSearchModule = {};

        //--------------------------------------------------------------------------------------------------------------

        xfAdvancedSearchModule.registerChildModule = function(childModule) {
            _UIObjectState.childModule = childModule;
        };

        //--------------------------------------------------------------------------------------------------------------

        // Register the module with the system
        modules.register('xfAdvancedSearch', function() {
        	return {
	        	start : function() {
	                var subTokens = {};
	                subTokens[chan.ADVANCE_SEARCH_DIALOG_REQUEST] = aperture.pubsub.subscribe(chan.ADVANCE_SEARCH_DIALOG_REQUEST, _onAdvancedSearchDialogRequest);
	                subTokens[chan.ALL_MODULES_STARTED] = aperture.pubsub.subscribe(chan.ALL_MODULES_STARTED, _modalBoxCreator);
	                _UIObjectState.subscriberTokens = subTokens;
	            },
	            end : function(){
	                for (token in _UIObjectState.subscriberTokens) {
	                    aperture.pubsub.unsubscribe(_UIObjectState.subscriberTokens[token]);
	                }
	            }
        	};
        });

        return xfAdvancedSearchModule;
    }
);