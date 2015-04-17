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
		'lib/communication/applicationChannels',
		'lib/communication/flowViewChannels',
		'hbs!templates/viewToolbar/infToolbarPatternSearch'
	],
	function(
		appChannel,
		flowChannel,
		infToolbarPatternSearchTemplate
	) {
		var module = {};

		module.createInstance = function (view, canvas) {

			var instance = {};

			var _UIObjectState = {
				view: view,
				canvas: canvas,
				widgetCanvas: null,
				subscriberTokens: null
			};

			//----------------------------------------------------------------------------------------------------------
			// Private Methods
			//----------------------------------------------------------------------------------------------------------

			var _$ = function(e) {
				return _UIObjectState.widgetCanvas.find(e);
			};

			//----------------------------------------------------------------------------------------------------------

			var _initializeWidget = function() {
				_$('.dropdown-menu a').click(function() {

					aperture.pubsub.publish(flowChannel.PATTERN_SEARCH_REQUEST);
					_$('.dropdown-toggle').dropdown('toggle');
					return false;
				}).mouseenter(
					function(e) {
						aperture.pubsub.publish(flowChannel.HIGHLIGHT_PATTERN_SEARCH_ARGUMENTS, {
							xfId : null,
							isHighlighted : true
						});
						aperture.pubsub.publish(appChannel.HOVER_START_EVENT, { element : e.target.title });
						aperture.pubsub.publish(appChannel.TOOLTIP_START_EVENT, { element : e.target.title });
						return false;
					}
				).mouseleave(
					function(e) {
						aperture.pubsub.publish(flowChannel.HIGHLIGHT_PATTERN_SEARCH_ARGUMENTS, {
							xfId : null,
							isHighlighted : false
						});
						aperture.pubsub.publish(appChannel.HOVER_END_EVENT, { element : e.target.title });
						aperture.pubsub.publish(appChannel.TOOLTIP_END_EVENT, { element : e.target.title });
						return false;
					}
				);
			};

			//----------------------------------------------------------------------------------------------------------
			// Public
			//----------------------------------------------------------------------------------------------------------

			instance.render = function () {

				if (!_UIObjectState.canvas || !_UIObjectState.canvas instanceof $) {
					throw 'No assigned canvas for rendering view toolbar widget';
				}

				var row = _UIObjectState.canvas.find('.row');

				// Plug everything into the template
				_UIObjectState.widgetCanvas = $(infToolbarPatternSearchTemplate({
					id: 'patterns-like-these',
					tooltip: 'Search for similar patterns of activity',
					icon: 'icon-pattern-search',
					text: 'Find Patterns Like This'
				})).appendTo(row);

				_initializeWidget();
			};

			// TODO: Do we need to listen to incoming changes?
			var subTokens = {};
			_UIObjectState.subscriberTokens = subTokens;
			// TODO: .. If so, we need to unsubscribe too.

			return instance;
		};

		return module;
	}
);