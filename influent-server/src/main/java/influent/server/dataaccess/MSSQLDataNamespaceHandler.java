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
package influent.server.dataaccess;

import org.joda.time.DateTime;


/**
 * @author djonker
 *
 */
public class MSSQLDataNamespaceHandler extends AbstractDataNamespaceHandler {

	
	/**
	 * Constructs a handler which uses standard table names.
	 */
	public MSSQLDataNamespaceHandler() {
		super();
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#createTempTable(java.lang.String, java.lang.String)
	 */
	@Override
	public String createTempTable(String tableName, String spec) {
		return " CREATE TABLE "+ tempTableName(tableName) + " " + spec + "; ";
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#tempTableName(java.lang.String)
	 */
	@Override
	public String tempTableName(String tableName) {
		String name = tableName;
		if (!tableName.startsWith("##")) {
			name = "##" + tableName;
		}
		return name.replaceAll("-", "_");
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#rowLimit(java.lang.String, long)
	 */
	@Override
	public String rowLimit(String selectBody, long limit) {
		if (limit > 0) {
			return "select top " + limit + " " + selectBody;
		}
		return "select " + selectBody;
	}

	


	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataViewNamespaceHandler#formatDate(org.joda.time.DateTime)
	 */
	@Override
	public String formatDate(DateTime date) {
		return DataAccessHelper.format(date);
	}




	/* (non-Javadoc)
	 * @see influent.server.dataaccess.AbstractDataNamespaceHandler#idToBinaryFromHex(java.lang.String)
	 */
	@Override
	protected String idToBinaryFromHex(String id) {
		return "CONVERT(VARBINARY(MAX), '" + id + "', 2)";
	}




	@Override
	protected String columnToHex(String columnName) {
		return "CAST(" + columnName + " AS VARCHAR(MAX)) as " + columnName;
	}
}
