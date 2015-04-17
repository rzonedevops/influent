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
		'lib/models/xfResultClusterBase',
		'lib/models/xfEntityResult',
		'lib/util/GUID'
	],
	function(
		constants,
		accountsChannel,
		xfResultClusterBase,
		resultModel,
		guid
	) {

		//--------------------------------------------------------------------------------------------------------------
		// Private Variables
		//--------------------------------------------------------------------------------------------------------------

		var MODULE_NAME = constants.MODULE_NAMES.ENTITY_RESULT_CLUSTER;

		var resultSpecTemplate = {
			groupKey : ''
		};

		//--------------------------------------------------------------------------------------------------------------
		// Public
		//--------------------------------------------------------------------------------------------------------------

		var xfResultModule = {};

		//--------------------------------------------------------------------------------------------------------------

		xfResultModule.createInstance = function(spec){

			//----------------------------------------------------------------------------------------------------------
			// Initialization
			//----------------------------------------------------------------------------------------------------------

			var _UIObjectState = {
				xfId       : '',
				UIType     : MODULE_NAME,
				spec       : {},
				children   : [],
				isExpanded : true,
				isVisible  : false
			};

			// set the xfId
			_UIObjectState.xfId = 'result_cluster_' + guid.generateGuid();

			// populate UI object state spec with passed in spec
			_UIObjectState.spec = this.getSpecTemplate();
			for (var key in spec) {
				if ( spec.hasOwnProperty(key) ) {
					if (key === 'items') {

						while(_UIObjectState.children.length > 0) {
							_UIObjectState.children.pop();
						}

						var members = spec.items;
						for (var i = 0; i < members.length; i++) {
							_UIObjectState.children.push(resultModel.createInstance(members[i]));
						}
					} else {
						_UIObjectState.spec[key] = spec[key];
					}
				}
			}

			// create new object instance
			var xfInstance = xfResultClusterBase.createInstance(_UIObjectState);

			//----------------------------------------------------------------------------------------------------------

			return xfInstance;
		};

		//--------------------------------------------------------------------------------------------------------------

		xfResultModule.getSpecTemplate = function() {

			var specTemplate = {};
			$.extend(true, specTemplate, resultSpecTemplate);

			return specTemplate;
		};

		//--------------------------------------------------------------------------------------------------------------

		xfResultModule.getModuleName = function() {
			return MODULE_NAME;
		};

		//--------------------------------------------------------------------------------------------------------------

		return xfResultModule;
	}
);
