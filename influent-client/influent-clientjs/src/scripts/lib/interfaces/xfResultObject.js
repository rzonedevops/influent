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

define(['lib/module', 'lib/communication/applicationChannels'], function() {
	var module = {};

	var unimpMethod = 'Unimplemented Method';

	module.implementedBy = function(implementingObject, implementingObjectModuleName) {

		implementingObject.getXfId = function() {
			aperture.log.error(unimpMethod + ': "getXfId" by module ' + implementingObjectModuleName);
		};

		implementingObject.getDataId = function() {
			aperture.log.error(unimpMethod + ': "getDataId" by module ' + implementingObjectModuleName);
		};

		implementingObject.getResultByXfId = function(xfId) {
			aperture.log.error(unimpMethod + ': "getResultByXfId" by module ' + implementingObjectModuleName);
		};

		implementingObject.getResultByDataId = function(dataId) {
			aperture.log.error(unimpMethod + ': "getResultByDataId" by module ' + implementingObjectModuleName);
		};

		implementingObject.getUIType = function() {
			aperture.log.error(unimpMethod + ': "getUIType" by module ' + implementingObjectModuleName);
		};

		implementingObject.update = function() {
			aperture.log.error(unimpMethod + ': "update" by module ' + implementingObjectModuleName);
		};

		implementingObject.getSpecs = function() {
			aperture.log.error(unimpMethod + ': "getSpecs" by module ' + implementingObjectModuleName);
		};

		implementingObject.dispose = function() {
			aperture.log.error(unimpMethod + ': "dispose" by module ' + implementingObjectModuleName);
		};

		implementingObject.setVisibility = function(isVisible, notify) {
			aperture.log.error(unimpMethod + ': "setVisibility" by module ' + implementingObjectModuleName);
		};

		implementingObject.numVisible = function() {
			aperture.log.error(unimpMethod + ': "numVisible" by module ' + implementingObjectModuleName);
		};

		implementingObject.numResults = function() {
			aperture.log.error(unimpMethod + ': "numResults" by module ' + implementingObjectModuleName);
		};

		implementingObject.showMoreResults = function(numResultsToShow) {
			aperture.log.error(unimpMethod + ': "showMoreResults" by module ' + implementingObjectModuleName);
		};

		implementingObject.setSelected = function(xfId, isSelected) {
			aperture.log.error(unimpMethod + ': "setSelected" by module ' + implementingObjectModuleName);
		};

		implementingObject.getSelectedDataIds = function() {
			aperture.log.error(unimpMethod + ': "getSelectedDataIds" by module ' + implementingObjectModuleName);
		};

		implementingObject.getSelectedSpecs = function() {
			aperture.log.error(unimpMethod + ': "getSelectedSpecs" by module ' + implementingObjectModuleName);
		};

		implementingObject.setExpanded = function(xfId, isExpanded) {
			aperture.log.error(unimpMethod + ': "setExpanded" by module ' + implementingObjectModuleName);
		};
	};

	return module;
});
