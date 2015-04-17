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
		'lib/constants',
		'lib/communication/accountsViewChannels',
		'lib/models/xfSearchResultBase',
		'lib/models/xfEntityResultCluster',
		'lib/models/xfEntityResult',
		'lib/util/GUID'
	],
	function(
		constants,
		accountsChannel,
		xfSearchResultBase,
		clusterModel,
		resultModel,
		guid
	) {

		//--------------------------------------------------------------------------------------------------------------
		// Private Variables
		//--------------------------------------------------------------------------------------------------------------

		var MODULE_NAME = constants.MODULE_NAMES.ACCOUNTS_SEARCH_RESULT;

		//--------------------------------------------------------------------------------------------------------------
		// Public
		//--------------------------------------------------------------------------------------------------------------

		var xfResultModule = {};

		//--------------------------------------------------------------------------------------------------------------

		xfResultModule.createInstance = function(searchResult, searchParams){

			//----------------------------------------------------------------------------------------------------------
			// Initialization
			//----------------------------------------------------------------------------------------------------------

			var _UIObjectState = {
				xfId     : '',
				UIType   : MODULE_NAME,
				children : [],
				headers  : {},
				totalResults : 0,
				matchScores : {}
			};

			// set the xfId
			_UIObjectState.xfId = 'search_result_' + guid.generateGuid();

			for (var i = 0; i < searchResult.data.length; i++) {
				if (searchResult.data[i].hasOwnProperty('groupKey')) {
					_UIObjectState.children.push(clusterModel.createInstance(searchResult.data[i]));
				} else {
					for (var j = 0; j < searchResult.data[i].items.length; j++) {
						_UIObjectState.children.push(resultModel.createInstance(searchResult.data[i].items[j]));
					}
				}
			}

			_UIObjectState.headers = searchResult.headers;

			_UIObjectState.totalResults = searchResult.totalResults;

			_UIObjectState.matchScores = searchResult.matchScores;

			_UIObjectState.levelOfDetail = searchResult.detailLevel;

			// create new object instance
			var xfInstance = xfSearchResultBase.createInstance(_UIObjectState, searchParams);

			//----------------------------------------------------------------------------------------------------------
			// Public methods
			//----------------------------------------------------------------------------------------------------------

			xfInstance.getHeaderInformation = function() {
				var columnInfo = [];
				var i, property;

				var numNonImageHeaders = 0;
				for (i = 0; i < _UIObjectState.headers.properties.length; i++) {
					property = _UIObjectState.headers.properties[i];
					if (property.propertyType !== 'IMAGE') {
						numNonImageHeaders++;
					}
				}

				var colWidth = (100 / Math.max(1,numNonImageHeaders)) + '%';

				var orderFunc = function(orderBy) {
					if (orderBy.propertyKey === property.key) {
						if (orderBy.ascending) {
							orderAsc = true;
						} else {
							orderDesc = true;
						}
						return true;
					}
				};

				// get column and sortBy labels
				for (i = 0; i < _UIObjectState.headers.properties.length; i++) {
					property = _UIObjectState.headers.properties[i];

					var orderAsc = false;
					var orderDesc = false;
					aperture.util.forEachUntil(_UIObjectState.headers.orderBy, orderFunc);
					
					var isImage = (property.propertyType === 'IMAGE');

					var numFormat = false;
					switch (property.propertyType) {
					case 'INTEGER':
					case 'FLOAT':
					case 'DOUBLE':
					case 'LONG':
					case 'DATE':
						numFormat = true;
					}
					
					columnInfo.push({
						isImage : isImage,
						columnWidth : isImage ? '100px' : colWidth,
						orderAsc : orderAsc,
						orderDesc : orderDesc,
						property: property,
						numFormat: numFormat
					});
				}

				return {
					columns : columnInfo
				};
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getDetailLevel = function() {
				return _UIObjectState.levelOfDetail;
			};

			//----------------------------------------------------------------------------------------------------------

			return xfInstance;
		};

		//--------------------------------------------------------------------------------------------------------------

		xfResultModule.getModuleName = function() {
			return MODULE_NAME;
		};

		//--------------------------------------------------------------------------------------------------------------

		return xfResultModule;
	}
);
