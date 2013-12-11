/**
 * Copyright (c) 2013 Oculus Info Inc.
 * http://www.oculusinfo.com/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package influent.bitcoin.server.dataaccess;

import influent.idl.FL_DataAccess;
import influent.idl.FL_DateRange;
import influent.idl.FL_EntitySearch;
import influent.idl.FL_Link;
import influent.idl.FL_LinkTag;
import influent.idl.FL_Property;
import influent.idl.FL_PropertyTag;
import influent.idl.FL_SortBy;
import influent.idl.FL_TransactionResults;
import influent.idlhelper.LinkHelper;
import influent.idlhelper.PropertyHelper;
import influent.server.dataaccess.DataAccessHelper;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.dataaccess.DataViewDataAccess;
import influent.server.utilities.SQLConnectionPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.avro.AvroRemoteException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hopefully a better implementation of the Data Access
 * @author msavigny
 *
 */

public class BitcoinDataAccess extends DataViewDataAccess implements FL_DataAccess {
	
	private static Logger s_logger = LoggerFactory.getLogger(BitcoinDataAccess.class);

	public BitcoinDataAccess(
			SQLConnectionPool connectionPool,
			FL_EntitySearch search,
			DataNamespaceHandler namespaceHandler)
			throws ClassNotFoundException, SQLException {
		
		super(connectionPool, search, namespaceHandler);
	}
	

	public FL_TransactionResults getAllTransactions(
			List<String> entities,
			FL_LinkTag tag,
			FL_DateRange date,
			FL_SortBy sort,
			List<String> linkFilter,
			long start,
			long max) throws AvroRemoteException {
		
		if (entities.size() == 1) {
			return getAllTransactionsForEntity(entities.get(0), tag, date, sort, max, linkFilter);
		}
		
		long count = 0;
		final List<FL_Link> links = new ArrayList<FL_Link>();
				
		for (String id : entities) {
			FL_TransactionResults results = getAllTransactionsForEntity(id, tag, date, sort, max, linkFilter);

			count+= results.getTotal();
			links.addAll(results.getResults());
		}
		
		return FL_TransactionResults.newBuilder().setTotal(count).setResults(links).build();
	}
	
	private FL_TransactionResults getAllTransactionsForEntity(
			String id, FL_LinkTag tag,
			FL_DateRange date, FL_SortBy sort, long max,
			List<String> linkFilter) throws AvroRemoteException {
		
		if (id == null || id.isEmpty()) {
			return new FL_TransactionResults(0L, new ArrayList<FL_Link>(0));
		}
		
		// translate global to local ids, with namespace
		String schema = getNamespaceHandler().namespaceFromGlobalEntityId(id);
		id = getNamespaceHandler().localFromGlobalEntityId(id);
		
		Long total= 0L;
		
		try {
			Connection connection = _connectionPool.getConnection();
			Statement statement = connection.createStatement();
			
			String focusIds = "";
			if (linkFilter != null) {
				// we will never find links across schemas, so filter it down again.
				linkFilter = getNamespaceHandler().entitiesByNamespace(linkFilter).get(schema);
				
				if (linkFilter != null) {
					focusIds = DataAccessHelper.createNodeIdListFromCollection(linkFilter, true, false);
				} else {
					return new FL_TransactionResults(0L, new ArrayList<FL_Link>(0));
				}
			}
			
			String sql = "id,source_id,dest_id,dt,amount,usd";
			sql += ", resultcount = COUNT(*) OVER()"; // comment out to leave out full record count
			sql += " FROM Bitcoin.dbo.[bitcoin-20130410] ";
			
			if (linkFilter == null) {
				sql += "WHERE (source_id = '" + id + "' OR dest_id = '" + id + "') ";
			} else {
				sql += "WHERE ((source_id = '" + id + "' AND dest_id in ("+focusIds+")) OR (dest_id = '" + id + "' AND source_id in ("+focusIds+"))) ";				
			}
			
			if (date != null && date.getStartDate() != null) {
				DateTime start = DataAccessHelper.getStartDate(date);
				DateTime end = DataAccessHelper.getEndDate(date);
				
				sql += " AND dt BETWEEN '" + DataAccessHelper.format(start) + "' ";
				sql += "              AND '" + DataAccessHelper.format(end) + "' ";
			}
			
			sql += sort != null? " ORDER BY " + (sort == FL_SortBy.DATE ? "dt ASC" : "usd DESC"): "";
			
			sql = getNamespaceHandler().rowLimit(sql, max);

			long start = System.currentTimeMillis();
			s_logger.trace("execute: " + sql);
			
			ResultSet resultSet = statement.executeQuery(sql);
	
			s_logger.trace("complete in " + (System.currentTimeMillis() - start)/1000.0);
			
			List<FL_Link> records = new ArrayList<FL_Link>();
			
			while (resultSet.next()) {
				String trans_id = resultSet.getString(1);
				String source_id = resultSet.getString(2);
				String dest_id = resultSet.getString(3);
				Date dt = new java.util.Date(resultSet.getTimestamp(4).getTime());
				Double btc = resultSet.getDouble(5);
				Double usd = resultSet.getDouble(6);

				// comment out to leave out full record count
				if (total != null && total == 0L) {
					total = resultSet.getLong(7);
				}
				
				List<FL_Property> props = new ArrayList<FL_Property>();
				props.add(new PropertyHelper(FL_PropertyTag.ID, trans_id));
				props.add(new PropertyHelper("dt", "dt", dt, Collections.singletonList(FL_PropertyTag.DATE)));
				props.add(new PropertyHelper("amount", "amount", usd, Collections.singletonList(id.equals(source_id) ? FL_PropertyTag.OUTFLOWING : FL_PropertyTag.INFLOWING)));

				String comment = (btc + " BTC ") + ((id.equals(source_id)) ? ("to " + dest_id) : ("from " + source_id));
				props.add(new PropertyHelper("comment", "comment", comment, Collections.singletonList(FL_PropertyTag.ANNOTATION)));
				
				// globalize these for return
				source_id = getNamespaceHandler().globalFromLocalEntityId(schema, source_id);
				dest_id = getNamespaceHandler().globalFromLocalEntityId(schema, dest_id);
				
				records.add(new LinkHelper(FL_LinkTag.FINANCIAL, source_id, dest_id, props));
			}
			
			resultSet.close();
			statement.close();
			connection.close();
			
			if (total == null || total == 0) {
				total = Long.valueOf(records.size());
			}
			
			return new FL_TransactionResults(total, records);
			
		} catch (SQLException e) {
			throw new AvroRemoteException(e);
		} catch (ClassNotFoundException e) {
			throw new AvroRemoteException(e);
		}
	}
	
}
