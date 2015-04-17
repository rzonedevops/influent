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
		'lib/toolbar/infToolbarOptionPicker',
		'lib/extern/OpenAjaxUnmanagedHub'
	],
	function(
		appChannel,
		constants,
		infToolbarOptionPicker
	) {

		describe('infToolbarOptionPicker adds an option selection widget to a view toolbar', function() {

			var widgetInst = null;
			var viewName = 'TEST_VIEW';
			var canvas = $('<div><div class="row"></div></div>');

			it('Has testing enabled', function () {
				expect(constants.UNIT_TESTS_ENABLED).toBeTruthy();
			});

			it('Returns a module', function() {
				expect(infToolbarOptionPicker).toBeDefined();
			});

			it('Creates and returns an instance', function() {
				widgetInst = infToolbarOptionPicker.createInstance(viewName, canvas, {
					elements: ['Test1', 'Test2', 'Test3'],
					selection: 'Test1'
				});
				expect(widgetInst).toBeDefined();
			});


			it ('Renders to a div', function() {
				widgetInst.render();
			});

			it('Responds to picker input and notifies subscribers', function() {

				// Todo - move this out to individual widget tests?
				var selectChangeCallback = jasmine.createSpy();

				var subTokens = {};
				subTokens[appChannel.FILTER_OPTION_PICKER_CHANGE_EVENT] = aperture.pubsub.subscribe(appChannel.FILTER_OPTION_PICKER_CHANGE_EVENT, selectChangeCallback);

				aperture.pubsub.publish(appChannel.FILTER_OPTION_PICKER_CHANGE_REQUEST, {
					view: viewName,
					selection: 'Test2'
				});

				expect(selectChangeCallback).toHaveBeenCalledWith(appChannel.FILTER_OPTION_PICKER_CHANGE_EVENT,	{
					view: viewName,
					selection: 'Test2'
				}, undefined, undefined);
			});
		});
	}
);