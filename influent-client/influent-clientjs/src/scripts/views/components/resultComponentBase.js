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

define(
	[
		'lib/constants',
		'lib/plugins',
		'lib/util/infTagUtilities',
		'hbs!templates/searchResults/searchDetails',
		'hbs!templates/searchResults/searchResult'
	],
	function(
		constants,
		plugins,
		tagUtil,
		searchDetails,
		searchResultTemplate
	) {
		var _add = function(xfId, container, headerInfo, result, snippet, selectionChangeChannel, entityDetailsChangeChannel) {
			var state = {
				canvas : null,
				summaryCanvas : null,
				detailsCanvas : null,
				xfId : xfId,
				snippet : snippet,
				result : result,
				resultData : result.getVisualInfo(headerInfo),
				selectionChangeChannel : selectionChangeChannel,
				entityDetailsChangeChannel : entityDetailsChangeChannel
			};

			var template = searchResultTemplate({
				xfId : state.xfId,
				columns : state.resultData.columns,
				detailsSpan : state.resultData.detailsSpan,
				promptForDetails : state.resultData.promptForDetails
			});

			container.append(template);

			state.canvas = container;
			state.summaryCanvas = container.find('#summary_' + state.xfId);
			state.detailsCanvas = container.find('#details_' + state.xfId);

			_addSelectionClickHandler(state);

			_updateDetailsCanvas(state);

			return state;
		};

		//--------------------------------------------------------------------------------------------------------------

		var _addDetailsClickHandler = function(state) {
			state.detailsCanvas.find('#infSearchResultShowDetails').click(function() {
				//signal event for a change in detail level
				aperture.pubsub.publish(state.entityDetailsChangeChannel, {
					xfId : state.xfId,
					container : state.detailsCanvas
				});
			});
		};

		//--------------------------------------------------------------------------------------------------------------

		var _showMatchDetails = function($details, state) {
			var template = searchDetails({properties: state.snippet});
			
			$details.append(template);
			
			_addDetailsClickHandler(state);
		};
		
		//--------------------------------------------------------------------------------------------------------------

		var _promptForDetailsCallback = function($details) {
			var resultDetails = $details.find('.resultDetails');
			resultDetails.find('.infSearchResultOverlay').remove();

			_showMatchDetails(resultDetails, this);

			this.result.setDetailsPrompt(false);
		};

		//--------------------------------------------------------------------------------------------------------------

		var _updateDetailsCanvas = function(state) {

			var $details = state.detailsCanvas.find('.resultDetails');

			var prompting = false;
			if (state.resultData.promptForDetails) {

				var extensions = plugins.get('details');
				var plugin = aperture.util.find(
					extensions,
					function (e) {
						return e.searchResultPrompt !== undefined;
					}
				);

				if (plugin) {
					plugin.searchResultPrompt(state.detailsCanvas, state.result, _promptForDetailsCallback.bind(state));
					prompting = true;
				}
			}

			if (!prompting) {
				_showMatchDetails($details, state);
			}
		};

		//--------------------------------------------------------------------------------------------------------------

		var _addSelectionClickHandler = function(state) {
			state.summaryCanvas.find('.selectSingleResult').click(function() {
				var $checkbox = $(this).find('input');
				var checked = $checkbox.prop('checked');

				_highlightResult($(this), checked);

				//signal event for selected id
				aperture.pubsub.publish(state.selectionChangeChannel, {
					xfId : state.xfId,
					isSelected : checked
				});
			});
		};

		//--------------------------------------------------------------------------------------------------------------

		var _highlightResult = function(container, highlight) {
			if (highlight) {
				container.addClass('searchResultSelected');
			} else {
				container.removeClass('searchResultSelected');
			}
		};

		//--------------------------------------------------------------------------------------------------------------

		return {
			addSearchResult: _add
		};
	}
);