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

define(
	[

	],
	function(

		){
		aperture.log.info('loaded Kiva stub view module');

		//--------------------------------------------------------------------------------------------------------------

		// return the module to the plugin in config.js
		return {
			onView : function (type, propertyMatchDescriptors, container) {
				var elem = $(container.canvas).find('.infSearchResultScroll');
				if (elem.length === 0) {
					elem = $('<div></div>');
					elem.addClass('infSearchResultScroll');
					$(container.canvas).append(elem);
				}

				$(elem).append('<div><h4>View called with following parameters:<h4></div>');
				$(elem).append('<div>type: ' + type + ' pmd: ' + JSON.stringify(propertyMatchDescriptors) + '</div>');

				if (type === 'entities') {
					container.search.entities(propertyMatchDescriptors, function (response, restInfo) {
						$(elem).append('<div><h4>Entities search result:<h4></div>');
						$(elem).append('<div><b>success:</b> ' + restInfo.success + '</div>');
						$(elem).append('<div><b>response:</b> ' + JSON.stringify(response) + '</div>');
					});
				}
				if (type === 'links') {
					container.search.links(propertyMatchDescriptors, function (response, restInfo) {
						$(elem).append('<div><h4>Entities search result:<h4></div>');
						$(elem).append('<div><b>success:</b> ' + restInfo.success + '</div>');
						$(elem).append('<div><b>response:</b> ' + JSON.stringify(response) + '</div>');
					});
				}

				container.events.stateChange(propertyMatchDescriptors);
			}
		};
	}
);