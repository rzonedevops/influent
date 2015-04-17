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

/**
 * This helper will delegate its results to a callback.
 *
 * Ex.
 * template.hbs:
 * <span>{{callback myCallback this}}</span>
 *
 * code.js:
 * template({
 *     myCallback: function(){ return 'hi';}
 * });
 *
 * Will result in the callback 'myCallback' being called and inserting 'hi' into the span element.
 *
 */
define('templates/helpers/callback', ['hbs/handlebars'], function(Handlebars) {
	/**
	 *
	 * @param callbackName
	 *   The name of the callback function parameter in options.data
	 * @param context
	 *   The context that's passed to the function
	 * @returns {SafeString}
	 */
	function callback(cb, context) {
		var result;

		if (cb && cb != null) {
			result = cb(context);
		}

		if (!result || result === null) {
			result = '';
		}

		return new Handlebars.SafeString(result);
	}

	Handlebars.registerHelper('callback', callback);
	return callback;
});
