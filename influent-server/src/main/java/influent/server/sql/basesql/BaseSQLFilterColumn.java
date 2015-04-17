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

import java.util.List;

import influent.server.sql.SQLFilterColumn;

/**
 * 
 * @author cregnier
 *
 */
public class BaseSQLFilterColumn implements SQLFilterColumn {

	protected enum OpType {
		Eq,
		NotEq,
		LessThan,
		LessThanEq,
		GreaterThan,
		GreaterThanEq,
		Like,
		In,
		Betweeen
	}
	
	//------------------------------------------------------

	
	private String name;
	private boolean negated = false;
	private OpType opType;
	private Object val1;
	private Object val2;
	private Object regex;
	private List<Object> inVals;
	
	
	//------------------------------------------------------
	
	
	@Override
	public SQLFilterColumn column(String name) {
		this.name = name;
		return this;
	}

	@Override
	public SQLFilterColumn eq(Object val) {
		opType = OpType.Eq;
		val1 = val;
		return this;
	}

	@Override
	public SQLFilterColumn notEq(Object val) {
		opType = OpType.NotEq;
		val1 = val;
		return this;
	}

	@Override
	public SQLFilterColumn lessThan(Object val) {
		opType = OpType.LessThan;
		val1 = val;
		return this;
	}

	@Override
	public SQLFilterColumn lessThanEq(Object val) {
		opType = OpType.LessThanEq;
		val1 = val;
		return this;
	}

	@Override
	public SQLFilterColumn greaterThan(Object val) {
		opType = OpType.GreaterThan;
		val1 = val;
		return this;
	}

	@Override
	public SQLFilterColumn greaterThanEq(Object val) {
		opType = OpType.GreaterThanEq;
		val1 = val;
		return this;
	}

	@Override
	public SQLFilterColumn like(Object regex) {
		opType = OpType.Like;
		this.regex = regex;
		return this;
	}

	@Override
	public SQLFilterColumn not() {
		negated = true;
		return this;
	}

	@Override
	public SQLFilterColumn in(List<Object> vals) {
		opType = OpType.In;
		this.inVals = vals;
		return this;
	}

	@Override
	public SQLFilterColumn between(Object val1, Object val2) {
		opType = OpType.Betweeen;
		this.val1 = val1;
		this.val2 = val2;
		return this;
	}

	
	protected String getName() {
		return name;
	}
	
	protected boolean isNegated() {
		return negated;
	}
	
	protected OpType getOpType() {
		return opType;
	}
	
	protected Object getVal1() {
		return val1;
	}
	
	protected Object getVal2() {
		return val2;
	}
	
	protected Object getLikeRegex() {
		return regex;
	}
	
	protected List<Object> getInValues() {
		return inVals;
	}
}
