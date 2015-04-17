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
package influent.server.sql.basesql;

import influent.server.sql.SQLBuilderException;
import influent.server.sql.SQLFrom;
import influent.server.sql.SQLSelect;

/**
 * 
 * @author cregnier
 *
 */
public class BaseSQLFrom implements SQLFrom {

	private String tableName;
	private String alias;
	private BaseSQLSelect fromQuery;
	
	
	//--------------------------------------------------------
	

	@Override
	public SQLFrom table(String name) {
		this.tableName = name;
		return this;
	}

	@Override
	public SQLFrom as(String alias) {
		this.alias = alias;
		return this;
	}

	@Override
	public SQLFrom fromQuery(SQLSelect fromQuery) {
		if (!(fromQuery instanceof BaseSQLSelect)) {
			throw new SQLBuilderException("Unexpected SQLSelect class: " + fromQuery.getClass() + ". Should be " + BaseSQLSelect.class);
		}
		this.fromQuery = (BaseSQLSelect)fromQuery;
		return this;
	}
	
	protected String getTableName() {
		return tableName;
	}
	
	protected String getAlias() {
		return alias;
	}
	
	protected BaseSQLSelect getSelectQuery() {
		return fromQuery;
	}
	
}
