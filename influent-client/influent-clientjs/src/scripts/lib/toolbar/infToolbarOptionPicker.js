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
		'hbs!templates/viewToolbar/infToolbarOptionPicker'
	],
	function(
		appChannel,
		infToolbarOptionPickerTemplate
	) {
		var module = {};

		module.createInstance = function (view, canvas, spec) {

			var instance = {};

			var _UIObjectState = {
				view: null,
				canvas: null,
				widgetCanvas: null,
				subscriberTokens: null,
				elements: spec.elements,
				selection: spec.selection
			};

			var _$ = function(e) {
				return _UIObjectState.widgetCanvas.find(e);
			};

			_UIObjectState.view = view;
			_UIObjectState.canvas = canvas;

			//--------------------------------------------------------------------------------------------------------------
			// Private Methods
			//--------------------------------------------------------------------------------------------------------------

			var _updateFilterUI = function() {
				_$('button').html(_UIObjectState.selection + ' <span class="caret"></span>');
			};

			//--------------------------------------------------------------------------------------------------------------

			var _sendFilterChange = function() {
				aperture.pubsub.publish(appChannel.FILTER_OPTION_PICKER_CHANGE_EVENT, {
					view: _UIObjectState.view,
					selection: _UIObjectState.selection
				});
			};

			//--------------------------------------------------------------------------------------------------------------

			var _initializeFilter = function () {

				_$('.dropdown-menu a').click(function() {

					if (_UIObjectState.selection !== $(this).text()) {
						_UIObjectState.selection = $(this).text();
						_sendFilterChange();
						_updateFilterUI();
					}
					_$('.dropdown-toggle').dropdown('toggle');
					return false;
				});
			};

			//--------------------------------------------------------------------------------------------------------------

			var _onFilterChangeRequest = function(eventChannel, data) {
				if (eventChannel !== appChannel.FILTER_OPTION_PICKER_CHANGE_REQUEST ||
					data.view !== _UIObjectState.view) {
					return;
				}

				_UIObjectState.selection = data.selection;
				_sendFilterChange();
				_updateFilterUI();
			};


			//--------------------------------------------------------------------------------------------------------------
			// Public
			//--------------------------------------------------------------------------------------------------------------

			instance.render = function () {

				if (!_UIObjectState.canvas || !_UIObjectState.canvas instanceof $) {
					throw 'No assigned canvas for rendering view toolbar widget';
				}

				if (_UIObjectState.elements.length === 0) {
					throw 'Selector filter widget has no elements';
				}

				var row = _UIObjectState.canvas.find('.row');

				var data = {
					entries: []
				};

				aperture.util.forEach(_UIObjectState.elements, function(ele) {
					data.entries.push({text: ele});
				});

				// Plug everything into the template
				_UIObjectState.widgetCanvas = $(infToolbarOptionPickerTemplate(data)).appendTo(row);

				_updateFilterUI();
				_initializeFilter();
			};

			// TODO: Do we need to listen to incoming changes?
			var subTokens = {};
			subTokens[appChannel.FILTER_OPTION_PICKER_CHANGE_REQUEST] = aperture.pubsub.subscribe(appChannel.FILTER_OPTION_PICKER_CHANGE_REQUEST, _onFilterChangeRequest);
			_UIObjectState.subscriberTokens = subTokens;

			// TODO: .. If so, we need to unsubscribe too.

			return instance;
		};

		return module;
	}
);