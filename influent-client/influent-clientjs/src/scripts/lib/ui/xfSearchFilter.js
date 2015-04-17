/*
 * Copyright (C) 2013-2015 Uncharted Software Inc.
 *
 * Property of Uncharted(TM), formerly Oculus Info Inc.
 * http://uncharted.software/
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

define(['lib/communication/applicationChannels'],
	function(appChannel) {

		var _UIObjectState = {
			showing : false,
			previousState : null,
			previousFilters : null,
			element : null
		};

		//--------------------------------------------------------------------------------------------------------------

		var _onSearchFilterChange = function(e) {
			var currentState = _getState();
			_UIObjectState.previousState = currentState;

			aperture.pubsub.publish(appChannel.SEARCH_FILTER_CHANGED, currentState);
		};

		//--------------------------------------------------------------------------------------------------------------

		var _getState = function() {
			var selectElements = _UIObjectState.element.find('select');
			var state = {};
			$.each(selectElements, function(idx,selectElem) {
				var select = $(selectElem);
				var key = select.attr('key');
				var value = $(this).find(':selected').attr('influentAnyOption') === 'true' ? '*' : $(this).val();
				state[key] = value;
			});
			return state;
		};

		//--------------------------------------------------------------------------------------------------------------

		var _setState = function(state) {
			$(_UIObjectState.element).find('select').each(function(index,selectElement) {
				var selectKey = $(selectElement).attr('key');
				var storedValue = state[selectKey];

				if (!storedValue){
					return;
				}

				var options = $(selectElement).find('option');
				for (var i = 0; i < options.length; i++) {
					var optionVal = $(options[i]).val();
					if (optionVal === storedValue) {
						$(options[i]).prop('selected',true);
						break;
					}
				}
			});
		};

		//--------------------------------------------------------------------------------------------------------------

		var searchFilter = {
			create : function(searchResponse) {

				var buckets;
				if (_UIObjectState.previousFilters) {
					buckets = _UIObjectState.previousFilters;
				} else {
					buckets = searchResponse.searchFilters;
					_UIObjectState.previousFilters = searchResponse.searchFilters;
				}

				var tools = $('.infSearchTools');
				if (tools.length) {
					tools.empty();
				} else {
					tools = $('<div/>').addClass('infSearchTools');
				}
				if (!_UIObjectState.showing) {
					tools.css('display','none');
				}
				_UIObjectState.element = tools;
				for (var key in buckets) {
					if (buckets.hasOwnProperty(key)) {
						var propertyValues = buckets[key];
						var select = $('<select/>').attr('key',key);

						var blankOption = $('<option>Any</option>').attr('influentAnyOption',true).attr('value','*');
						select.append(blankOption);

						for (var i = 0; i < propertyValues.values.length; i++) {
							$('<option value="' + propertyValues.values[i] + '">' + propertyValues.values[i] + '</option>').appendTo(select);
						}

						tools.append(propertyValues.friendlyText + ':').append(select);
					}
				}
				var clearBtn = $('<button/>').html('Clear').addClass('simpleViewFileCreateButton').button().click(function() {
					var selectElements = $(_UIObjectState.element).find('select');
					selectElements.unbind('change');
					$.each(selectElements, function(index,element) {
						$(element).find('option[influentAnyOption=true]').prop('selected',true);
					});
					searchFilter.clearState();
					_onSearchFilterChange();
				});
				tools.append(clearBtn);

				// apply previous state
				if (_UIObjectState.previousState != null) {
					_setState(_UIObjectState.previousState);
				}

				tools.find('select').change(_onSearchFilterChange);

				return tools;
			},
			clearState : function() {
				_UIObjectState.previousState = null;
				_UIObjectState.previousFilters = null;
			},
			setShowing : function(bShow) {
				_UIObjectState.showing = bShow;
			},
			getShowing : function() {
				return _UIObjectState.showing;
			}
		};
		return searchFilter;
	}
);
