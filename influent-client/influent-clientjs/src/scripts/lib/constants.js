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
	{
		MODULE_NAMES : {
			ACCOUNTS_SEARCH_RESULT : 'xfAccountsSearchResult',
			CLUSTER_BASE : 'xfClusterBase',
			COLUMN : 'xfColumn',
			ENTITY : 'xfEntity',
			ENTITY_RESULT : 'xfEntityResult',
			ENTITY_RESULT_CLUSTER : 'xfEntityResultCluster',
			FILE : 'xfFile',
			IMMUTABLE_CLUSTER : 'xfImmutableCluster',
			LINK : 'xfLink',
			MATCH : 'xfMatch',
			MUTABLE_CLUSTER : 'xfMutableCluster',
			RESULT_BASE : 'xfResultBase',
			RESULT_CLUSTER_BASE : 'xfResultClusterBase',
			SEARCH_RESULT_BASE : 'xfSearchResultBase',
			SUMMARY_CLUSTER : 'xfSummaryCluster',
			TRANSACTION_RESULT : 'xfTransactionResult',
			TRANSACTION_RESULT_CLUSTER : 'xfTransactionResultCluster',
			TRANSACTIONS_SEARCH_RESULT : 'xfTransactionsSearchResult',
			WORKSPACE : 'xfWorkspace'
		},
		ACCOUNT_TYPES : {
			ACCOUNT_OWNER : 'account_owner',
			CLUSTER : 'cluster',
			CLUSTER_SUMMARY : 'cluster_summary',
			ENTITY : 'entity',
			ENTITY_CLUSTER : 'entity_cluster',
			FILE : 'file'
		},
		AJAX_SPINNER_FILE : 'img/ajax-loader.gif',
		AJAX_SPINNER_BG	: 'url("img/ajax-loader.gif") no-repeat center center',
		AJAX_SPINNER_LARGE_BG	: 'url("img/ajax-loader-large.gif") no-repeat center center',
		SORT_FUNCTION : {
			INCOMING : 'incoming',
			OUTGOING : 'outgoing',
			BOTH : 'both'
		},
		VIEWS : {
			SUMMARY : {
				NAME : 'Summary',
				ICON : 'glyphicon glyphicon-info-sign'
			},
			ACCOUNTS : {
				NAME : 'Accounts',
				ICON : 'glyphicon glyphicon-user'
			},
			TRANSACTIONS : {
				NAME : 'Transactions',
				ICON : 'glyphicon glyphicon-list'
			},
			FLOW : {
				NAME : 'Flow',
				ICON : 'glyphicon glyphicon-random'
			}
		},
		UNIT_TESTS_ENABLED : aperture.config.get()['influent.config']['unitTestsEnabled'] || false,
		DATE_FORMAT: aperture.config.get()['influent.config'].dateFormat,
		TIME_FORMAT: aperture.config.get()['influent.config'].timeFormat,
		QUERY_DATE_FORMAT: 'YYYY-MM-DD',
		BOOTSTRAP_DATE_FORMAT: aperture.config.get()['influent.config'].dateFormat
			.replace('YYYY', 'yyyy')
			.replace('YY', 'yy')
			.replace('MMMM', 'MM')
			.replace('MMM', 'M')
			.replace('D', 'd')
	}
);
