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

		var MODULE_NAME = constants.MODULE_NAMES.RESULT_BASE;

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
				return (xfId != null && _UIObjectState.xfId === xfId) ? xfInstance : null;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getResultByDataId = function(dataId) {
				return (dataId != null && _UIObjectState.spec.dataId != null && _UIObjectState.spec.dataId === dataId) ? xfInstance : null;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getUIType = function() {
				return _UIObjectState.UIType;
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
				_UIObjectState.spec = null;
				_UIObjectState = null;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.setVisibility = function(isVisible, notify) {
				var hasChanged = false;
				if (_UIObjectState.isVisible !== isVisible) {
					_UIObjectState.isVisible = isVisible;
					hasChanged = true;
				}

				if (notify && hasChanged) {
					aperture.pubsub.publish(accountsChannel.RESULT_VISIBILITY_CHANGE);
				}

				return hasChanged;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.numVisible = function() {
				return _UIObjectState.isVisible ? 1 : 0;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.numResults = function() {
				return 1;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.showMoreResults = function(numResultsToShow) {

				if (numResultsToShow <= 0) {
					return 0;
				}

				var localNumResultsToShow = numResultsToShow;
				if (localNumResultsToShow > 0 && !_UIObjectState.isVisible) {
					_UIObjectState.isVisible = true;
					localNumResultsToShow--;
				}

				return localNumResultsToShow;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.setSelected = function(xfId, isSelected) {
				if (_UIObjectState.xfId === xfId) {
					_UIObjectState.isSelected = isSelected;
				}
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getSelectedDataIds = function() {
				var dataIds = [];
				if (_UIObjectState.isSelected) {
					dataIds.push(_UIObjectState.spec.dataId);
				}
				return dataIds;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getSelectedSpecs = function() {
				var specs = [];
				if (_UIObjectState.isSelected) {
					specs.push(_.clone(_UIObjectState.spec));
				}
				return specs;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.setExpanded = function(/*xfId, isExpanded*/) {};

			//----------------------------------------------------------------------------------------------------------
			// Result specific methods
			//----------------------------------------------------------------------------------------------------------

			xfInstance.isVisible = function() {
				return _UIObjectState.isVisible;
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