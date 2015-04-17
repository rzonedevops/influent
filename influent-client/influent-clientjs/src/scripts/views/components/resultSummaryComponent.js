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
		'hbs!templates/searchResults/resultsSummary'
	],
	function(
		resultsSummaryTemplate
	) {
		var state = {
			template : null,
			results : false,
			sortedBy : null,
			sumVisible : 0,
			sumTotal : 0
		};

		//--------------------------------------------------------------------------------------------------------------

		var _reset = function(container, summaryData) {
			container.empty();

			state.results = summaryData.results;
			state.sortedBy = summaryData.sortedBy;
			state.sumVisible = summaryData.sumVisible;
			state.sumTotal = summaryData.sumTotal;

			state.template = resultsSummaryTemplate(summaryData);

			container.append(state.template);
		};

		//--------------------------------------------------------------------------------------------------------------

		var _refresh = function(container, summaryData) {

			if (state.results !== summaryData.results) {
				_reset(container, summaryData);
			} else {
				if (state.sumVisible !== summaryData.sumVisible) {
					state.sumVisible = summaryData.sumVisible;
					$('.infSearchResultSumVisibleCount').text(state.sumVisible);
				}

				if (state.sumTotal !== summaryData.sumTotal) {
					state.sumTotal = summaryData.sumTotal;
					$('.infSearchResultSumTotalCount').text(state.sumTotal);
				}
			}
		};

		//--------------------------------------------------------------------------------------------------------------

		return {
			setSearchSummaryStats : _reset,
			updateSearchSummaryStats : _refresh
		};
	}
);