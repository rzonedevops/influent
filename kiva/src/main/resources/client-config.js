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
		rpcEndpoint : '%host%/${project.artifactId}/rpc',
		restEndpoint : '%host%/${project.artifactId}/rest'
	},

	'influent.plugins' : {
		details : [
			{
				/*
				 * the pathname of a require module to load on initialisation
				 * which will be made available to the plugin functions via this.module
				 */
				module: 'extensions',

				searchResultPrompt: function(container, result, callback) {
					return this.module.promptForDetails(container, result, callback);
				}
			},
			{
				/*
				 * the pathname of a require module to load on initialisation
				 * which will be made available to the plugin functions via this.module
				 */
				module: 'extensions',

				/*
				 * Returns the content to be displayed on the footer overlay when an entity with the
				 * 'promptForDetails' tag is selected. The user is prompted before fetching the full
				 * details of the entity.
				 *
				 * @entityData details of the selected object, which include:
				 * {
				dataId: the fully qualified id of the data item in the form type.namespace.id,
				type: the type of the selected object:
				'cluster', 'cluster_summary', 'account_owner', 'entity',
				label: the object's label text
				numMembers: the number of members in the object
				}
				 *
				 * @this.module a optional require module loaded for this plugin, by name
				 *
				 * @returns html to be displayed in the footer above an 'OK' button
				 */
				footerPrompt: function (entityData) {
					return this.module.extendFooterPrompt(entityData);
				}
			}
		]
	},
	
	/*
	 * Influent specific configuration
	 */
	'influent.config' : {
		useAuth : true,
		datasource : 'Kiva',
		title : 'Kiva',
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
		defaultEndDate: new Date(2011, 6, 1),
		dateFormat: 'MMM D, YYYY',
		timeFormat: 'h:mma',
		defaultShowDetails : true,
		defaultGraphScale : 100,
		doubleEncodeSourceUncertainty : true,
		maxSearchResults : 50,
		searchResultsPerPage : 12,
		sessionTimeoutInMinutes : 24*60,
		sessionRestorationEnabled : false,
		promptForDetailsInFooter : true,
		usePatternSearchInFlowView : true,
		objectDegreeWarningCount : 1000,
		objectDegreeLimitCount: 10000,
		enableAdvancedSearchMatchType : true,
		enableAdvancedSearchWeightedTerms : true,
		advancedSearchFuzzyLevels : {
			'is very like' : 0.9,
			'is like': 0.5,
			'is vaguely like': 0.1
		},
		iconOrder : ['TYPE', 'GEO', 'STATUS', 'WARNING'],
		iconMap : {
			TYPE : {
				map : function(name, value) {
					switch (value) {
						case 'lender':
							return {
								title: 'lender',
								icon: {type: 'Person', attributes: {role: 'business'}}
							};
						case 'partner':
							return {
								title: 'partner',
								icon: {type: 'Organization', attributes: {role: 'business'}}
							};
						case 'loan':
							return {
								title: 'loan',
								icon: {type: 'Person'}
							};
					}
				},
				limit : 1
			},
			GEO : {
				map : function(name, value) {
					if (value && value.cc) {
						return {
							title: value.text || value.cc,
							icon: {type: 'Place', attributes: {code: value.cc, country: value.cc}}
						};
					}
				},
				limit: 3
			},
			STATUS : {
				map : function(name, value) {
					switch (value) {
						case 'defaulted':
							return {
								title: 'defaulted',
								icon: {type: 'stamp', attributes: {text: 'default'}}
							};
						case 'closed':
							return {
								title: 'closed',
								icon: {type: 'stamp', attributes: {text: 'closed'}}
							};
					}
				},
				limit: 1
			},
			WARNING : {
				map : function(name, value) {
					switch (name) {
						case 'PartnerDelinquencyRate':
							if (value >= 5.0) {
								return {
									title: 'high delinquency rate',
									icon: {type: 'warning'}
								};
							}
						case 'PartnerDefaultRate':
							if (value >= 1.0) {
								return {
									title: 'high default rate',
									icon: {type: 'warning'}
								};
							}
					}
				}
			}
		}
	}
}