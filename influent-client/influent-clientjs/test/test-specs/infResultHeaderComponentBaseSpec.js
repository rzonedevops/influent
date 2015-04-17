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
		'views/components/resultHeaderComponentBase',
		'lib/communication/accountsViewChannels'
	],
	function(
		fixture,
		accountsChannel
	) {

		var headerInfo ={
			'columns':[
				{'isImage':true,'columnWidth':'100px','orderAsc':false,'orderDesc':false,'property':{'friendlyText':'Image','typeMappings':{'lender':'image','loan':'image','partner':'image'},'searchableBy':'NONE','propertyType':'IMAGE','range':'SINGLETON','defaultTerm':false,'constraint':'FUZZY_PARTIAL_OPTIONAL','key':'image'},'numFormat':false},
				{'isImage':false,'columnWidth':'16.666666666666668%','orderAsc':false,'orderDesc':false,'property':{'friendlyText':'Name','typeMappings':{'lender':'lenders_name','loan':'loans_name','partner':'partners_name'},'searchableBy':'FREE_TEXT','propertyType':'STRING','range':'SINGLETON','defaultTerm':true,'constraint':'FUZZY_PARTIAL_OPTIONAL','key':'NAME'},'numFormat':false},
				{'isImage':false,'columnWidth':'16.666666666666668%','orderAsc':false,'orderDesc':false,'property':{'friendlyText':'ID','typeMappings':{'lender':'id','loan':'id','partner':'id'},'searchableBy':'DESCRIPTOR','propertyType':'STRING','range':'SINGLETON','defaultTerm':true,'constraint':'FUZZY_PARTIAL_OPTIONAL','key':'id'},'numFormat':false},
				{'isImage':false,'columnWidth':'16.666666666666668%','orderAsc':false,'orderDesc':false,'property':{'friendlyText':'Type','typeMappings':{'lender':'type','loan':'type','partner':'type'},'searchableBy':'NONE','propertyType':'STRING','range':'SINGLETON','defaultTerm':false,'constraint':'FUZZY_PARTIAL_OPTIONAL','key':'type'},'numFormat':false},
				{'isImage':false,'columnWidth':'16.666666666666668%','orderAsc':false,'orderDesc':false,'property':{'friendlyText':'Location','typeMappings':{'lender':'geo','loan':'geo','partner':'geo'},'searchableBy':'NONE','propertyType':'GEO','range':'SINGLETON','defaultTerm':false,'constraint':'FUZZY_PARTIAL_OPTIONAL','key':'geo'},'numFormat':false},
				{'isImage':false,'columnWidth':'16.666666666666668%','orderAsc':false,'orderDesc':false,'property':{'friendlyText':'Transactions','typeMappings':{'lender':'numTransactions','loan':'numTransactions','partner':'numTransactions'},'searchableBy':'NONE','propertyType':'INTEGER','range':'SINGLETON','defaultTerm':false,'constraint':'FUZZY_PARTIAL_OPTIONAL','key':'numTransactions'},'numFormat':true},
				{'isImage':false,'columnWidth':'16.666666666666668%','orderAsc':false,'orderDesc':false,'property':{'friendlyText':'Average (USD)','typeMappings':{'lender':'avgTransaction','loan':'avgTransaction','partner':'avgTransaction'},'searchableBy':'NONE','propertyType':'DOUBLE','range':'SINGLETON','defaultTerm':false,'constraint':'FUZZY_PARTIAL_OPTIONAL','key':'avgTransaction'},'numFormat':true}
			]
		};

		//--------------------------------------------------------------------------------------------------------------

		describe('resultHeaderComponentBase tests', function() {

			var headerContainer;
			var resultsContainer;

			beforeEach(function () {

				headerContainer = $('<div>');
				resultsContainer = $('<div>');

				fixture.setSearchResultsHeader(
					'myXfId',
					headerContainer,
					resultsContainer,
					headerInfo,
					7,
					'search string',
					accountsChannel.RESULT_SELECTION_CHANGE,
					accountsChannel.RESULT_SORT_ORDER_CHANGE
				);
			});

			//----------------------------------------------------------------------------------------------------------

			it('Ensure the resultComponentBase fixture is defined', function () {
				expect(fixture).toBeDefined();
			});

			//----------------------------------------------------------------------------------------------------------

			it('Ensure the addSelectionClickHandler is set up', function () {

				var toggle = headerContainer.find('.infSearchResultsSelectAll');
				expect(toggle).toExist();

				spyOn(aperture.pubsub, 'publish');

				spyOnEvent(toggle, 'click');

				toggle.find('input').prop('checked', 'checked');
				toggle.trigger('click');

				expect(aperture.pubsub.publish).toHaveBeenCalledWith(
					accountsChannel.RESULT_SELECTION_CHANGE,
					{
						xfId: 'myXfId',
						isSelected: toggle.find('input').prop('checked')
					}
				);

				toggle.find('input').removeProp('checked');
				toggle.trigger('click');

				expect(aperture.pubsub.publish).toHaveBeenCalledWith(
					accountsChannel.RESULT_SELECTION_CHANGE,
					{
						xfId: 'myXfId',
						isSelected: toggle.find('input').prop('checked')
					}
				);

				var column = headerContainer.find('.summaryColumnHeader');
				spyOnEvent(column, 'click');
				$(column[1]).trigger('click');

				expect(aperture.pubsub.publish).toHaveBeenCalledWith(
					accountsChannel.RESULT_SORT_ORDER_CHANGE,
					{
						searchString: 'search string ORDER:NAME'

					}
				);
			});
		});
	}
);