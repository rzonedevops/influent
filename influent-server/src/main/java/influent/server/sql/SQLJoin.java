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

/**
 * Creates a SQL JOIN statement and binds two tables together using a given ON
 * clause. This is very similar to a {@link SQLFrom} clause.<br>
 * Selecting the different types of join statements is done by using the
 * {@link SQLBuilder}.
 * @author cregnier
 *
 */
public interface SQLJoin {

	/**
	 * Sets the name of joined table to select results from.<br>
	 * This cannot be set if {@link #fromQuery(SQLSelect)} has also been set.
	 * @param name
	 *   The name of an existing table in the database
	 * @return
	 */
	SQLJoin table(String name);
	
	/**
	 * Adds an alias for the joined table.
	 * @param alias
	 *   The name of the alias that will be used in the SQL statement.
	 * @return
	 */
	SQLJoin as(String alias);
	
	/**
	 * Sets a subquery that should be used to select joined results from.<br>
	 * This cannot be set if {@link #table(String)} has also been set.
	 * @param fromQuery
	 *   A subquery to select results from.
	 * @return
	 */
	SQLJoin fromQuery(SQLSelect fromQuery);
	
	/**
	 * Specifies the parameters to join the two tables on.
	 * @param onClause
	 *   A string that specifies which table columns to join together.
	 * @return
	 */
	SQLJoin on(String onClause);
	
}
