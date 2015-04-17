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
		'lib/constants',
		'lib/toolbar/infToolbarDatePicker',
		'lib/toolbar/infToolbarOptionPicker',
		'lib/toolbar/infToolbarPatternSearch',
		'lib/toolbar/infToolbarSearch',
		'lib/util/duration',
		'lib/util/xfUtil',
		'hbs!templates/viewToolbar/infToolbar'
	],
	function(
		appChannel,
		constants,
		infToolbarDatePicker,
		infToolbarOptionPicker,
		infToolbarPatternSearch,
		infToolbarSearch,
		duration,
		xfUtil,
		infToolbarTemplate
	) {
		// All widget types are defined here
		var _widgetTypes = {
			DATE_PICKER: function(dates)	{ return {
				module: infToolbarDatePicker,
				spec: dates
			};},
			OPTION_PICKER: function(elements, selection) { return {
				module: infToolbarOptionPicker,
				spec: {
					elements: elements,
					selection: selection
				}
			};},
			SEARCH: function(searchParams) { return {
				module: infToolbarSearch,
				spec : {
					searchParams : searchParams
				}
			};},
			PATTERN_SEARCH: function() { return {
				module: infToolbarPatternSearch
			};}
		};

		//--------------------------------------------------------------------------------------------------------------

		var _moduleSpecTemplate = {
			widgets: []
		};

		var module = {};
		$.extend(module, _widgetTypes);

		module.createInstance = function (view, canvas, spec) {

			var instance = {};

			var _UIObjectState = {
				view: null,
				canvas: null,
				subscriberTokens: null,
				spec: null,
				widgetInstances: []
			};

			_UIObjectState.view = view;
			_UIObjectState.canvas = canvas;

			// populate state spec with passed in spec
			_UIObjectState.spec = this.getSpecTemplate();
			for (var key in spec) {
				if (spec.hasOwnProperty(key)) {
					_UIObjectState.spec[key] = spec[key];
				}
			}

			//----------------------------------------------------------------------------------------------------------
			// Private
			//----------------------------------------------------------------------------------------------------------

			//----------------------------------------------------------------------------------------------------------
			// Public
			//----------------------------------------------------------------------------------------------------------

			instance.render = function () {

				if (!_UIObjectState.canvas || !_UIObjectState.canvas instanceof $) {
					throw 'No assigned canvas for rendering view toolbar';
				}

				_UIObjectState.canvas.empty();

				// Plug everything into the template
				_UIObjectState.canvas.append(infToolbarTemplate());

				aperture.util.forEach(_UIObjectState.widgetInstances, function(ele) {
					ele.render();
				});
			};

			//--------------------------------------------------------------------------------------------------------------

			// Add all the widgets to the bar and render it
			aperture.util.forEach(_UIObjectState.spec.widgets, function(ele) {
				if (!ele.module) {
					throw 'Invalid view toolbar module';
				}

				var inst = ele.module.createInstance(_UIObjectState.view, _UIObjectState.canvas, ele.spec);
				_UIObjectState.widgetInstances.push(inst);
			});
			instance.render();


			// UNIT TESTING --------------------------------------------------------------------------------------------

			if (constants.UNIT_TESTS_ENABLED) {
				instance._UIObjectState = _UIObjectState;
			}

			// UNIT TESTING --------------------------------------------------------------------------------------------

			return instance;
		};

		module.getSpecTemplate = function () {

			var specTemplate = {};
			$.extend(true, specTemplate, _moduleSpecTemplate);

			return specTemplate;
		};

		return module;
	}
);