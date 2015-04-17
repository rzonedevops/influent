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
import influent.server.sql.SQLJoin;
import influent.server.sql.SQLSelect;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 
 * @author cregnier
 *
 */
public class BaseSQLJoin implements SQLJoin {

	public static enum SQLJoinType {
		Inner,
		Left,
		Right,
		Full
	}
	
	protected static class JoinOnData {
		String onClause;
		
		public JoinOnData(String onClause) {
			this.onClause = onClause;
		}
	}
	
	private final SQLJoinType joinType;
	private String tableName;
	private String alias;
	private BaseSQLSelect fromQuery;
	
	
	//--------------------------------------------------------
	
	
	private List<JoinOnData> joinOns = Lists.newArrayList();
	
	
	//--------------------------------------------------------
	
	
	public BaseSQLJoin(SQLJoinType joinType) {
		this.joinType = joinType;
	}

	@Override
	public SQLJoin table(String name) {
		this.tableName = name;
		return this;
	}

	@Override
	public SQLJoin as(String alias) {
		this.alias = alias;
		return this;
	}

	@Override
	public SQLJoin fromQuery(SQLSelect fromQuery) {
		if (!(fromQuery instanceof BaseSQLSelect)) {
			throw new SQLBuilderException("Unexpected select query type: " + fromQuery.getClass() + ". Should be " + BaseSQLSelect.class);
		}
		this.fromQuery = (BaseSQLSelect)fromQuery;
		return this;
	}
	
	@Override
	public SQLJoin on(String onClause) {
		joinOns.add(new JoinOnData(onClause));
		return this;
	}

	protected SQLJoinType getJoinType() {
		return joinType;
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
	
	
	protected List<JoinOnData> getJoinOns() {
		return joinOns;
	}
}
