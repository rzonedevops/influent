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
		'lib/module',
		'lib/communication/applicationChannels',
		'hbs!templates/InfHeader',
		'lib/constants'
	],
	function(
		modules,
		appChannel,
		infHeaderTemplate,
		constants
	) {

		//--------------------------------------------------------------------------------------------------------------
		// Private Variables
		//--------------------------------------------------------------------------------------------------------------

		var _specOptions = {
			FLOW_VIEW : 0,
			ACCOUNTS_VIEW : 1,
			TRANSACTIONS_VIEW : 2
		};

		var _UIObjectState = {
			canvas : null,
			subscriberTokens : null,
			data: null,
			spec: []
		};

		//--------------------------------------------------------------------------------------------------------------
		// Private Functions
		//--------------------------------------------------------------------------------------------------------------

		var _init = function(sandbox) {
			_UIObjectState.canvas = $(sandbox.dom);
			_UIObjectState.canvas.empty();
		};

		//--------------------------------------------------------------------------------------------------------------

		var _hasOption = function(prop) {
			return _UIObjectState.spec && _UIObjectState.spec.indexOf(prop) !== -1;
		};

		//--------------------------------------------------------------------------------------------------------------

		var _constructMenus = function() {

			var datasource = aperture.config.get()['influent.config']['datasource'];
			_UIObjectState.data = {
				dataSource: 'Data: ' + (datasource ? datasource : 'Undefined'),
				menus: []
			};

			// - Workspace menu ----------------------------------------------------------------------------------------

			var fileMenu = {
				id: 'file-menu',
				text: 'File',
				children: []
			};
            fileMenu.children.push({
                id : 'new-workspace',
                icon: 'icon-new-chart',
                text: 'New Workspace',
                tooltip: 'open a blank workspace in a new tab',
                click: function() {
                    aperture.pubsub.publish(appChannel.NEW_WORKSPACE_REQUEST);
                }
            });
			fileMenu.children.push({
				id : 'import-chart',
				icon: 'icon-import-xml',
				text: 'Import Chart...',
				tooltip: 'import chart of filed workspace',
				disabled: !_hasOption(_specOptions.FLOW_VIEW),
				click: function() {
					aperture.pubsub.publish(appChannel.IMPORT_GRAPH_REQUEST);
				}
			});
			fileMenu.children.push({
				id : 'export-chart',
				icon: 'icon-export-xml',
				text: 'Export Chart',
				tooltip: 'export chart of filed workspace',
				disabled: !_hasOption(_specOptions.FLOW_VIEW),
				click: function() {
					aperture.pubsub.publish(appChannel.EXPORT_GRAPH_REQUEST);
				}
			});
			fileMenu.children.push({
				id : 'export-transactions',
				icon: 'icon-export-xml',
				text: 'Export Transactions',
				tooltip: 'export transactions to .csv',
				disabled: !_hasOption(_specOptions.TRANSACTIONS_VIEW),
				click: function() {
					aperture.pubsub.publish(appChannel.EXPORT_TRANSACTIONS_REQUEST);
				}
			});
			fileMenu.children.push({
				id : 'export-accounts',
				icon: 'icon-export-xml',
				text: 'Export Accounts',
				tooltip: 'export accounts to .csv',
				disabled: !_hasOption(_specOptions.ACCOUNTS_VIEW),
				click: function() {
					aperture.pubsub.publish(appChannel.EXPORT_ACCOUNTS_REQUEST);
				}
			});
			var INCLUDE_CAPTURE = aperture.config.get()['influent.config']['includeCaptureMenuItem'] !== false;
			if (INCLUDE_CAPTURE) {
				fileMenu.children.push({
					id : 'export-image',
					icon: 'icon-export-capture',
					text: 'Export Image',
					tooltip: 'export an image of the workspace',
					disabled: true,
					click: function() {
						aperture.pubsub.publish(appChannel.EXPORT_CAPTURED_IMAGE_REQUEST);
					}
				});
			}
			var useAuth = aperture.config.get()['influent.config']['useAuth'] || false;
			if (useAuth) {

				fileMenu.children.push({
					id: 'logout',
					icon: 'icon-logout',
					text: 'Logout',
					tooltip: 'logout of Influent',
					click: function() {
						aperture.pubsub.publish(appChannel.LOGOUT_REQUEST);
					}
				});
			}
			_UIObjectState.data.menus.push(fileMenu);

			// - View menu ---------------------------------------------------------------------------------------------

			var viewMenu = {
				id: 'view-menu',
				text: 'View',
				children: []
			};
			viewMenu.children.push({
				id : 'account-holders',
				icon: 'icon-display-entity',
				text: 'Account Holders',
				tooltip: 'view cards without activity charts',
				disabled: !_hasOption(_specOptions.FLOW_VIEW),
				click: function() {
					aperture.pubsub.publish(appChannel.CARD_DETAILS_CHANGE, {showDetails : false});
				}
			});
			viewMenu.children.push({
				id : 'account-activity',
				icon: 'icon-display-chart',
				text: 'Account Activity',
				tooltip: 'view cards with activity charts',
				disabled: !_hasOption(_specOptions.FLOW_VIEW),
				click: function() {
					aperture.pubsub.publish(appChannel.CARD_DETAILS_CHANGE, {showDetails : true});
				}
			});

			_UIObjectState.data.menus.push(viewMenu);

			// - Help menu ---------------------------------------------------------------------------------------------

			var helpMenu = {
				id: 'help-menu',
				text: 'Help',
				children: []
			};
			var helpURL = aperture.config.get()['influent.config']['help'];
			if (helpURL) {

				helpMenu.children.push({
					id: 'user-guide',
					icon: 'icon-info',
					text: 'User Guide',
					click: function() {
						aperture.pubsub.publish(appChannel.OPEN_EXTERNAL_LINK_REQUEST, {
							link: helpURL
						});
					}
				});
			}
			var aboutURL = aperture.config.get()['influent.config']['about'];
			if (aboutURL) {
				helpMenu.children.push({
					id: 'about',
					icon: 'icon-info',
					text: 'About Influent',
					click: function() {
						// TODO: Change this to about page location
						aperture.pubsub.publish(appChannel.OPEN_EXTERNAL_LINK_REQUEST, {
							link: aboutURL
						});
					}
				});
			}
			_UIObjectState.data.menus.push(helpMenu);
		};

		//--------------------------------------------------------------------------------------------------------------
		// Public
		//--------------------------------------------------------------------------------------------------------------

		var module = {};
		$.extend(module, _specOptions);

		module.setSpec = function(spec) {
			_UIObjectState.spec = spec;
		};

		module.render = function() {

			_UIObjectState.canvas.empty();

			_constructMenus();

			// Plug everything into the template
			_UIObjectState.canvas.append(infHeaderTemplate(_UIObjectState.data));

			// Link up any clickables
			aperture.util.forEach(_UIObjectState.data.menus, function(e) {
				if (e.click) {
					$('#' + e.id).click(e.click);
				}

				aperture.util.forEach(e.children, function(e) {
					if (e.click) {
						$('#' + e.id).click(e.click);
					}
				});
			});

		};

		//--------------------------------------------------------------------------------------------------------------

		// Register the module with the system
		modules.register('infHeader', function(sandbox) {
			return {
				start: function() {

					var subTokens = {};
					subTokens[appChannel.ALL_MODULES_STARTED] = aperture.pubsub.subscribe(appChannel.ALL_MODULES_STARTED, function() { _init(sandbox); });
					_UIObjectState.subscriberTokens = subTokens;
				},
				end : function() {
					// unsubscribe to all channels
					for (var token in _UIObjectState.subscriberTokens) {
						if (_UIObjectState.subscriberTokens.hasOwnProperty(token)) {
							aperture.pubsub.unsubscribe(_UIObjectState.subscriberTokens[token]);
						}
					}
				}
			};
		});

		// UNIT TESTING ------------------------------------------------------------------------------------------------

		if (constants.UNIT_TESTS_ENABLED) {
			module._UIObjectState = _UIObjectState;
			module._constructMenus = _constructMenus;
		}

		// UNIT TESTING ------------------------------------------------------------------------------------------------

		return module;
	}
);
