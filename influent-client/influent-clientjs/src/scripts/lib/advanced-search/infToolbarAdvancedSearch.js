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
		'lib/advanced-search/infToolbarAdvancedMatchCriteria',
		'lib/communication/applicationChannels',
		'lib/constants',
		'hbs!templates/viewToolbar/infToolbarAdvancedSearch'
	],
	function(
		infToolbarAdvancedMatchCriteria,
		appChannel,
		constants,
		infToolbarAdvancedSearchTemplate
	) {
		var module = {};

		module.createInstance = function (view, canvas, searchParams) {

			var instance = {};

			var _UIObjectState = {
				view: view,
				canvas: canvas,
				criteriaContainer: null,
				widget: null,
				subscriberTokens: null,
				matchCriteria: null,
				orderFields : null
			};

			//----------------------------------------------------------------------------------------------------------
			// Private Methods
			//----------------------------------------------------------------------------------------------------------

			var _onAdvancedSearchRequest = function(eventChannel, data) {
				if (data.view !== _UIObjectState.view) {
					return;
				}

				if (data.dataIds != null && !_.isEmpty(data.dataIds)) {
					//_UIObjectState.orderFields = null;
					_UIObjectState.matchCriteria.setFieldsFromDataIds(data.dataIds);
				} else if (data.specs != null && !_.isEmpty(data.specs)) {
					//_UIObjectState.orderFields = null;
					switch (_UIObjectState.view) {
						case require('views/infAccountsView').name():
							_UIObjectState.matchCriteria.setFieldsFromEntitySpecs(data.specs);
							break;
						case require('views/infTransactionsView').name():
							_UIObjectState.matchCriteria.setFieldsFromTransactionSpecs(data.specs);
							break;
					}
				} else {
					//var regex = new RegExp('\\sORDER:\\s*(\\S)+(\\s*(asc|desc))?', 'gi');
					//_UIObjectState.orderFields = data.input.match(regex);
					_UIObjectState.matchCriteria.setFieldsFromString(data.input);
				}

				_UIObjectState.widget.modal('show');
			};

			//----------------------------------------------------------------------------------------------------------

			function onSearchAction() {
				var searchTerm = _UIObjectState.matchCriteria.assembleSearchString();
				//searchTerm = _UIObjectState.orderFields !== null ? searchTerm.trim() + _UIObjectState.orderFields.join(' ') : searchTerm;
				var msgChan = appChannel.FILTER_SEARCH_CHANGE_EVENT;
				aperture.pubsub.publish(msgChan, {
					view: _UIObjectState.view,
					input: searchTerm
				});
			}

			//----------------------------------------------------------------------------------------------------------

			var _initialize = function () {
				_UIObjectState.matchCriteria = infToolbarAdvancedMatchCriteria.createInstance(_UIObjectState.view, _UIObjectState.criteriaContainer, searchParams);
				_UIObjectState.matchCriteria.build();
			};

			//----------------------------------------------------------------------------------------------------------
			// Public
			//----------------------------------------------------------------------------------------------------------

			instance.render = function () {
				if (!_UIObjectState.canvas || !_UIObjectState.canvas instanceof $) {
					throw 'No assigned canvas for rendering view toolbar widget';
				}

				// Plug everything into the template
				var searchDialog = $(infToolbarAdvancedSearchTemplate());
				searchDialog.find('#infAdvancedSearchGo').click(function() {
					onSearchAction();
				});

				_UIObjectState.widget = searchDialog.appendTo(_UIObjectState.canvas);
				_UIObjectState.criteriaContainer = _UIObjectState.widget.find('#criteria-container');

				_initialize();
			};

			// TODO: Do we need to listen to incoming changes?
			var subTokens = {};
			subTokens[appChannel.ADVANCED_SEARCH_REQUEST] = aperture.pubsub.subscribe(appChannel.ADVANCED_SEARCH_REQUEST, _onAdvancedSearchRequest);
			_UIObjectState.subscriberTokens = subTokens;

			// When popping state, hide any modals and remove the fade background
			window.onpopstate = function() {
				$('.modal').modal('hide');
				$('.modal-backdrop').remove();
			};

			// TODO: .. If so, we need to unsubscribe too.

			if (constants.UNIT_TESTS_ENABLED) {
				instance.onAdvancedSearchRequest = _onAdvancedSearchRequest;
				instance.onSearchAction = onSearchAction;
				instance.UIObjectState = _UIObjectState;
			}

			return instance;
		};

		return module;
	}
);