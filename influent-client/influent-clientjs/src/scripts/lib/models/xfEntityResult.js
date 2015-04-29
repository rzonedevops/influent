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
		'lib/models/xfResultBase',
		'lib/util/GUID',
		'lib/util/iconUtil',
		'lib/util/infTagUtilities'
	],
	function(
		constants,
		accountsChannel,
		xfResultBase,
		guid,
		iconUtil,
		tagUtil
	) {

		//--------------------------------------------------------------------------------------------------------------
		// Private Variables
		//--------------------------------------------------------------------------------------------------------------

		var MODULE_NAME = constants.MODULE_NAMES.ENTITY_RESULT;

		var resultSpecTemplate = {
			dataId     : '',
			entityType : '',
			entityTags : [],
			properties : {}
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
				xfId    : '',
				UIType  : MODULE_NAME,
				spec    : {},
				isVisible : false,
				isSelected : false
			};

			// set the xfId
			_UIObjectState.xfId = 'result_' + guid.generateGuid();

			// populate UI object state spec with passed in spec
			_UIObjectState.spec = this.getSpecTemplate();
			for (var key in spec) {
				if ( spec.hasOwnProperty(key) ) {
					if (key === 'uid') {
						_UIObjectState.spec.dataId = spec[key];
					}
					_UIObjectState.spec[key] = spec[key];
				}
			}

			// create new object instance
			var xfInstance = xfResultBase.createInstance(_UIObjectState);

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getVisualInfo = function(headerInfo, dedupDetails) {

				var i, property = null, column = null;

				var details = aperture.util.extend({}, _UIObjectState.spec.properties);
				
				var visualInfo = {
					uid: _UIObjectState.spec.uid,
					columns: [],
//					details: details,
					properties: details,
					detailsSpan: 0,
					promptForDetails: _UIObjectState.spec.promptForDetails
				};

				var numNonImageProperties = headerInfo.columns.length;
				for (i = 0; i < headerInfo.columns.length; i++) {
					if (headerInfo.columns[i].isImage) {
						numNonImageProperties--;
					}
				}

				// create summary information
				for (i = 0; i < headerInfo.columns.length; i++) {

					property = null;
					column = null;

					var columnHeader = headerInfo.columns[i];

					if (_UIObjectState.spec.properties) {

						property = _UIObjectState.spec.properties[columnHeader.property.key];

						if (!property) {
							var typeVal = _UIObjectState.spec.type;
							var mappedKey = columnHeader.property.typeMappings[typeVal];

							if (mappedKey) {
								property = _UIObjectState.spec.properties[mappedKey];
							}
						}

						if (property) {
							column = {};
							column.isImage = columnHeader.isImage;
							column.columnWidth = columnHeader.columnWidth;
							column.key = property.key;
							column.numFormat = columnHeader.numFormat;
							
							if (property.key) {
								if (dedupDetails) {
									delete details[property.key];
								}
							}
							
							var icon = null;

							if (columnHeader.isImage) {
								column.text = '';
								if (property.value) {
									column.thumbnailImage = property.value;
								} else if (property.range && property.range.values.length !== 0) {
									column.thumbnailImage = property.range.values[0];
								}
							} else {
								icon = iconUtil.getIconForProperty(property);
								column.image = null;
								if (icon) {
									if (icon.imgUrl && icon.imgUrl.substr(0,6) === 'class:') {
										column.cssicon = icon.imgUrl.substr(6);
									} else {
										column.image = icon.imgUrl;
									}
								}
								column.text = tagUtil.getFormattedValue(property);
							}
						}

						if (column == null) {
							column = {
								isImage : columnHeader.isImage,
								text : '',
								columnWidth : columnHeader.columnWidth
							};
						}

						visualInfo.columns.push(column);
					}
				}

				visualInfo.detailsSpan = numNonImageProperties;

				return visualInfo;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.setDetailsPrompt = function(promptForDetails) {
				_UIObjectState.spec.promptForDetails = promptForDetails;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getDetailsPrompt = function() {
				return _UIObjectState.spec.promptForDetails;
			};

			//----------------------------------------------------------------------------------------------------------
			xfInstance.expandProperties = function(properties) {
				_UIObjectState.spec.properties = properties;
			};

			//----------------------------------------------------------------------------------------------------------

			xfInstance.getSelectedEntityData = function() {
				return {
					xfId : _UIObjectState.xfId,
					dataId : _UIObjectState.spec.dataId,
					entityType: constants.MODULE_NAMES.ENTITY,
					contextId: '',
					label: tagUtil.getPropertiesByTag(_UIObjectState.spec, 'LABEL')[0].value,
					count: 1,
					promptForDetails: _UIObjectState.spec.promptForDetails
				};
			};

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
