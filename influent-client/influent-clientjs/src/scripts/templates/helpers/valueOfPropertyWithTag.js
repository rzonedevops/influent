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

define('templates/helpers/valueOfPropertyWithTag', ['hbs/handlebars'], function(Handlebars) {

	/**
	* Returns the value of the first property found with a given tag.  (ie/ 'DATE')
	* @param propertyMap
	*	The map of properties past to the context handler template
	* @param tag
	*	String value of the tag we're looking for (ie/ 'DATE','ANNOTATION', etc)
	*/
	function valueOfPropertyWithTag(propertyMap,tag) {
		var taggedPropKey = null;
		for(var prop in propertyMap) {
			if (propertyMap.hasOwnProperty(prop)) {
				for (var i = 0; i < propertyMap[prop].tags.length; i++) {
					if (propertyMap[prop].tags[i].toLowerCase() === tag.toLowerCase()) {
						taggedPropKey = prop;
					}
				}
				if (taggedPropKey !== null) {
					break;
				}
			}
		}
		if (taggedPropKey) {
			return propertyMap[taggedPropKey].value;
		} else {
			return null;
		}
	}

	Handlebars.registerHelper('valueOfPropertyWithTag', valueOfPropertyWithTag);
	return valueOfPropertyWithTag;
});