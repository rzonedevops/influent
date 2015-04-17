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

define('templates/helpers/ellipsis', ['hbs/handlebars'], function(Handlebars) {

	var DDD = '...';
	var DEFAULT_LENGTH = 80;
	
	/**
	* Returns the ellipsis truncated version of a value constrained by string length,
	* or the raw value if under the limit.
	* 
	* @param value
	*	The value being subjected to truncation in raw (non-html) form
	* @param [length]=80
	*	The limit length of the string as a character count.
	* @returns
	*	A value guaranteed to be less than or equal to the limit in length, including
	*	ellipsis if too long.
	*/
	function text(value, length) {
		length = length || DEFAULT_LENGTH;
		
		// stringify
		value = String(value);

		// too long?
		if (value.length > length) {
			
			if (length <= DDD.length) {
				return '';
			}
			
			// cut
			value = value.substr(0, length - DDD.length);
			
			// look for whitespace break.
			var iright = value.match(/\s*[^\s]+$/);
			
			if (iright) {
				value = value.substring(0, iright.index); // nicer break point.
			}
			
			return value + DDD;
		}
		
		return value;
	}

	// recursive method which trims html nodes to a length limit
	function trim(node, length) {
		if (length === 0) {
			node.parentNode.removeChild(node);
			
			return 0;
		}
		
		if (node.nodeType === 3) {
			var txt = node.nodeValue;
			
			// shorten?
			if (txt.length > length) {
				txt = text(txt, length);
				
				node.nodeValue = txt;
				
				return length;
			} 
			
			return txt.length;
			
		} else if (node.childNodes) {
			var nodes= node.childNodes,
				child,
				count = 0,
				i= 0;
			
			for (i=0; i<nodes.length; i++) {
				child = nodes.item(i);
				count += trim(child, length-count);
				
				if (child.parentNode == null) {
					i--;
				}
			}
			
			return count;
		}
		
		return 0;
	}
	
	/**
	* Returns the ellipsis truncated version of html constrained by string length,
	* or the original html if under the limit.
	* 
	* @param htm
	*	The html being subjected to truncation
	* @param [length]=80
	*	The limit length of the string as a character count.
	* @returns
	*	A value guaranteed to be less than or equal to the limit in length, including
	*	ellipsis if too long.
	*/
	function html(htm, length) {
		length = length || DEFAULT_LENGTH;

		var $html = $('<p>'+ htm+ '</p>');
		var txt = $html.text();
		
		// too long?
		if (txt.length > length) {
			var /*c = 0,*/ n = length - DDD.length;

			if (n < 1) {
				return DDD;
			}
			
			var elem = $html.get()[0];
			
			trim(elem, length);
			
			return $html.html();
		}
		
		return htm;
	}

	Handlebars.registerHelper('ellipsis.html', html);
	Handlebars.registerHelper('ellipsis.text', text);
	
	return {
		text: text,
		html: html
	};
});