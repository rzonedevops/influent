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
 */package influent.server.sql.mssql;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import influent.server.sql.basesql.BaseSQLBuilder;

public class MSSQLBuilder extends BaseSQLBuilder {

    public final static String TYPE_KEY = "mssql";

	@Override
	public String escape(String name) {
		return (name.charAt(0) == '[' && name.charAt(name.length() - 1) == ']')? name : "[" + name + "]";
	}
	
	@Override
	public String unescape(String name) {
		return (name.charAt(0) == '[' && name.charAt(name.length() - 1) == ']')? name.substring(1, name.length() - 1) : name;
	}
	
	@Override
	public Date getDate(Object date) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse((String) date);
		} catch(Exception e) {
			System.out.println(e);
			return null;
		}
	}
}
