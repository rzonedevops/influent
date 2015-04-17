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

define(['modules/infHeader', 'lib/constants'],
	function(infHeader, constants) {

		var state = infHeader._UIObjectState;

		var getMenuFromID = function(id) {
			return aperture.util.find(state.data.menus, function(ele) {
				return ele.id === id;
			});
		};

		var getChildMenuFromID = function(parent, id) {
			return aperture.util.find(parent.children, function(ele) {
				return ele.id === id;
			});
		};

		describe('infHeader adds a headerbar at the top of the application page', function() {
			it('Has testing enabled', function() {
				expect(constants.UNIT_TESTS_ENABLED).toBeTruthy();
			});

			it('Returns a module', function() {
				expect(infHeader).toBeDefined();
			});

			it('Constructs menus from a default spec', function() {
				infHeader._constructMenus();
				expect(state.data.menus.length).toBeGreaterThan(0);

				// Check to see if file menu has been created
				var fileMenu = getMenuFromID('file-menu');
				expect(fileMenu).toBeDefined();

				// Check to see if file menu has a named child
				var exportChart = getChildMenuFromID(fileMenu, 'export-chart');
				expect(exportChart).toBeDefined();
			});

			it('Enables/Disables certain menu items via the spec', function() {
				// Check a flow-view only item in a view other than flow view
				infHeader.setSpec([]);
				infHeader._constructMenus();

				var fileMenu = getMenuFromID('file-menu');
				var exportChart = getChildMenuFromID(fileMenu, 'export-chart');

				// Check to see if export chart is disabled.
				expect(exportChart.disabled).toBe(true);

				// Go to flow view
				infHeader.setSpec([infHeader.FLOW_VIEW]);
				infHeader._constructMenus();

				fileMenu = getMenuFromID('file-menu');
				exportChart = getChildMenuFromID(fileMenu, 'export-chart');

				// Check that export chart is now available
				expect(exportChart.disabled).toBe(false);
			});

			it('Shows help menu items if a help URL is defined', function() {
				var helpURL = aperture.config.get()['influent.config']['help'];

				var helpMenu = getMenuFromID('help-menu');
				var userGuide = getChildMenuFromID(helpMenu, 'user-guide');

				if (helpURL) {
					expect(userGuide).toBeDefined();
				} else {
					expect(userGuide).toBeUndefined();
				}
			});

			it('Shows logout menu item if useAuth is true', function() {
				var useAuth = aperture.config.get()['influent.config']['useAuth'];

				var fileMenu = getMenuFromID('file-menu');
				var logout = getChildMenuFromID(fileMenu, 'logout');

				if (useAuth) {
					expect(logout).toBeDefined();
				} else {
					expect(logout).toBeUndefined();
				}
			});
		});
	}
);