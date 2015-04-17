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

import influent.server.sql.SQLBuilder;
import influent.server.sql.SQLFilter;
import influent.server.sql.SQLFilterColumn;
import influent.server.sql.SQLFilterGroup;
import influent.server.sql.SQLFrom;
import influent.server.sql.SQLJoin;
import influent.server.sql.SQLBuilder.Helpers.LazyNamedParam;
import influent.server.sql.basesql.BaseSQLJoin.SQLJoinType;
import influent.server.sql.SQLSelect;

/**
 * 
 * @author cregnier
 *
 */
public abstract class BaseSQLBuilder implements SQLBuilder {

	@Override
	public SQLSelect select() {
		return new BaseSQLSelect(this);
	}

	@Override
	public SQLFrom from() {
		return new BaseSQLFrom();
	}

	@Override
	public SQLJoin innerJoin() {
		return new BaseSQLJoin(SQLJoinType.Inner);
	}

	@Override
	public SQLJoin leftJoin() {
		return new BaseSQLJoin(SQLJoinType.Left);
	}
	
	@Override
	public SQLJoin rightJoin() {
		return new BaseSQLJoin(SQLJoinType.Right);
	}
	
	@Override
	public SQLJoin fullJoin() {
		return new BaseSQLJoin(SQLJoinType.Full);
	}
	
	@Override
	public SQLFilterColumn filter() {
		return new BaseSQLFilterColumn();
	}

	@Override
	public SQLFilterGroup and(SQLFilter... filters) {
		SQLFilterGroup group = new BaseSQLFilterGroup(BaseSQLFilterGroup.FilterGroupType.AND);
		for (SQLFilter filter : filters) {
			if (filter != null)
				group.addFilter(filter);
		}
		return group;
	}

	@Override
	public SQLFilterGroup or(SQLFilter... filters) {
		SQLFilterGroup group = new BaseSQLFilterGroup(BaseSQLFilterGroup.FilterGroupType.OR);
		for (SQLFilter filter : filters) {
			if (filter != null)
				group.addFilter(filter);
		}
		return group;
	}

	@Override
	public SQLFilterGroup group(SQLFilter filter) {
		return (new BaseSQLFilterGroup(BaseSQLFilterGroup.FilterGroupType.GROUP)).addFilter(filter);
	}

	@Override
	public SQLFilterGroup not(SQLFilter filter) {
		return (new BaseSQLFilterGroup(BaseSQLFilterGroup.FilterGroupType.GROUP)).not().addFilter(filter);
	}

	@Override
	public LazyNamedParam lazyParam(String paramName) {
		return new LazyNamedParam(paramName);
	}	
}
