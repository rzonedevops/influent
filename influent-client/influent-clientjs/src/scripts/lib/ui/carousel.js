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

define( [ 'lib/util/xfUtil' ],
	function( xfUtil ) {
		function openImage() {
			var url = $(this).css('background-image');
			url = url.substr(4, url.length-5);
			
			window.open(url, '_blank');
			
			return false;
		}
		
		function carousel(imgUrls, image) {
			image.click(openImage);
			
			if (imgUrls.length > 1) {
				var currentIdx = 0;
				var controlDiv = image.find('.infSearchResultImageCarouselControls');
				
				image.hover(function() {
					controlDiv.css('visibility','visible');
				}, function() {
					controlDiv.css('visibility','hidden');
				});
	
				image.find('#prevCarouselButton').click(function() {
					currentIdx--;
					if (currentIdx < 0) {
						currentIdx = imgUrls.length-1;
					}
					image.css('background-image','url('+ imgUrls[currentIdx]+ ')');
					return false;
				});

				image.find('#nextCarouselButton').click(function() {
					currentIdx++;
					if (currentIdx > imgUrls.length-1) {
						currentIdx = 0;
					}
					image.css('background-image','url('+ imgUrls[currentIdx]+ ')');
					return false;
				});
			}
		}
		
		return carousel;
	}
);
