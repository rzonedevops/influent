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
		'views/infSummaryView',
		'lib/constants'
	],	
	function(fixture, constants) {

		describe('Initial View Test', function() {
			it('infSummaryView Has testing enabled', function() {
				expect(constants.UNIT_TESTS_ENABLED).toBeTruthy();
			});

			it('infSummaryView returns a module', function() {
				expect(fixture).toBeDefined();
			});
		});

		//--------------------------------------------------------------------------------------------------------------

		describe('Test _getStat function', function() {
			var stats = {
				myStat: {
					label: 'myStatLabel',
					value: 'myStatValue'
				},
				myMissingLabelStat: {
					value: 'myStatValue'
				},
				myMissingValueStat: {
					label: 'myStatLabel'
				},
				myNullLabelStat: {
					label: null,
					value: 'myStatValue'
				},
				myNullValueStat: {
					label: 'myStatLabel',
					value: null
				},
				myNoObject: null
			};

			it('test proper stat object', function() {
				var stat = fixture._getStat(stats, 'myStat');

				expect(stat.label).toEqual('myStatLabel');
				expect(stat.value).toEqual('myStatValue');
			});

			it('test missing and null values', function() {
				expect(fixture._getStat(stats, 'myMissingLabelStat')).toBe(null);
				expect(fixture._getStat(stats, 'myMissingValueStat')).toBe(null);
				expect(fixture._getStat(stats, 'myNullLabelStat')).toBe(null);
				expect(fixture._getStat(stats, 'myNullValueStat')).toBe(null);
			});
		});

		//--------------------------------------------------------------------------------------------------------------
		var state = fixture._UIObjectState;
		//--------------------------------------------------------------------------------------------------------------

		describe('Test _handleSummaryResponse function', function() {

			it('the summary view handles null responses', function() {

				var response = null;

				state.canvas = $('<div></div>');
				fixture._handleSummaryResponse(response);

				var summaryLabel = state.canvas.find('.summaryCaption');
				expect(summaryLabel.text()).toEqual('SUMMARY DATA UNAVAILABLE');
			});

			it('the summary view handles empty object responses', function() {

				var response = {};

				state.canvas = $('<div></div>');
				fixture._handleSummaryResponse(response);

				var summaryLabel = state.canvas.find('.summaryCaption');
				expect(summaryLabel.text()).toEqual('SUMMARY DATA UNAVAILABLE');
			});

			it('the summary view handles empty array responses', function() {

				var response = [];

				state.canvas = $('<div></div>');
				fixture._handleSummaryResponse(response);

				var summaryLabel = state.canvas.find('.summaryCaption');
				expect(summaryLabel.text()).toEqual('SUMMARY DATA UNAVAILABLE');
			});

			it('the summary view handles proper responses', function() {

				var response = [
					{
						key: 'InfoSummary',
						label: 'ABOUT',
						value: 'Some Interesting Description'
					},
					{
						key: 'ExtraValue1',
						label: 'Extra Value 1 Label',
						value: 'Extra Value 1 Value'
					},
					{
						key: 'ExtraValue2',
						label: 'Extra Value 2 Label',
						value: 'Extra Value 2 Value'
					},
					{
						key: 'ExtraValue3',
						label: 'Extra Value 3 Label',
						value: 'Extra Value 3 Value'
					}
				];

				state.canvas = $('<div></div>');
				fixture._handleSummaryResponse(JSON.stringify(response));

				expect(state.canvas.find('.app-logo span').text()).toEqual('ABOUT');
				expect(state.canvas.find('.summaryCaption').text()).toEqual('Some Interesting Description');

				var statLabels = ['Extra Value 1 Label', 'Extra Value 2 Label', 'Extra Value 3 Label'];
				var statValues = ['Extra Value 1 Value', 'Extra Value 2 Value', 'Extra Value 3 Value'];

				state.canvas.find('#summaryStats .caption').each(
					function(index) {
						var label = $(this).find('.summaryViewLabel').text();
						var value = $(this).find('.summaryViewValue').text();

						expect(label).toEqual(statLabels[index]);
						expect(value).toEqual(statValues[index]);
					}
				);
			});
		});
	}
);