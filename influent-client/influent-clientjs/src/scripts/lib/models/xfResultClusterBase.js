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
		'lib/interfaces/xfResultObject'
	],
	function(
		constants,
		accountsChannel,
		xfUIObject
	) {

		//--------------------------------------------------------------------------------------------------------------
		// Private Variables
		//--------------------------------------------------------------------------------------------------------------

		var MODULE_NAME = constants.MODULE_NAMES.RESULT_CLUSTER_BASE;

		//--------------------------------------------------------------------------------------------------------------
		// Public
		//--------------------------------------------------------------------------------------------------------------

		var xfResultModule = {};

		//--------------------------------------------------------------------------------------------------------------

		xfResultModule.createInstance = function(uiObjectState){

			//----------------------------------------------------------------------------------------------------------
			// Initialization
			//----------------------------------------------------------------------------------------------------------

			var _UIObjectState = uiObjectState;

			// create new object instance
			var xfInstance = {};
			xfUIObject.implementedBy(xfInstance, MODULE_NAME);

			//----------------------------------------------------------------------------------------------------------
			// Public methods
			//----------------------------------------------------------------------------------------------------------

			xfInstance.getXfId = function() {
				return _UIObjectState.xfId;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getDataId = function() {
				return _UIObjectState.spec.dataId;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getResultByXfId = function(xfId) {

				for (var i = 0; i < _UIObjectState.children.length; i++) {
					var object = _UIObjectState.children[i].getResultByXfId(xfId);
					if (object != null) {
						return object;
					}
				}

				if (xfId != null && _UIObjectState.xfId === xfId) {
					return xfInstance;
				}

				return null;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getResultByDataId = function(dataId) {

				for (var i = 0; i < _UIObjectState.children.length; i++) {
					var object = _UIObjectState.children[i].getResultByDataId(dataId);
					if (object != null) {
						return object;
					}
				}

				if (dataId != null && _UIObjectState.spec.dataId != null && _UIObjectState.spec.dataId === dataId) {
					return xfInstance;
				}

				return null;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getUIType = function() {
				return _UIObjectState.UIType;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getEntityType = function() {
				return _UIObjectState.spec.entityType;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.update = function(spec) {
				for (var key in spec) {
					if ( spec.hasOwnProperty(key) ) {
						_UIObjectState.spec[key] = spec[key];
					}
				}
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getSpecs = function(bOnlyEmptySpecs) {
				var specs = [];

				if (bOnlyEmptySpecs) {
					if (_UIObjectState.spec.graphUrl === '') {
						specs.push(_.clone(_UIObjectState.spec));
					}
				} else {
					specs.push(_.clone(_UIObjectState.spec));
				}

				return specs;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.dispose = function() {

				var kids = _UIObjectState.children;
				_UIObjectState.children = [];

				for (var i = 0; i < kids.length; i++) {
					kids[i].dispose();
					kids[i] = null;
				}

				_UIObjectState.spec = null;
				_UIObjectState = null;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.setVisibility = function(isVisible, notify) {
				var hasChanged = false;
				for (var i = 0; i < _UIObjectState.children.length; i++) {
					if (_UIObjectState.children[i].setVisibility(isVisible, false)) {
						hasChanged = true;
					}
				}

				if (notify && hasChanged) {
					aperture.pubsub.publish(accountsChannel.RESULT_VISIBILITY_CHANGE);
				}

				return hasChanged;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.numVisible = function() {
				var numVisible = 0;

				for (var i = 0; i < _UIObjectState.children.length; i++) {
					numVisible += _UIObjectState.children[i].numVisible();
				}

				return numVisible;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.numResults = function() {
				var numResults = 0;

				for (var i = 0; i < _UIObjectState.children.length; i++) {
					numResults += _UIObjectState.children[i].numResults();
				}

				return numResults;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.showMoreResults = function(numResultsToShow) {

				if (numResultsToShow <= 0) {
					return 0;
				}

				var localNumResultsToShow = numResultsToShow;
				for (var i = 0; i < _UIObjectState.children.length; i++) {
					localNumResultsToShow = _UIObjectState.children[i].showMoreResults(localNumResultsToShow);

					if (localNumResultsToShow <= 0) {
						return 0;
					}
				}

				return localNumResultsToShow;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.setSelected = function(xfId, isSelected) {
				if (_UIObjectState.xfId === xfId) {
					aperture.util.forEach(_UIObjectState.children, function(child) {
						child.setSelected(child.getXfId(), isSelected);
					});
				} else {
					aperture.util.forEach(_UIObjectState.children, function(child) {
						child.setSelected(xfId, isSelected);
					});
				}
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getSelectedDataIds = function() {
				var dataIds = [];
				aperture.util.forEach(_UIObjectState.children, function(child) {
					aperture.util.forEach(child.getSelectedDataIds(), function(dataId) {
						dataIds.push(dataId);
					});
				});

				return dataIds;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getSelectedSpecs = function() {
				var specs = [];
				aperture.util.forEach(_UIObjectState.children, function(child) {
					aperture.util.forEach(child.getSelectedSpecs(), function(result) {
						specs.push(result);
					});
				});

				return specs;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.setExpanded = function(xfId, isExpanded) {
				if (_UIObjectState.xfId === xfId) {
					_UIObjectState.isExpanded = isExpanded;
				}
			};

			//----------------------------------------------------------------------------------------------------------
			// Cluster specific methods
			//----------------------------------------------------------------------------------------------------------

			xfInstance.getNumChildren = function() {
				return _UIObjectState.children.length;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getChildren = function() {
				return _.clone(_UIObjectState.children);
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getClusterLabel = function() {
				return _UIObjectState &&
					_UIObjectState.spec &&
					_UIObjectState.spec.groupKey &&
					_UIObjectState.spec.groupKey;
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
