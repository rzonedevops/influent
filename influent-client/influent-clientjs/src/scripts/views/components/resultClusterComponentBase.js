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
		'hbs!templates/searchResults/searchResultCluster'
	],
	function(
		searchResultCluster
	) {


		//--------------------------------------------------------------------------------------------------------------

		var _add = function(xfId, container, clusterData, selectionChangeChannel, visibilityChangeChannel) {

			var state = {
				xfId : xfId,
				clusterLabel : clusterData.sortByLabel,
				selectionChangeChannel : selectionChangeChannel,
				visibilityChangeChannel : visibilityChangeChannel
			};

			clusterData['clusterState'] = 'up';

			var template = searchResultCluster({
				xfId : xfId,
				clusterInfo : clusterData
			});

			container.append(template);

			state.canvas = container.find('#' + state.xfId);

			_addSelectionClickHandler(state);
			_addCollapseHandler(state);

			return state.canvas;
		};

		//--------------------------------------------------------------------------------------------------------------

		var _addSelectionClickHandler = function(state) {
			state.canvas.find('.infSelectGroupResults').click(function() {
				var $checkbox = $(this).find('input');
				var checked = $checkbox.prop('checked');

				var $checkboxes = state.canvas.find('#infSearchResultTable').find('input');
				$checkboxes.prop('checked', checked);

				_highlightResult($checkboxes.parent(), checked);

				//signal event for id that changed
				aperture.pubsub.publish(state.selectionChangeChannel, {
					xfId : state.xfId,
					isSelected : checked
				});
			});
		};

		//--------------------------------------------------------------------------------------------------------------

		var _addCollapseHandler = function(state) {
			state.canvas.find('.infResultClusterAccordionButton').click(function() {
				var cluster = state.canvas.find('.batchResults');
				var icon;
				var expanded = true;
				if ($(cluster).hasClass('in')) {
					$(cluster).removeClass('in');
					icon = $(this).find('.infResultClusterAccordionButtonIcon');
					icon.removeClass('glyphicon-chevron-up');
					icon.addClass('glyphicon-chevron-down');
					expanded = false;
				} else {
					$(cluster).addClass('in');
					icon = $(this).find('.infResultClusterAccordionButtonIcon');
					icon.removeClass('glyphicon-chevron-down');
					icon.addClass('glyphicon-chevron-up');
				}

				//signal event for cluster that changed
				aperture.pubsub.publish(state.visibilityChangeChannel, {
					xfId : state.xfId,
					isExpanded : expanded
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
			addSearchResultCluster : _add
		};
	}
);