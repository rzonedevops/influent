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

package influent.kiva.server.dataaccess;

import com.google.inject.Inject;

import influent.idl.*;
import influent.idlhelper.EntityHelper;
import influent.midtier.kiva.data.KivaTypes;
import influent.server.dataaccess.DataAccessHelper;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.dataaccess.DataViewDataAccess;
import influent.server.utilities.SQLConnectionPool;
import influent.server.utilities.InfluentId;

import org.apache.avro.AvroRemoteException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oculus.aperture.spi.common.Properties;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Hopefully a better implementation of the Kiva Data Access
 * @author msavigny
 *
 */

public class KivaDataAccess extends DataViewDataAccess implements FL_DataAccess {

	public static final String s_kivaTypeKey = "kiva_type";
	private static Logger s_logger = LoggerFactory.getLogger(KivaDataAccess.class);

	// --- Brokers ---
	public static final String PARTNER_BROKERS 		= "PartnerBrokers";
	
	private final static boolean USE_PREPARED_STATEMENTS = false;



	@Inject	
	public KivaDataAccess(
		Properties config,
		SQLConnectionPool connectionPool,
		FL_EntitySearch search,
		DataNamespaceHandler namespaceHandler
	) throws ClassNotFoundException, SQLException, JSONException {
		super(config, connectionPool, search, namespaceHandler);
	}




	@Override
	public Map<String, List<FL_Entity>> getAccounts(
			List<String> entities
		) throws AvroRemoteException {
		
		if (USE_PREPARED_STATEMENTS) {
			return getAccountsPrepared(entities);
		} else {
			return getAccountsNoPrepared(entities);
		}
	}




	public Map<String, List<FL_Entity>> getAccountsNoPrepared(
			List<String> entities
	) throws AvroRemoteException {
		final List<String> ns_entities = InfluentId.typedFromInfluentIds(entities);
		Map<String, List<FL_Entity>> map = new HashMap<String, List<FL_Entity>>();

		if (ns_entities.isEmpty()) return map;
		Connection connection = null;
		try {
			connection = _connectionPool.getConnection();
			Statement stmt = connection.createStatement();

			String partnerBrokersTable = _applicationConfiguration.getTable("PartnerBrokers", "PartnerBrokers");
			String brokerColumn = _applicationConfiguration.getColumn("PartnerBrokers", "BrokerEntityId");
			String partnerColumn = _applicationConfiguration.getColumn("PartnerBrokers", "PartnerEntityId");

			// lookup partner accounts for partner entities
			String sql = "SELECT " +
				brokerColumn + ", " +
				partnerColumn + " " +
				"FROM " + partnerBrokersTable + " " +
				"WHERE " + partnerColumn + " IN " + DataAccessHelper.createInClause(ns_entities);

			List<String> accounts = new LinkedList<String>();

			if (stmt.execute(sql)) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					String id = rs.getString(brokerColumn);
					String accountId = InfluentId.ACCOUNT + "." + id;
					accounts.add(accountId);
				}
			}

			for (FL_Entity entity : getEntities(accounts, FL_LevelOfDetail.SUMMARY) ) {
				String ownerId = InfluentId.ACCOUNT_OWNER + "." + (String)EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.ACCOUNT_OWNER).getValue();

				if (!map.containsKey(ownerId)) {
					map.put(ownerId, new LinkedList<FL_Entity>());
				}
				map.get(ownerId).add(entity);
			}

			stmt.close();
			connection.close();
		}
		catch (Exception e) {
			throw new AvroRemoteException(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return map;
	}




	public Map<String, List<FL_Entity>> getAccountsPrepared(
		List<String> entities
	) throws AvroRemoteException {

		final List<String> ns_entities = InfluentId.nativeFromInfluentIds(entities);
		Map<String, List<FL_Entity>> map = new HashMap<String, List<FL_Entity>>();
		
		if (ns_entities.isEmpty()) return map;
		Connection connection = null;
		try {
			List<String> accounts = new LinkedList<String>();
			
			connection = _connectionPool.getConnection();
			
			String partnerBrokersTable = _applicationConfiguration.getTable("PartnerBrokers", "PartnerBrokers");
			String brokerColumn = _applicationConfiguration.getColumn("PartnerBrokers", "BrokerEntityId");
			String partnerColumn = _applicationConfiguration.getColumn("PartnerBrokers", "PartnerEntityId");
			
			String preparedStatementString = buildPreparedStatementForGetAccounts(
				ns_entities.size(),
				partnerBrokersTable,
				brokerColumn,
				partnerColumn
			);
			PreparedStatement stmt = connection.prepareStatement(preparedStatementString);
			
			int index = 1;
			
			for (int i = 0; i < ns_entities.size(); i++) {
				stmt.setString(index++, getNamespaceHandler().toSQLId(ns_entities.get(i), null));
			}

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String id = rs.getString(brokerColumn);
				String accountId = InfluentId.ACCOUNT + "." + id;
				accounts.add(accountId);
			}

			
			for (FL_Entity entity : getEntities(accounts, FL_LevelOfDetail.SUMMARY) ) {
				String ownerId = InfluentId.ACCOUNT_OWNER + "." + (String)EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.ACCOUNT_OWNER).getValue();

				if (!map.containsKey(ownerId)) {
					map.put(ownerId, new LinkedList<FL_Entity>());
				}
				map.get(ownerId).add(entity);
			}
			
			stmt.close();
			connection.close();
		}
		catch (Exception e) {
			throw new AvroRemoteException(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
	
	
	
	
	private String buildPreparedStatementForGetAccounts(
		int numIds,
		String partnerBrokersTable,
	    String brokerColumn,
	    String partnerColumn
	) {
		if (numIds < 1 || 
			partnerBrokersTable == null
		) {
			s_logger.error("buildPreparedStatementForGetAccounts: Invalid parameter");
			return null;
		}

		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT " + brokerColumn + ", " + partnerColumn + " ");
		sb.append("FROM " + partnerBrokersTable + " ");
		sb.append("WHERE " + partnerColumn + " IN (");
		for (int i = 1; i < numIds; i++) {
			sb.append("?, ");
		}
		sb.append("?) ");
		
		return sb.toString();
	}
}
