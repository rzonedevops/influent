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
package influent.server.sql;

import influent.server.sql.SQLBuilder.Helpers.LazyNamedParam;

import java.util.List;

/**
 * A SQL WHERE filter that represents a single column.

 * @author cregnier
 *
 */
public interface SQLFilterColumn extends SQLFilter {

	/**
	 * Sets the name of the column that should be used in the filter
	 * @param name
	 *   A column from the selected table
	 * @return
	 */
	SQLFilterColumn column(String name);
	
	/**
	 * Specifies a value to do an '=' test on the selected column.
	 * @param val
	 *   The value to compare to. A {@link LazyNamedParam} can also be used
	 *   here, and will allow the value to be replaced later.
	 * @return
	 */
	SQLFilterColumn eq(Object val);

	/**
	 * Specifies a value to do a '!=' test on the selected column.
	 * @param val
	 *   The value to compare to. A {@link LazyNamedParam} can also be used
	 *   here, and will allow the value to be replaced later.
	 * @return
	 */
	SQLFilterColumn notEq(Object val);
	
	/**
	 * Specifies a value to do a '<' test on the selected column.
	 * @param val
	 *   The value to compare to. A {@link LazyNamedParam} can also be used
	 *   here, and will allow the value to be replaced later.
	 * @return
	 */
	SQLFilterColumn lessThan(Object val);
	
	/**
	 * Specifies a value to do a '<=' test on the selected column.
	 * @param val
	 *   The value to compare to. A {@link LazyNamedParam} can also be used
	 *   here, and will allow the value to be replaced later.
	 * @return
	 */
	SQLFilterColumn lessThanEq(Object val);
	
	/**
	 * Specifies a value to do a '>' test on the selected column.
	 * @param val
	 *   The value to compare to. A {@link LazyNamedParam} can also be used
	 *   here, and will allow the value to be replaced later.
	 * @return
	 */
	SQLFilterColumn greaterThan(Object val);
	
	/**
	 * Specifies a value to do a '>=' test on the selected column.
	 * @param val
	 *   The value to compare to. A {@link LazyNamedParam} can also be used
	 *   here, and will allow the value to be replaced later.
	 * @return
	 */
	SQLFilterColumn greaterThanEq(Object val);
	
	/**
	 * Sets the filter to use a 'LIKE' filter with the passed in string value or regex. 
	 * @param regex
	 *   The value to use in the SQL statement. A {@link LazyNamedParam} can also be used
	 *   here, and will allow the value to be replaced later.
	 * @return
	 */
	SQLFilterColumn like(Object regex);
	
	/**
	 * Negates the filter and adds a NOT in front of things.
	 * @return
	 */
	SQLFilterColumn not();
	
	/**
	 * Sets the filter to use an 'IN' filter with the passed set of values. 
	 * @param val
	 *   A list of values that will be stringified to be used as parameters to the 'IN' clause.
	 *   {@link LazyNamedParam}s can also be used here, and will allow values to be replaced later.
	 * @return
	 */
	SQLFilterColumn in(List<Object> vals);
	
	/**
	 * Sets the filter to use a 'BETWEEN' filter. 
	 * @param val1
	 *   The lower bounds value for the column.
	 *   {@link LazyNamedParam}s can also be used here, and will allow values to be replaced later.
	 * @param val2
	 *   The upper bounds value for the column.
	 *   {@link LazyNamedParam}s can also be used here, and will allow values to be replaced later.
	 * @return
	 */
	SQLFilterColumn between(Object val1, Object val2);
}
