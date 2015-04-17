/**
 * Copyright (c) 2013-2014 Oculus Info Inc. 
 * http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

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
			},
			/*
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
		rpcEndpoint : '%host%/${project.artifactId}/rpc',
		restEndpoint : '%host%/${project.artifactId}/rest'
	},

	/*
	 * Influent specific configuration
	 */
	'influent.config' : {
		useAuth : false,
		datasource : 'Walker',
		title : 'Walker',
		help : '/docs/user-guide/',
		about : '/docs/about',
		aboutFlow : '/docs/user-guide/investigate-flow/#add-accounts',
		dateRangeIntervals : {
			P14D : '2 weeks',
			P112D : '16 weeks',
			P1Y : '1 year',
			P16M : '16 months',
			P4Y : '4 years',
			P16Y : '16 years'
		},
		startingDateRange : 'P16M',
		defaultEndDate: new Date('Jan 1, 2011'),
		dateFormat: 'MMM D, YYYY',
		timeFormat: 'h:mma',
		defaultShowDetails : true,
		defaultGraphScale : 100,
		doubleEncodeSourceUncertainty : false,
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
		iconOrder : ['TEXT'],
		iconMap : {
			TEXT : {
				map : function(name,value) {

							switch (value) {
								case 'Business and Industry':
									return {
										title : 'Business and Industry',
										icon : {type: 'person', attributes: { role: 'business'}}
									};
								case 'Internet and Telecom':
									return {
										title : 'Internet and Telecom',
										icon : {type: 'person'}
									};
								case 'Law and Government':
									return {
										title : 'Law and Government',
										icon : {type: 'person', attributes: { role: 'political'}}
									};
								case 'Finance':
									return {
										title : 'Finance',
										icon : {type: 'ledger', attributes: { instrumenttype : 'cash' }}
									};
								case 'Health':
									return {
										title : 'Health',
										icon : {type: 'person', attributes: { role: 'healthcare'}}
									};
								case 'Career and Education':
									return {
										title : 'Career and Education',
										icon : {type: 'person', attributes: { role: 'academic'}}
									};
								case 'News and Media':
									return {
										title : 'News and Media',
										icon : {type: 'document', attributes: { mimetype : 'text/html' }}
									};
								case 'Arts and Entertainment':
									return {
										title : 'Arts and Entertainment',
										icon : {type: 'video'}
									};
								case 'Travel':
									return {
										title : 'Travel',
										icon : {type: 'place'}
									};
								case 'Shopping':
									return {
										title : 'Shopping',
										icon : {type: 'store'}
									};
								case 'Computer and Electronics':
									return {
										title : 'Computer and Electronics',
										icon : {type: 'document', attributes: { mimetype : 'application/powerpoint' }}
									};
								default:
									return {
										title : 'Account',
										icon : {type: 'person'}
									};
							}


				},
				distName : 'text-dist',
				limit : 4
			}
		}
	}
}
