/**
 * Copyright © 2013-2015 Uncharted Software Inc.
 *
 * Property of Uncharted™, formerly Oculus Info Inc.
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
define([
		'lib/communication/applicationChannels',
		'lib/plugins',
		'views/infView',
		'lib/search',
		'lib/util/infDescriptorUtilities'
	],
	function(
		appChannel,
		plugins,
		infView,
		search,
		descriptorUtil
	) {
		var _UIObjectState = {
			subscriberTokens : null,
			views : {},
			searchParams : {}
		};

		//--------------------------------------------------------------------------------------------------------------

		var _startViews = function() {
			_UIObjectState.subscriberTokens = {};
			_UIObjectState.subscriberTokens[appChannel.VIEW_REGISTERED] = aperture.pubsub.subscribe(appChannel.VIEW_REGISTERED, _onViewRegistered);
			_UIObjectState.subscriberTokens[appChannel.ALL_MODULES_STARTED] = aperture.pubsub.subscribe(appChannel.ALL_MODULES_STARTED, _initViews);
			_UIObjectState.subscriberTokens[appChannel.VIEW_PARAMS_CHANGED] = aperture.pubsub.subscribe(appChannel.VIEW_PARAMS_CHANGED, _onViewParametersChanged);
			_UIObjectState.subscriberTokens[appChannel.SEARCH_PARAMS_EVENT] = aperture.pubsub.subscribe(appChannel.SEARCH_PARAMS_EVENT, _onSearchParams);

			var views = plugins.get('views');
			aperture.util.forEach(views, function(view) {

				var _getCaptureDimensions = function() {
					var viewDiv = _UIObjectState[view.key].canvas.find('.simpleViewContentsContainer');
					return {
						width : viewDiv[0].scrollWidth,
						height : viewDiv[0].scrollHeight
					};
				};

				_UIObjectState.views[view.key] = view;
				infView.registerView({
					title: view.key,
					label: view.label,
					icon: view.icon,
					getCaptureDimensions : _getCaptureDimensions,
					saveState : function(callback){ callback(true); },
					queryParams : function() { return {query: view.query}; }
				});
			});
		};

		//--------------------------------------------------------------------------------------------------------------

		var _buildContainerAPI = function(view, type) {
			var stateChange = function(propertyMatchDescriptors) {
				if (view.stateless) {
					var query = descriptorUtil.getQueryFromDescriptors(propertyMatchDescriptors, type, _UIObjectState.searchParams);
					if (view.query !== query) {
						view.query = query;
						aperture.pubsub.publish(appChannel.SELECT_VIEW, {
							title: view.key,
							queryParams: {
								query: view.query
							}
						});
					}
				}
			};

			var container = {
				search : {
					entities : search.entities,
					links : search.links
				},
				events : {
					stateChange : stateChange
				},
				canvas : view.canvas
			};
			return container;
		};

		//--------------------------------------------------------------------------------------------------------------

		var _addSpecToList = function(specs, view, type, getDescriptors) {
			var containerAPI = _buildContainerAPI(view, type);

			specs.push({
				icon: view.icon,
				switchesTo: view.key,
				title: view.tooltip,
				requiresSelection: true,

				callback: function () {
					var selectViewData = {
						title : view.key
					};

					if (view.stateless) {
						view.query = descriptorUtil.getQueryFromDescriptors(getDescriptors(), type, _UIObjectState.searchParams);
						selectViewData.queryParams = {
							query: view.query
						};
					}
					view.view(type, getDescriptors(), containerAPI);
					aperture.pubsub.publish(appChannel.SELECT_VIEW, selectViewData);
				}
			});
		};

		//--------------------------------------------------------------------------------------------------------------

		var _isMatch = function(sendDescs, view, type) {
			return (view.receives.hasOwnProperty(type) && view.receives[type] &&
				sendDescs.hasOwnProperty(type) && sendDescs[type]);
		};

		//--------------------------------------------------------------------------------------------------------------

		var _addButtonSpec = function(sendDescs, buttonSpecs, getDescriptors) {
			aperture.util.forEach(_UIObjectState.views, function(view) {
				if (_isMatch(sendDescs, view, 'entities')) {
					_addSpecToList(buttonSpecs, view, 'entities', getDescriptors);
				}
				if (_isMatch(sendDescs, view, 'links')) {
					_addSpecToList(buttonSpecs, view, 'links', getDescriptors);
				}
			});
		};

		//--------------------------------------------------------------------------------------------------------------

		var _getOpsBarButtonSpecForView = function(sendDescs, getDescriptors) {
			var buttonSpecs = [];

			_addButtonSpec(sendDescs, buttonSpecs, getDescriptors);

			return buttonSpecs;
		};

		//--------------------------------------------------------------------------------------------------------------

		/**
		 *
		 * @param eventChannel
		 * @param data
		 * @private
		 */
		var _onViewParametersChanged = function(eventChannel, data) {

			if (eventChannel !== appChannel.VIEW_PARAMS_CHANGED) {
				aperture.log.error('_onViewParametersChanged: function called with illegal event channel');
				return false;
			}

			if (data == null ||
				data.title == null ||
				data.all == null
				) {
				aperture.log.error('_onViewParametersChanged: function called with invalid data');
				return false;
			}

			aperture.util.forEach(_UIObjectState.views, function(view) {
				if (view.key === data.title) {
					if (!data.all.query || !view.stateless) {
						return false;
					}

					if (data.all.query !== view.query) {
						view.query = data.all.query;
						var descriptorObj = descriptorUtil.getDescriptorsFromQuery(view.query, _UIObjectState.searchParams);
						view.view(descriptorObj.searchType, descriptorObj.matchDescriptorMap,  _buildContainerAPI(view, descriptorObj.searchType));
					}
					return false;
				}
			});

			return false;
		};

		//--------------------------------------------------------------------------------------------------------------

		var _onViewRegistered = function(eventChannel, data) {
			if (eventChannel !== appChannel.VIEW_REGISTERED) {
				aperture.log.error('_onViewRegistered: function called with illegal event channel');
				return false;
			}

			aperture.util.forEach(_UIObjectState.views, function(view) {
				if (view.key === data.name) {
					view.canvas = data.canvas;
				}
			});
		};

		//--------------------------------------------------------------------------------------------------------------

		var _onSearchParams = function(eventChannel, data) {
			if (eventChannel !== appChannel.SEARCH_PARAMS_EVENT) {
				aperture.log.error('_onSearchParams: function called with illegal event channel');
				return false;
			}

			_UIObjectState.searchParams[data.paramsType] = data.searchParams;
		};

		//--------------------------------------------------------------------------------------------------------------

		var _initViews = function(eventChannel) {
			if (eventChannel !== appChannel.ALL_MODULES_STARTED) {
				aperture.log.error('_initViews: function called with illegal event channel');
				return false;
			}

			aperture.util.forEach(_UIObjectState.views, function(view) {
				view.init(view.canvas);
				aperture.pubsub.publish(appChannel.VIEW_INITIALIZED, {name: view.key});
			});
		};

		//--------------------------------------------------------------------------------------------------------------
		// Public
		//--------------------------------------------------------------------------------------------------------------

		var viewPluginModule = {
			startViews : _startViews,
			getOpsBarButtonSpecForView : _getOpsBarButtonSpecForView
		};

		return viewPluginModule;
	}
);