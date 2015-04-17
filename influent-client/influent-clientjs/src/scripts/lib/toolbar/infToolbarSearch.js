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
		'lib/advanced-search/infToolbarAdvancedSearch',
		'lib/communication/applicationChannels',
		'hbs!templates/viewToolbar/infToolbarSearch'
	],
	function(
		infToolbarAdvancedSearch,
		appChannel,
		infToolbarSearchTemplate
	) {
		var module = {};

		module.createInstance = function (view, canvas, spec) {

			var instance = {};
			
			var _UIObjectState = {
				view: view,
				canvas: canvas,
				widgetCanvas: null,
				subscriberTokens: null,
				advancedModule: null
			};

			_UIObjectState.advancedModule = infToolbarAdvancedSearch.createInstance(view, canvas, spec.searchParams);

			//--------------------------------------------------------------------------------------------------------------
			// Private Methods
			//--------------------------------------------------------------------------------------------------------------

			var _$ = function(e) {
				return _UIObjectState.widgetCanvas.find(e);
			};

			//--------------------------------------------------------------------------------------------------------------

			var _sendFilterChange = function() {
				aperture.pubsub.publish(appChannel.FILTER_SEARCH_CHANGE_EVENT, {
					view: _UIObjectState.view,
					input: _$('input').val()
				});
			};

			var _showAdvancedSearch = function() {
				aperture.pubsub.publish(appChannel.ADVANCED_SEARCH_REQUEST, {
					view: _UIObjectState.view,
					input: _$('input').val()
				});
			};
			
			//--------------------------------------------------------------------------------------------------------------

			var _onFilterChangeRequest = function(eventChannel, data) {
				_$('input').val(data.input);
				if (eventChannel !== appChannel.FILTER_SEARCH_CHANGE_REQUEST ||
					data.view !== _UIObjectState.view) {
					return;
				}

				_$('input').val(data.input);
				_sendFilterChange();
			};

			var _onFilterDisplayChangeRequest = function(eventChannel, data) {
				if (eventChannel !== appChannel.FILTER_SEARCH_DISPLAY_CHANGE_REQUEST ||
					data.view !== _UIObjectState.view) {
					return;
				}
				_$('input').val(data.input);
			};

			//--------------------------------------------------------------------------------------------------------------

			var _initializeFilter = function () {

				_$('input').keypress(function(event) {
					if ( event.which === 13 ) {
						event.preventDefault();
						
						_sendFilterChange();
					}
				});

				_$('.infGoSearch').click(function() {
					_sendFilterChange();
				});

				_$('.infAdvSearchShow').click(function() {
					_showAdvancedSearch();
				});

			};

			//--------------------------------------------------------------------------------------------------------------
			// Public
			//--------------------------------------------------------------------------------------------------------------

			instance.render = function () {

				if (!_UIObjectState.canvas || !_UIObjectState.canvas instanceof $) {
					throw 'No assigned canvas for rendering view toolbar widget';
				}

				var row = _UIObjectState.canvas.find('.row');

				// Plug everything into the template
				_UIObjectState.widgetCanvas = $(infToolbarSearchTemplate()).appendTo(row);

				_initializeFilter();

				// propagate
				_UIObjectState.advancedModule.render();
			};

			// TODO: Do we need to listen to incoming changes?
			var subTokens = {};
			subTokens[appChannel.FILTER_SEARCH_CHANGE_REQUEST] = aperture.pubsub.subscribe(appChannel.FILTER_SEARCH_CHANGE_REQUEST, _onFilterChangeRequest);
			subTokens[appChannel.FILTER_SEARCH_DISPLAY_CHANGE_REQUEST] = aperture.pubsub.subscribe(appChannel.FILTER_SEARCH_DISPLAY_CHANGE_REQUEST, _onFilterDisplayChangeRequest);
			_UIObjectState.subscriberTokens = subTokens;

			// TODO: .. If so, we need to unsubscribe too.

			return instance;
		};

		return module;
	}
);