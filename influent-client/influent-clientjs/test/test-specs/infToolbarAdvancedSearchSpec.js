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
		'lib/advanced-search/infToolbarAdvancedSearch',
		'lib/advanced-search/infToolbarAdvancedMatchCriteria',
		'views/infAccountsView',
		'lib/communication/applicationChannels',
		'hbs!templates/viewToolbar/infToolbarAdvancedSearch',
		'lib/constants',
		'lib/advanced-search/infSearchParams'
	],
	function(
		infToolbarAdvancedSearch,
		infToolbarAdvancedMatchCriteria,
		accountsView,
		appChannel,
		infToolbarAdvancedSearchTemplate,
		constants,
		infSearchParams
		) {
		describe('infToolbarAdvancedSearch', function() {
			var initializationResponse = {
				'properties': [
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'id',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'ID',
						'sortable': true,
						'typeMappings': {
							'partner': 'id',
							'loan': 'id',
							'lender': 'id'
						}
					},
					{
						'defaultTerm': true,
						'range': 'SINGLETON',
						'freeTextIndexed': true,
						'friendlyText': 'Name',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'NAME',
						'sortable': true,
						'typeMappings': {
							'partner': 'partners_name',
							'loan': 'loans_name',
							'lender': 'lenders_name'
						},
						'tags': [
							'SHARED_IDENTIFIER'
						]
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Occupation',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'occupation',
						'sortable': true,
						'typeMappings': {
							'lender': 'lenders_occupation'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Teams',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'teams',
						'sortable': true,
						'typeMappings': {
							'lender': 'lender_teams'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Description',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'description',
						'sortable': true,
						'typeMappings': {
							'loan': 'loans_description_texts_en',
							'lender': 'lenders_loanBecause'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Country code',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'countryCode',
						'sortable': true,
						'typeMappings': {
							'loan': 'loans_location_countryCode',
							'lender': 'lenders_countryCode'
						},
						'tags': [
							'SHARED_IDENTIFIER'
						]
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Occupational info',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'occupationalInfo',
						'sortable': true,
						'typeMappings': {
							'lender': 'lenders_occupationalInfo'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Inviter ID',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'inviterId',
						'sortable': true,
						'typeMappings': {
							'lender': 'lenders_inviterId'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Country',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'country',
						'sortable': true,
						'typeMappings': {
							'loan': 'loans_location_country'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Loan status',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'loan_status',
						'sortable': true,
						'typeMappings': {
							'loan': 'loans_status'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Loan use',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'loan_use',
						'sortable': true,
						'typeMappings': {
							'loan': 'loans_use'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Activity',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'activity',
						'sortable': true,
						'typeMappings': {
							'loan': 'loans_activity'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Sector',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'sector',
						'sortable': true,
						'typeMappings': {
							'loan': 'loans_sector'
						},
						'tags': [
							'SHARED_IDENTIFIER'
						]
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Partner status',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'partner_status',
						'sortable': true,
						'typeMappings': {
							'partner': 'partners_status'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Due diligence type',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'due_diligence_type',
						'sortable': true,
						'typeMappings': {
							'partner': 'partners_dueDiligenceType'
						}
					}],
					'types': [{
						'exclusive': false,
						'friendlyText': 'Lender',
						'group': 'Individuals',
						'key': 'lender'
					},
					{
						'exclusive': false,
						'friendlyText': 'Loan',
						'group': 'Individuals',
						'key': 'loan'
					},
					{
						'exclusive': false,
						'friendlyText': 'Partner',
						'group': 'Organizations',
						'key': 'partner'
					}],
					'orderBy': [{
						'propertyKey': 'SCORE'
					}]
			};

			var asWidgetInst;
			var viewName = 'Accounts';

			beforeEach(function() {

				var container = $('<div/>');
				var searchDialog = $(infToolbarAdvancedSearchTemplate());
				var searchParams = infSearchParams.create(initializationResponse);
				var critieria = infToolbarAdvancedMatchCriteria.createInstance(viewName, searchDialog.find('#criteria-container'), searchParams, true);
				critieria.build();

				asWidgetInst = infToolbarAdvancedSearch.createInstance(viewName, container, searchParams);
				asWidgetInst.render();
				asWidgetInst.UIObjectState.matchCriteria = critieria;
			});

			//----------------------------------------------------------------------------------------------------------

			it('has testing enabled', function () {
				expect(constants.UNIT_TESTS_ENABLED).toBeTruthy();
			});

			//----------------------------------------------------------------------------------------------------------

			it('returns required modules', function() {
				expect(infToolbarAdvancedMatchCriteria).toBeDefined();
				expect(infToolbarAdvancedSearch).toBeDefined();
			});

			//----------------------------------------------------------------------------------------------------------

			it('creates and returns required instances', function() {
				expect(asWidgetInst).toBeDefined();
			});

			it('Ensure advance search preserve ORDER criteria', function () {
				var searchInputSearchQuery = 'NAME:"bob" ORDER:ID';		//search toolbar query
				asWidgetInst.onAdvancedSearchRequest(appChannel.ADVANCED_SEARCH_REQUEST, {input: searchInputSearchQuery, view: viewName});

				spyOn(aperture.pubsub, 'publish');

				asWidgetInst.onSearchAction();

				expect(aperture.pubsub.publish).toHaveBeenCalledWith(appChannel.FILTER_SEARCH_CHANGE_EVENT, {view: 'Accounts', input: 'NAME:"bob" TYPE:partner TYPE:loan TYPE:lender ORDER:ID'});
			});
		});
	}
);