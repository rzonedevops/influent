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
		/**
		 *
		 */
		RESULT_VISIBILITY_CHANGE: 'accounts-result-visibility-change',

		/**
		 * data {
		 *      xfId: (string)
		 *      isSelected: (boolean)
		 * }
		 */
		RESULT_SELECTION_CHANGE: 'accounts-result-selection-change',

		/**
		 * data {
		 *      xfId: (string)
		 *      container: (object)
		 * }
		 */
		RESULT_ENTITY_FULL_DETAILS_SHOW: 'accounts-result-entity-full-details-show',

		/**
		 * data {
		 *      xfId: (string)
		 *      isExpanded: (boolean)
		 * }
		 */
		RESULT_CLUSTER_VISIBILITY_CHANGE: 'accounts-result-cluster-visibility-change',

		/**
		 * data {
		 *		searchString: (string)
		 * }
		 */
		RESULT_SORT_ORDER_CHANGE: 'accounts-result-sort-order-change'
	}
);
