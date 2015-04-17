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

define('templates/helpers/markupTerms', ['hbs/handlebars', 'templates/helpers/ellipsis'], function(Handlebars, ellipsis) {

	// escape special chars in text for html
	function escape(text) {
		return $('<div/>').text(text).html();
	}
	
	/**
	* Returns html representation of value text with css class markup of the terms specified.
	* Note that since this is a Handlebars helper and the result is html, the result should
	* use triple 'stashes instead of double: {{{markupTerms value terms cssClass}}}
	* 
	* @param value
	*	The value being subjected to markup in raw (non-html) form
	* @param terms
	*	The terms being marked as regular expressions or strings
	* @param cssClass
	*	The CSS class to mark with a span.
	* 
	* @returns
	*	An html value.
	*/
	function markupTerms(value, terms, cssClass, maxLength) {
		
		// skip out early if no terms.
		if (terms == null || terms.length === 0) {
			if (maxLength) {
				value= ellipsis.text(value, maxLength);
			}
			
			return escape(value);
		}

		// if not an array, make it one.
		if (!aperture.util.isArray(terms)) {
			terms = [terms];
		}

		
		var spans = [{unmatched : String(value)}];
		
		// process for each term in turn
		terms.forEach(function(term) {

			// skip out early if term is empty
			if (term.length === 0) {
				return;
			}

			// convert to regexp if a string.
			// escape all regexp flags, then allow any whitespace for any whitespace
			if (aperture.util.isString(term)) {
				term = new RegExp(
					term.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&') // string to regex (escape flags)
						.replace(/\s/, '\\s'), // string to regex (any whitespace)
						'gi'); // all, case insensitive
			}
			
			var newSpans = [];

			// process spans.
			spans.forEach(function(span) {
				var text = span.unmatched,
					start = 0,
					match;
			
				if (text != null) {
					while ((match = term.exec(text)) !== null) {
						
						// if there is text preceding this match, add it.
						if (match.index > start) {
							newSpans.push({unmatched:text.substring(start, match.index)});
						}
						
						// then add the match
						newSpans.push({matched: match[0]});

						// then update the start of the next match
						start = match.index + match[0].length;
					}
					
					// when done check for tail
					if (start < text.length) {
						newSpans.push({unmatched: text.substr(start)});
					}

				// if already a match just push it along
				} else {
					newSpans.push(span);
				}
			});
			
			spans = newSpans;
			
		});

		var markup = '',
			spanTag = '<span class="'+ cssClass + '">';

		// now mark it up
		spans.forEach(function(span) {
			if (span.matched) {
				markup += spanTag + escape(span.matched) + '</span>';
			} else {
				markup += escape(span.unmatched);
			}
		});
		
		if (maxLength) {
			markup= ellipsis.html(markup, maxLength);
		}
		
		return markup;
	}

	Handlebars.registerHelper('markupTerms', markupTerms);
	
	return markupTerms;
});