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
{
	/*
	 * A default log configuration, which appends info to the console but pops up notifications.
	 */
	'aperture.log' : {
		level : 'log',
		logWindowErrors : {
			log : true,
			preventDefault : true
		},
		appenders : {
			consoleAppender : {
				level : 'info'
			},
			notifyAppender : {
				level : 'error'
			}/*,
			draperAppender : {
				address : 'http://10.1.90.46:1337',
				webworker : 'scripts/lib/extern/draper.activity_worker-2.1.1.js',
			}
			*/
		}
	},
	
	/*
	 * The endpoint locations for Aperture services accessed through the io interface
	 */
	'aperture.io' : {
		rpcEndpoint : '%host%/influent/rpc',
		restEndpoint : '%host%/influent/rest'
	},
	
	/*
	 * Influent specific configuration
	 */
	'influent.config' : {
		useAuth : false,
		datasource : 'Influent Example',
		title : 'Influent',
		help : 'http://community.influent.org/docs/user-guide/',
		about : 'http://community.influent.org/docs/about/',
		aboutFlow : 'http://community.influent.org/docs/user-guide/how-to/investigate-flow/#add-accounts',
		dateRangeIntervals : {
			P14D : '2 weeks',
			P112D : '16 weeks',
			P1Y : '1 year',
			P16M : '16 months',
			P4Y : '4 years',
			P16Y : '16 years'
		},
		startingDateRange : 'P1Y',
		defaultEndDate: new Date(2015, 3, 1),
		dateFormat: 'MMM D, YYYY',
		timeFormat: 'h:mma',
		defaultShowDetails : true,
		defaultGraphScale : 1000,
		doubleEncodeSourceUncertainty : true,
		maxSearchResults : 50,
		searchResultsPerPage : 12,
		enableAdvancedSearchMatchType : true,
		sessionTimeoutInMinutes : 24*60,
		sessionRestorationEnabled : false,
		promptForDetailsInFooter : false,
		usePatternSearchInFlowView : false,
		objectDegreeWarningCount : 1000,
		objectDegreeLimitCount: 10000,
		enableAdvancedSearchMatchType : true,
		enableAdvancedSearchWeightedTerms : true,
		advancedSearchFuzzyLevels : {
			'is very like' : 0.9,
			'is like': 0.5,
			'is vaguely like': 0.1
		},
		iconOrder : ['ENTITY_TYPE', 'STAT', 'COUNTRY_CODE'],
		iconMap : {
			ENTITY_TYPE : {
				map: function(name, value) {
					switch (value) {
						case 'Personal':
							return {
								title: 'Personal',
								icon : {type: 'person'}
							};
						case 'Commercial':
							return {
								title: 'Commercial',
								icon : {type: 'person', attributes: { role: 'business'}}
							};
						default:
					}
				},
				limit: 1
			},
			COUNTRY_CODE : {
				map: function(name, value) {
					if (value) {
						return {
							title: value,
							icon: {type: 'Place', attributes: {code: value, country: 'US'}}
						};
					}
				},
				limit: 2
			},
			STAT : {
				map: function(name, value) {
					switch (value) {
						case 'Savings':
							return {
								title: 'Savings',
								icon : {type: 'ledger', attributes: {instrumenttype: 'deposit'}}
							};
						case 'Credit Card':
							return {
								title: 'Credit',
								icon : {type: 'ledger', attributes: {instrumenttype: 'creditcard'}}
							};
						case 'Checking':
							return {
								title: 'Checking',
								icon : {type: 'account'}
							};
						case 'Cash':
							return {
								title: 'Cash',
								icon : {type: 'ledger', attributes: {instrumenttype: 'cash'}}
							};
						case 'Line of Credit':
							return {
								title: 'Line of Credit',
								icon : {type: 'ledger', attributes: {instrumenttype: 'loan'}}
							};
						default:
					}
				},
				limit: 1
			}
		}
	}
}
