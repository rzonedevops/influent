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
		'lib/communication/applicationChannels',
		'lib/constants',
		'lib/toolbar/infToolbarSearch',
		'lib/advanced-search/infSearchParams',
		'lib/extern/OpenAjaxUnmanagedHub'
	],

	function(
		appChannel,
		constants,
		infToolbarSearch,
		infSearchParams
	) {

		describe('infToolbarSearch adds a search widget to a view toolbar', function() {

			var initializationResponse = {
				'properties': [
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'id',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'id',
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
						'key': 'name',
						'typeMappings': {
							'partner': 'partners_name',
							'loan': 'loans_name',
							'lender': 'lenders_name'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Occupation',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'occupation',
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
						'typeMappings': {
							'loan': 'loans_location_countryCode',
							'lender': 'lenders_countryCode'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Occupational info',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'occupationalInfo',
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
						'typeMappings': {
							'loan': 'loans_sector'
						}
					},
					{
						'defaultTerm': false,
						'range': 'SINGLETON',
						'freeTextIndexed': false,
						'friendlyText': 'Partner status',
						'constraint': 'FUZZY_PARTIAL_OPTIONAL',
						'key': 'partner_status',
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
						'typeMappings': {
							'partner': 'partners_dueDiligenceType'
						}
					}],
				'types': [
					{
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
					}
				]
			};
			var widgetInst = null;
			var viewName = 'TEST_VIEW';
			var canvas = $('<div><div class="row"></div></div>');

			it('Has testing enabled', function () {
				expect(constants.UNIT_TESTS_ENABLED).toBeTruthy();
			});

			it('Returns a module', function() {
				expect(infToolbarSearch).toBeDefined();
			});

			it('Creates and returns an instance', function() {
				var spec = {
					searchParams : infSearchParams.create(initializationResponse)
				};
				widgetInst = infToolbarSearch.createInstance(viewName, canvas, spec);
				expect(widgetInst).toBeDefined();
			});


			it ('Renders to a div', function() {
				widgetInst.render();
			});

			it('Responds to search input and notifies subscribers', function() {

				// Todo - move this out to individual widget tests?
				var searchChangeCallback = jasmine.createSpy();

				var subTokens = {};
				subTokens[appChannel.FILTER_SEARCH_CHANGE_EVENT] = aperture.pubsub.subscribe(appChannel.FILTER_SEARCH_CHANGE_EVENT, searchChangeCallback);

				aperture.pubsub.publish(appChannel.FILTER_SEARCH_CHANGE_REQUEST, {
					view: viewName,
					input: 'test'
				});

				expect(searchChangeCallback).toHaveBeenCalledWith(appChannel.FILTER_SEARCH_CHANGE_EVENT,	{
					view: viewName,
					input: 'test'
				}, undefined, undefined);
			});
		});
	}
);