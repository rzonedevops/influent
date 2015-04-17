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
		'views/components/resultFooterComponentBase',
		'lib/communication/accountsViewChannels'
	],
	function(
		fixture,
		accountsChannel
	) {

		//--------------------------------------------------------------------------------------------------------------

		describe('resultFooterComponentBase tests', function() {

			//----------------------------------------------------------------------------------------------------------

			it('Ensure the resultComponentBase fixture is defined', function () {
				expect(fixture).toBeDefined();
			});

			//----------------------------------------------------------------------------------------------------------

			it('Ensure the footer is set up with more results', function () {

				var container = $('<div>');

				fixture.setFooter(
					container,
					true,
					accountsChannel.RESULT_VISIBILITY_CHANGE
				);

				var moreButton = container.find('button');
				expect(moreButton).toExist();

				spyOn(aperture.pubsub, 'publish');

				var spyEvent = spyOnEvent(moreButton, 'click');
				moreButton.trigger('click');

				expect('click').toHaveBeenTriggeredOn(moreButton);
				expect(spyEvent).toHaveBeenTriggered();

				expect(aperture.pubsub.publish).toHaveBeenCalledWith(accountsChannel.RESULT_VISIBILITY_CHANGE);
			});

			//----------------------------------------------------------------------------------------------------------

			it('Ensure the footer is set up with no more results', function () {

				var container = $('<div>');

				fixture.setFooter(
					container,
					false,
					accountsChannel.RESULT_VISIBILITY_CHANGE
				);

				var moreButton = container.find('button');
				expect(moreButton).not.toExist();
			});
		});
	}
);