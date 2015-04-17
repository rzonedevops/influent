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

import influent.idl.*;
import influent.idlhelper.DataPropertyDescriptorHelper;
import influent.idlhelper.EntityHelper;
import influent.idlhelper.PropertyHelper;
import influent.server.configuration.ApplicationConfiguration;
import influent.server.utilities.Pair;
import influent.server.utilities.PropertyField;
import influent.server.utilities.SQLConnectionPool;
import influent.server.utilities.InfluentId;
import org.apache.avro.AvroRemoteException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oculus.aperture.spi.common.Properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static influent.server.configuration.ApplicationConfiguration.SystemPropertyKey.*;


public class DataViewDataAccess implements FL_DataAccess {

	protected final SQLConnectionPool _connectionPool;
	protected final FL_EntitySearch _search;
	private final DataNamespaceHandler _namespaceHandler;
	private final boolean _isMultiType;
	protected final ApplicationConfiguration _applicationConfiguration;

	private static Pattern COLUMN_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*", 0);

	protected static final Logger s_logger = LoggerFactory.getLogger(DataViewDataAccess.class);

	protected Logger getLogger() {
		return s_logger;
	}



	public DataViewDataAccess(
		Properties config,
		SQLConnectionPool connectionPool,
		FL_EntitySearch search,
		DataNamespaceHandler namespaceHandler
	) throws ClassNotFoundException, SQLException {

		// TODO: ehCacheConfig!!
		_connectionPool = connectionPool;
		_search = search;
		_namespaceHandler = namespaceHandler;
		_applicationConfiguration = ApplicationConfiguration.getInstance(config);
		_isMultiType = (_applicationConfiguration.getEntityDescriptors().getTypes().size() > 1);
	}




	protected DataNamespaceHandler getNamespaceHandler() {
		return _namespaceHandler;
	}




	protected FL_PropertyDescriptors getDescriptors() {
		return _applicationConfiguration.getEntityDescriptors();
	}




	public PropertyField.Provider getPropertyFieldProvider() {
		return _applicationConfiguration;
	}


	private ApplicationConfiguration.SystemColumnType _getIdColumnType() {
		if (_isMultiType) {
			// Multitype IDs are always strings
			return ApplicationConfiguration.SystemColumnType.STRING;
		} else {
			// There is only one type, so get the column type for it
			String typeString = getDescriptors().getTypes().get(0).getKey();
			return _applicationConfiguration.getColumnType(typeString, FIN_ENTITY.name(), ENTITY_ID.name());
		}
	}


	private InfluentId influentIDFromRaw(char entityClass, String entityType, String rawId) {
		if (_isMultiType) {
			return InfluentId.fromTypedId(entityClass, rawId);
		} else {
			return InfluentId.fromNativeId(entityClass, entityType, rawId);
		}
	}

	private String rawFromInfluentID(InfluentId infId) {
		if (_isMultiType) {
			return infId.getTypedId();
		} else {
			return infId.getNativeId();
		}
	}


	private String typeFromRaw(String rawId) {
		if (_isMultiType) {
			return InfluentId.fromTypedId(InfluentId.ACCOUNT, rawId).getIdType();
		} else {
			return getDescriptors().getTypes().get(0).getKey();
		}
	}


	@Override
	public List<FL_Entity> getEntities(
		List<String> entities,
		FL_LevelOfDetail levelOfDetail
	) throws AvroRemoteException {

		List<FL_Entity> results = new LinkedList<FL_Entity>();

		if (entities == null || entities.isEmpty()) {
			return results;
		}

		FL_PropertyDescriptors descriptors = getDescriptors();
		Map<String, List<String>> entitiesByType = _namespaceHandler.entitiesByType(entities);

		try {

			Connection connection = _connectionPool.getConnection();

			for (Map.Entry<String, List<String>> entry : entitiesByType.entrySet()) {

				String entityType = entry.getKey();
				List<String> entitySubgroup = entry.getValue();

				if (entitySubgroup == null || entitySubgroup.isEmpty()) {
					continue;
				}

				String finEntityTable = _applicationConfiguration.getTable(entityType, FIN_ENTITY.name(), FIN_ENTITY.name());
				String finEntityEntityId = _applicationConfiguration.getColumn(entityType, FIN_ENTITY.name(), ENTITY_ID.name());

				// process entities in batches
				List<String> idsCopy = new ArrayList<String>(entitySubgroup); // copy the ids as we will take 1000 at a time to process and the take method is destructive
				while (idsCopy.size() > 0) {
					List<String> tempSubList = (idsCopy.size() > 1000) ? idsCopy.subList(0, 999) : idsCopy; // get the next 1000
					List<String> subIds = new ArrayList<String>(tempSubList); // copy as the next step is destructive
					tempSubList.clear(); // this clears the IDs from idsCopy as tempSubList is backed by idsCopy
					String ids = createIdListFromCollection(subIds);

					StringBuilder sb = new StringBuilder();
					sb.append("SELECT ");
					for (FL_PropertyDescriptor prop : descriptors.getProperties()) {
						for (FL_TypeMapping map : prop.getMemberOf()) {
							if (map.getType().equalsIgnoreCase(entityType)) {

								// non-primitive objects need decomposition
								if (prop.getPropertyType() == FL_PropertyType.GEO) {
									List<PropertyField> fields = getPropertyFieldProvider().getFields(prop.getKey());

									if (fields != null) {
										for (PropertyField field : fields) {
											final String fieldKey = DataPropertyDescriptorHelper.getFieldname(field.getProperty(), entry.getKey(), null);

											if (fieldKey != null) {
												sb.append(fieldKey + ", ");
											}
										}
									}

								} else {
									sb.append(map.getMemberKey() + ", ");
								}
							}
						}
					}
					sb.replace(sb.length() - 2, sb.length(), " ");

					sb.append("FROM " + finEntityTable + " ");
					sb.append("WHERE " + finEntityEntityId + " IN (" + ids + ")");

					Statement stmt = connection.createStatement();

					ResultSet rs = stmt.executeQuery(sb.toString());
					while (rs.next()) {

						List<FL_Property> props = new ArrayList<FL_Property>();

						for (FL_PropertyDescriptor prop : descriptors.getProperties()) {
							// Test to see if the property is set hidden, or if it's hidden by Level of Detail
							boolean isHidden = prop.getLevelOfDetail().equals(FL_LevelOfDetail.HIDDEN) ||
									(levelOfDetail.equals(FL_LevelOfDetail.SUMMARY) && prop.getLevelOfDetail().equals(FL_LevelOfDetail.FULL));

							List<Object> dataList = new ArrayList<Object>();

							if (prop.getPropertyType() == FL_PropertyType.GEO) {
								// Handle GEO composite properties
								List<Object> textValues = getCompositeFieldValues(rs, prop.getKey(), entityType, "text");
								List<Object> ccValues = getCompositeFieldValues(rs, prop.getKey(), entityType, "cc");
								List<Object> latValues = getCompositeFieldValues(rs, prop.getKey(), entityType, "lat");
								List<Object> lonValues = getCompositeFieldValues(rs, prop.getKey(), entityType, "lon");

								// Assume we have the same number of values in all lists.
								for (int i = 0; i < latValues.size(); i++) {

									FL_GeoData.Builder geoDataBuilder = FL_GeoData.newBuilder()
											.setText(textValues != null ? (String) textValues.get(i)    : null)
											.setCc(ccValues     != null ? (String) ccValues.get(i)      : null)
											.setLat(latValues   != null ? parseLatLon(latValues.get(i)) : null)
											.setLon(lonValues   != null ? parseLatLon(lonValues.get(i)) : null);

									dataList.add(geoDataBuilder.build());
								}

							} else {
								// Handle everything else
								dataList = getPropertyValuesFromResults(rs, prop, entityType);
							}

							if (dataList == null || dataList.isEmpty()) {
								continue;
							}

							Object range;
							// Create a list range, or a singleton range depending on how many values we got
							if (dataList.size() > 1) {
								range =	FL_ListRange.newBuilder()
										.setType(prop.getPropertyType())
										.setValues(dataList)
										.build();
							} else {
								range =	FL_SingletonRange.newBuilder()
										.setType(prop.getPropertyType())
										.setValue(dataList.get(0))
										.build();
							}

							// Add the property
							props.add(
									new PropertyHelper(
											prop.getKey(),
											prop.getFriendlyText(),
											null,
											null,
											prop.getTags(),
											isHidden,
											range
									)
							);
						}

						FL_Property idProp = PropertyHelper.getPropertyByKey(props, FL_RequiredPropertyKey.ID.name());
						String idVal = idProp != null ? PropertyHelper.getValue(idProp).toString() : null;

						char entityClass = getEntityClass(subIds, entityType, idVal);

						FL_Entity entity = createEntity(
							idVal,
							entityClass,
							entityType,
							props
						);

						results.add(entity);
					}
					rs.close();

					stmt.close();
				}
			}

			connection.close();
			return results;

		} catch (Exception e) {
			throw new AvroRemoteException(e);
		}
	}




	private Double parseLatLon(Object value) {
		// Lat/Lon conversion helper function

		if (value instanceof Float) {
			Float f = (Float) value;
			return f.doubleValue();
		} else if (value instanceof String) {
			return Double.parseDouble(value.toString());
		}

		return (Double)value;
	}




	protected List<Object> getCompositeFieldValues(ResultSet rs, String key, String type, String fieldName) throws AvroRemoteException {
		// Get values for fields in composite properties

		PropertyField pf = getPropertyFieldProvider().getField(key, fieldName);
		FL_PropertyDescriptor pd = pf.getProperty();

		if (pf != null) {
			final String mappedKey = DataPropertyDescriptorHelper.getFieldname(pd, type, null);

			if (mappedKey != null) {
				return getPropertyValuesFromResults(rs, pd, type);
			}
		}

		return null;
	}

	private char getEntityClass(List<String> influentIds, String type, String rawId) {
		String unclassedId = influentIDFromRaw(InfluentId.ACCOUNT, type, rawId).toString();
		for (String influentId : influentIds) {
			if (influentId.substring(2).equals(unclassedId.substring(2))) {
				return influentId.charAt(0);
			}
		}
		return InfluentId.ACCOUNT;
	}


	protected List<Object> getPropertyValuesFromResults(ResultSet rs, FL_PropertyDescriptor pd, String type) throws AvroRemoteException {
		// Get values for a property from the results set

		boolean isMultiValue = pd.getMultiValue();
		String key = null;
		for (FL_TypeMapping typeMapping : pd.getMemberOf()) {
			if (typeMapping.getType().equals(type)) {
				key = typeMapping.getMemberKey();
				break;
			}
		}

		if (key == null) {
			return null;
		}

		final FL_PropertyType dataType = pd.getPropertyType();
		List<Object> values = new ArrayList<Object>();
		Object value;

		try {
			value = rs.getObject(key);

			// If the value was a clob, convert it to a string first
			if (value instanceof Clob) {
				StringBuilder sb = new StringBuilder();
				Clob clob = (Clob) (value);
				Reader reader = clob.getCharacterStream();
				BufferedReader bufferedReader = new BufferedReader(reader);

				String line;
				while (null != (line = bufferedReader.readLine())) {
					sb.append(line);
				}
				bufferedReader.close();
				value = sb.toString();
			}
		} catch (Exception e) {
			throw new AvroRemoteException(e);
		}

		if (isMultiValue) {
			// Multivalue properties should always be strings. Split them up.
			values.addAll(Arrays.asList(value.toString().split(",")));
		} else {
			if (value != null) {
				values.add(value);
			}
		}

		if (values.isEmpty()) {
			return null;
		}

		for (int i = 0; i < values.size(); i++) {
			Object val = values.get(i);

			switch (dataType) {
				case DATE:
					Timestamp dateValue;
					if (val instanceof Long) {
						dateValue = new Timestamp((Long)val);
					} else {
						dateValue = (Timestamp) val;
					}

					val = dateValue.getTime();
					break;
				case STRING:
					val = val.toString();
					break;
			}

			values.set(i, val);
		}

		return values;

	}

	protected FL_Entity createEntity(String id, char entityClass, String entityType, List<FL_Property> props) {
		String uid = influentIDFromRaw(entityClass, entityType, id).toString();
		return new EntityHelper(uid, entityType, Collections.singletonList(FL_EntityTag.ACCOUNT), null, null, props);

	}


	@Override
	public List<FL_DataSummary> getDataSummary() {
		List<FL_DataSummary> ret = new ArrayList<FL_DataSummary>();
		try {

			String dataSummaryTable = _applicationConfiguration.getTable(DATA_SUMMARY.name(), DATA_SUMMARY.name());
			String summaryKeyColumn = _applicationConfiguration.getColumn(DATA_SUMMARY.name(), SUMMARY_KEY.name());
			String summaryLabelColumn = _applicationConfiguration.getColumn(DATA_SUMMARY.name(), SUMMARY_LABEL.name());
			String summaryValueColumn = _applicationConfiguration.getColumn(DATA_SUMMARY.name(), SUMMARY_VALUE.name());
			String summaryOrderColumn = _applicationConfiguration.getColumn(DATA_SUMMARY.name(), SUMMARY_ORDER.name());

			Connection con = _connectionPool.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(String.format("SELECT %s, %s, %s FROM %s ORDER BY %s ASC", summaryKeyColumn, summaryLabelColumn,
				summaryValueColumn, dataSummaryTable, summaryOrderColumn));
			while (rs.next()) {
				String key = rs.getString("SummaryKey");
				String label = rs.getString("SummaryLabel");
				String value = rs.getString("SummaryValue");

				FL_DataSummary tmp = FL_DataSummary.newBuilder()
					.setKey(key)
					.setLabel(label)
					.setValue(value)
					.build();

				ret.add(tmp);
			}
		} catch (Exception e) {
			System.out.println("Error while retrieving summary from data source");
			e.printStackTrace();
		}
		return ret;
	}



	@Override
	public Map<String, List<FL_Link>> getFlowAggregation(
		List<String> entities,
		List<String> focusEntities,
		FL_DirectionFilter direction,
		FL_LinkEntityTypeFilter entityTypeFilter,
		FL_DateRange date
	) throws AvroRemoteException {

		Map<String, List<FL_Link>> results = new HashMap<String, List<FL_Link>>();

		FL_PropertyDescriptors linkDescriptors = _applicationConfiguration.getLinkDescriptors();

		DateTime startDate = DataAccessHelper.getStartDate(date);
		DateTime endDate = DataAccessHelper.getEndDate(date);

		Connection connection = null;
		try {
			connection = _connectionPool.getConnection();
			for (FL_TypeDescriptor td : linkDescriptors.getTypes()) {

				String type = td.getKey();

				// get tables
				String finFlowTable = _applicationConfiguration.getTable(type, FIN_FLOW.name(), FIN_FLOW.name());
				String finFlowIntervalTable = _applicationConfiguration.getIntervalTable(type, FIN_FLOW_BUCKETS.name(), date.getDurationPerBin().getInterval());

				// get columns
				String finFlowFromEntityIdColumn = _applicationConfiguration.getColumn(type, FIN_FLOW_BUCKETS.name(), FROM_ENTITY_ID.name());
				String finFlowFromEntityTypeColumn = _applicationConfiguration.getColumn(type, FIN_FLOW_BUCKETS.name(), FROM_ENTITY_TYPE.name());
				String finFlowToEntityIdColumn = _applicationConfiguration.getColumn(type, FIN_FLOW_BUCKETS.name(), TO_ENTITY_ID.name());
				String finFlowToEntityTypeColumn = _applicationConfiguration.getColumn(type, FIN_FLOW_BUCKETS.name(), TO_ENTITY_TYPE.name());
				String finFlowAmountColumn = _applicationConfiguration.getColumn(type, FIN_FLOW_BUCKETS.name(), AMOUNT.name());
				String finFlowDateColumn = _applicationConfiguration.getColumn(type, FIN_FLOW_BUCKETS.name(), PERIOD_DATE.name());

				Statement stmt = connection.createStatement();

				List<String> idsCopy = new ArrayList<String>(entities); // copy the ids as we will take 100 at a time to process and the take method is destructive
				String focusIds = createIdListFromCollection(focusEntities);

				while (idsCopy.size() > 0) {
					List<String> tempSubList = (idsCopy.size() > 100) ? idsCopy.subList(0, 99) : idsCopy; // get the next 100
					List<String> subIds = new ArrayList<String>(tempSubList); // copy as the next step is destructive
					tempSubList.clear(); // this clears the IDs from idsCopy as tempSubList is backed by idsCopy
					
					String ids = createIdListFromCollection(subIds);

					String sourceDirectionClause = finFlowFromEntityIdColumn + " in (" + ids + ")";
					String destDirectionClause = finFlowToEntityIdColumn + " in (" + ids + ")";

					if (focusIds != null) {
						sourceDirectionClause += " and " + finFlowToEntityIdColumn + " in (" + focusIds + ")";
						destDirectionClause += " and " + finFlowFromEntityIdColumn + " in (" + focusIds + ")";
					}
					String directionClause = (direction == FL_DirectionFilter.BOTH) ? sourceDirectionClause + " and " + destDirectionClause : (direction == FL_DirectionFilter.DESTINATION) ? destDirectionClause : (direction == FL_DirectionFilter.SOURCE) ? sourceDirectionClause : "1=1";
					String entityTypeClause = linkEntityTypeClause(
						direction,
						entityTypeFilter,
						finFlowFromEntityTypeColumn,
						finFlowToEntityTypeColumn
					);

					String dateRespectingFlowSQL = "select " + finFlowFromEntityIdColumn + ", " + finFlowToEntityIdColumn + ", sum(" + finFlowAmountColumn + ") as " + finFlowAmountColumn + " from " + finFlowIntervalTable + " where " + finFlowDateColumn + " between '" + getNamespaceHandler().formatDate(startDate) + "' and '" + getNamespaceHandler().formatDate(endDate) + "' and " + directionClause + " and " + entityTypeClause + " group by " + finFlowFromEntityIdColumn + ", " + finFlowToEntityIdColumn;
					String flowSQL = "select " + finFlowFromEntityIdColumn + ", " + finFlowFromEntityTypeColumn + ", " + finFlowToEntityIdColumn + ", " + finFlowToEntityTypeColumn + " from " + finFlowTable + " where " + directionClause + " and " + entityTypeClause;

					Map<String, Map<String, Double>> fromToAmountMap = new HashMap<String, Map<String, Double>>();

					getLogger().trace(dateRespectingFlowSQL);
					if (stmt.execute(dateRespectingFlowSQL)) {
						ResultSet rs = stmt.getResultSet();
						while (rs.next()) {
							String from = rs.getString(finFlowFromEntityIdColumn);
							String to = rs.getString(finFlowToEntityIdColumn);
							Double amount = rs.getDouble(finFlowAmountColumn);
							if (fromToAmountMap.containsKey(from)) {
								if (fromToAmountMap.get(from).containsKey(to)) {
									fromToAmountMap.get(from).put(to, fromToAmountMap.get(from).get(to) + amount);
									getLogger().warn("Duplicate entity to entity link discovered: " + from + " to " + to + ". The link has been aggregated.");
								} else {
									fromToAmountMap.get(from).put(to, amount);
								}
							} else {
								Map<String, Double> toAmountMap = new HashMap<String, Double>();
								toAmountMap.put(to, amount);
								fromToAmountMap.put(from, toAmountMap);
							}
						}
					}
					getLogger().trace(flowSQL);
					if (stmt.execute(flowSQL)) {
						ResultSet rs = stmt.getResultSet();
						while (rs.next()) {
							String from = rs.getString(finFlowFromEntityIdColumn);
							char fromClass = rs.getString(finFlowFromEntityTypeColumn).toLowerCase().charAt(0);
							String fromType = typeFromRaw(from);
							String to = rs.getString(finFlowToEntityIdColumn);
							char toClass = rs.getString(finFlowToEntityTypeColumn).toLowerCase().charAt(0);
							String toType = typeFromRaw(to);

							// globalize this for return
							String keyId = InfluentId.fromNativeId(InfluentId.LINK, type, from + "_" + to).toString();

							List<FL_Link> linkList = results.get(keyId);
							if (linkList == null) {
								linkList = new LinkedList<FL_Link>();
								results.put(keyId, linkList);
							}

							// only use the amount calculated above with the date respecting FLOW map
							Double amount = (fromToAmountMap.containsKey(from) && fromToAmountMap.get(from).containsKey(to)) ? amount = fromToAmountMap.get(from).get(to) : 0.0;
							List<FL_Property> properties = new ArrayList<FL_Property>();
							properties.add(new PropertyHelper(FL_PropertyTag.AMOUNT, amount));

							// globalize these for return
							from = influentIDFromRaw(fromClass, fromType, from).toString();
							to = influentIDFromRaw(toClass, toType, to).toString();

							//Finally, create the link between the two, and add it to the map.
							FL_Link link = FL_Link.newBuilder()
								.setUid(keyId)
								.setLinkTypes(null)
								.setSource(from)
								.setTarget(to)
								.setType(type)
								.setDirected(true)
								.setProvenance(null)
								.setUncertainty(null)
								.setProperties(properties)
								.build();
							linkList.add(link);
						}
						rs.close();
					}
				}

				stmt.close();
			}

			connection.close();
		} catch (ClassNotFoundException e) {
			throw new AvroRemoteException(e);
		} catch (SQLException e) {
			throw new AvroRemoteException(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return results;
	}




	private String createIdListFromCollection(List<String> ids) {
		if (ids == null || ids.isEmpty()) return null;

		StringBuilder resultString = new StringBuilder();

		ApplicationConfiguration.SystemColumnType idType = _getIdColumnType();

		for (String id : ids) {
			InfluentId infId = InfluentId.fromInfluentId(id);
			resultString.append(getNamespaceHandler().toSQLId(rawFromInfluentID(infId), idType));
			resultString.append(",");
		}

		resultString.deleteCharAt(resultString.lastIndexOf(","));

		return resultString.toString();
	}


	@Override
	public Map<String, List<FL_Link>> getTimeSeriesAggregation(
		List<String> entities,
		List<String> focusEntities,
		FL_DateRange date
	) throws AvroRemoteException {

		Map<String, List<FL_Link>> results = new HashMap<String, List<FL_Link>>();

		FL_PropertyDescriptors linkDescriptors = _applicationConfiguration.getLinkDescriptors();
		FL_PropertyDescriptors entityDescriptors = _applicationConfiguration.getEntityDescriptors();

		DateTime startDate = DataAccessHelper.getStartDate(date);
		DateTime endDate = DataAccessHelper.getEndDate(date);
		String focusIds = createIdListFromCollection(focusEntities);

		Connection connection = null;
		try {
			connection = _connectionPool.getConnection();

			if (focusEntities != null && focusEntities.size() > 0) {
				for (FL_TypeDescriptor td : linkDescriptors.getTypes()) {

					String linkType = td.getKey();

					String finFlowIntervalTable = _applicationConfiguration.getIntervalTable(linkType, FIN_FLOW_BUCKETS.name(), date.getDurationPerBin().getInterval());
					String finFlowFromEntityIdColumn = _applicationConfiguration.getColumn(linkType, FIN_FLOW_BUCKETS.name(), FROM_ENTITY_ID.name());
					String finFlowFromEntityTypeColumn = _applicationConfiguration.getColumn(linkType, FIN_FLOW_BUCKETS.name(), FROM_ENTITY_TYPE.name());
					String finFlowToEntityIdColumn = _applicationConfiguration.getColumn(linkType, FIN_FLOW_BUCKETS.name(), TO_ENTITY_ID.name());
					String finFlowToEntityTypeColumn = _applicationConfiguration.getColumn(linkType, FIN_FLOW_BUCKETS.name(), TO_ENTITY_TYPE.name());
					String finFlowAmountColumn = _applicationConfiguration.getColumn(linkType, FIN_FLOW_BUCKETS.name(), AMOUNT.name());
					String finFlowDateColumn = _applicationConfiguration.getColumn(linkType, FIN_FLOW_BUCKETS.name(), PERIOD_DATE.name());

					Statement stmt = connection.createStatement();

					List<String> idsCopy = new ArrayList<String>(entities); // copy the ids as we will take 100 at a time to process and the take method is destructive
					while (idsCopy.size() > 0) {
						List<String> tempSubList = (idsCopy.size() > 100) ? idsCopy.subList(0, 99) : idsCopy; // get the next 100
						List<String> subIds = new ArrayList<String>(tempSubList); // copy as the next step is destructive
						tempSubList.clear(); // this clears the IDs from idsCopy as tempSubList is backed by idsCopy

						String ids = createIdListFromCollection(subIds);

						String focusedSQL = buildStatementForFocusFlow(
								ids,
								focusIds,
								finFlowFromEntityIdColumn,
								finFlowFromEntityTypeColumn,
								finFlowToEntityIdColumn,
								finFlowToEntityTypeColumn,
								finFlowDateColumn,
								finFlowAmountColumn,
								finFlowIntervalTable,
								startDate,
								endDate
						);

						getLogger().trace(focusedSQL);

						if (stmt.execute(focusedSQL)) {
							ResultSet rs = stmt.getResultSet();
							while (rs.next()) {

								String from = rs.getString(finFlowFromEntityIdColumn);
								char fromClass = rs.getString(finFlowFromEntityTypeColumn).toLowerCase().charAt(0);
								String fromType = typeFromRaw(from);
								String to = rs.getString(finFlowToEntityIdColumn);
								char toClass = rs.getString(finFlowToEntityTypeColumn).toLowerCase().charAt(0);
								String toType = typeFromRaw(to);

								Double amount = rs.getDouble(finFlowAmountColumn);
								Date rsDate = rs.getDate(finFlowDateColumn);

								// globalize these for return
								String globalFrom = InfluentId.fromNativeId(fromClass, fromType, from).toString();
								String globalTo = InfluentId.fromNativeId(toClass, toType, to).toString();

								String keyId = globalFrom;
								if (subIds.contains(globalTo)) {
									keyId = globalTo;
								}

								List<FL_Link> linkList = results.get(keyId);
								if (linkList == null) {
									linkList = new LinkedList<FL_Link>();
									results.put(keyId, linkList);
								}

								// only use the amount calculated above with the date respecting FLOW map
								List<FL_Property> properties = new ArrayList<FL_Property>();
								properties.add(new PropertyHelper(FL_PropertyTag.AMOUNT, amount));
								properties.add(new PropertyHelper(FL_PropertyTag.DATE, rsDate));

								FL_Link link = FL_Link.newBuilder()
									.setUid(InfluentId.fromNativeId(InfluentId.LINK, linkType, UUID.randomUUID().toString()).toString())
									.setSource(globalFrom)
									.setTarget(globalTo)
									.setLinkTypes(null)
									.setType(linkType)
									.setDirected(true)
									.setProvenance(null)
									.setUncertainty(null)
									.setProperties(properties)
									.build();
								linkList.add(link);
							}
							rs.close();
						}
					}
					stmt.close();
				}
			}

			Map<String, List<String>> entitiesByType = _namespaceHandler.entitiesByType(entities);
			for (Map.Entry<String, List<String>> entry : entitiesByType.entrySet()) {

				String entityType = entry.getKey();
				List<String> entitySubgroup = entry.getValue();

				if (entitySubgroup == null || entitySubgroup.isEmpty()) {
					continue;
				}

				String finEntityIntervalTable = _applicationConfiguration.getIntervalTable(entityType, FIN_ENTITY_BUCKETS.name(), date.getDurationPerBin().getInterval());
				String finEntityEntityIdColumn = _applicationConfiguration.getColumn(entityType, FIN_ENTITY_BUCKETS.name(), ENTITY_ID.name());
				String finEntityInboundAmountColumn = _applicationConfiguration.getColumn(entityType, FIN_ENTITY_BUCKETS.name(), INBOUND_AMOUNT.name());
				String finEntityOutboundAmountColumn = _applicationConfiguration.getColumn(entityType, FIN_ENTITY_BUCKETS.name(), OUTBOUND_AMOUNT.name());
				String finEntityDateColumn = _applicationConfiguration.getColumn(entityType, FIN_ENTITY_BUCKETS.name(), PERIOD_DATE.name());

				Statement stmt = connection.createStatement();

				// process entities in batches
				List<String> idsCopy = new ArrayList<String>(entitySubgroup); // copy the ids as we will take 1000 at a time to process and the take method is destructive
				while (idsCopy.size() > 0) {
					List<String> tempSubList = (idsCopy.size() > 1000) ? idsCopy.subList(0, 999) : idsCopy; // get the next 1000
					List<String> subIds = new ArrayList<String>(tempSubList); // copy as the next step is destructive
					tempSubList.clear(); // this clears the IDs from idsCopy as tempSubList is backed by idsCopy

					String ids = createIdListFromCollection(subIds);

					String tsSQL = buildStatementForTimeSeriesAggregation(
						ids,
						finEntityEntityIdColumn,
						finEntityDateColumn,
						finEntityInboundAmountColumn,
						finEntityOutboundAmountColumn,
						finEntityIntervalTable,
						startDate,
						endDate
					);

					getLogger().trace(tsSQL);

					if (stmt.execute(tsSQL)) {
						ResultSet rs = stmt.getResultSet();
						while (rs.next()) {
							String entity = rs.getString(finEntityEntityIdColumn);
							Double inboundAmount = rs.getDouble(finEntityInboundAmountColumn);
							Double outboundAmount = rs.getDouble(finEntityOutboundAmountColumn);
							Date rsDate = rs.getDate(finEntityDateColumn);

							// globalize this for return
							entity = influentIDFromRaw(InfluentId.ACCOUNT, entityType, entity).toString();

							List<FL_Link> linkList = results.get(entity);
							if (linkList == null) {
								linkList = new LinkedList<FL_Link>();
								results.put(entity, linkList);
							}

							List<FL_Property> inProperties = new ArrayList<FL_Property>();
							inProperties.add(new PropertyHelper(FL_PropertyTag.AMOUNT, inboundAmount));
							inProperties.add(new PropertyHelper(FL_PropertyTag.DATE, rsDate));

							List<FL_Property> outProperties = new ArrayList<FL_Property>();
							outProperties.add(new PropertyHelper(FL_PropertyTag.AMOUNT, outboundAmount));
							outProperties.add(new PropertyHelper(FL_PropertyTag.DATE, rsDate));

							FL_Link inLink = FL_Link.newBuilder()
								.setUid(InfluentId.fromNativeId(InfluentId.LINK, linkDescriptors.getTypes().get(0).getKey(), UUID.randomUUID().toString()).toString())
								.setLinkTypes(null)
								.setSource(null)
								.setTarget(entity)
								.setType(linkDescriptors.getTypes().get(0).getKey())
								.setDirected(false)
								.setProvenance(null)
								.setUncertainty(null)
								.setProperties(inProperties)
								.build();
							FL_Link outLink = FL_Link.newBuilder()
								.setUid(InfluentId.fromNativeId(InfluentId.LINK, linkDescriptors.getTypes().get(0).getKey(), UUID.randomUUID().toString()).toString())
								.setLinkTypes(null)
								.setSource(entity)
								.setTarget(null)
								.setType(linkDescriptors.getTypes().get(0).getKey())
								.setDirected(false)
								.setProvenance(null)
								.setUncertainty(null)
								.setProperties(outProperties)
								.build();
							linkList.add(inLink);
							linkList.add(outLink);
						}
						rs.close();
					}
				}
				stmt.close();
			}
			connection.close();
		} catch (ClassNotFoundException e) {
			throw new AvroRemoteException(e);
		} catch (SQLException e) {
			throw new AvroRemoteException(e);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return results;
	}




	protected String buildStatementForTimeSeriesAggregation(
		String ids,
		String finEntityEntityIdColumn,
		String finEntityDateColumn,
		String finEntityInboundAmountColumn,
		String finEntityOutboundAmountColumn,
		String finEntityIntervalTable,
		DateTime startDate,
		DateTime endDate

	) {
		if (finEntityEntityIdColumn == null ||
			finEntityDateColumn == null ||
			finEntityInboundAmountColumn == null ||
			finEntityOutboundAmountColumn == null ||
			finEntityIntervalTable == null ||
			startDate == null ||
			endDate == null
			) {
			getLogger().error("buildStatementForTimeSeriesAggregation: Invalid parameter");
			return null;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT ");
		sb.append(finEntityEntityIdColumn);
		sb.append(", ");
		sb.append(finEntityDateColumn);
		sb.append(", ");
		sb.append(finEntityInboundAmountColumn);
		sb.append(", ");
		sb.append(finEntityOutboundAmountColumn);
		sb.append(" FROM ");
		sb.append(finEntityIntervalTable);
		sb.append(" WHERE ");
		sb.append(finEntityDateColumn);
		sb.append(" BETWEEN '");
		sb.append(getNamespaceHandler().formatDate(startDate));
		sb.append("' AND '");
		sb.append(getNamespaceHandler().formatDate(endDate));
		sb.append("'");

		if (ids.length() > 0) {
			sb.append(" AND ");
			sb.append(finEntityEntityIdColumn);
			sb.append(" IN (");
			sb.append(ids);
			sb.append(") ");
		}

		return sb.toString();
	}




	protected String buildStatementForFocusFlow(
		String ids,
		String focusIds,
		String finFlowFromEntityIdColumn,
		String finFlowFromEntityTypeColumn,
		String finFlowToEntityIdColumn,
		String finFlowToEntityTypeColumn,
		String finFlowDateColumn,
		String finFlowAmountColumn,
		String finFlowIntervalTable,
		DateTime startDate,
		DateTime endDate
	) {
		if (finFlowFromEntityIdColumn == null ||
			finFlowFromEntityTypeColumn == null ||
			finFlowToEntityIdColumn == null ||
			finFlowToEntityTypeColumn == null ||
			finFlowDateColumn == null ||
			finFlowAmountColumn == null ||
			finFlowIntervalTable == null ||
			startDate == null ||
			endDate == null
		) {
			getLogger().error("buildStatementForFlow: Invalid parameter");
			return null;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT ");
		sb.append(finFlowFromEntityIdColumn);
		sb.append(", ");
		sb.append(finFlowFromEntityTypeColumn);
		sb.append(", ");
		sb.append(finFlowToEntityIdColumn);
		sb.append(", ");
		sb.append(finFlowToEntityTypeColumn);
		sb.append(", ");
		sb.append(finFlowDateColumn);
		sb.append(", ");
		sb.append(finFlowAmountColumn);
		sb.append(" FROM ");
		sb.append(finFlowIntervalTable);
		sb.append(" WHERE ");
		sb.append(finFlowDateColumn);
		sb.append(" BETWEEN '");
		sb.append(getNamespaceHandler().formatDate(startDate));
		sb.append("' AND '");
		sb.append(getNamespaceHandler().formatDate(endDate));
		sb.append("'");
		if (ids.length() > 0) {
			sb.append(" AND ");
			sb.append(finFlowFromEntityIdColumn);
			sb.append(" IN (");
			sb.append(ids);
			sb.append(") ");
		}
		if (focusIds.length() > 0) {
			sb.append(" AND ");
			sb.append(finFlowToEntityIdColumn);
			sb.append(" IN (");
			sb.append(focusIds);
			sb.append(") ");
		}
		sb.append(" UNION ");
		sb.append("SELECT ");
		sb.append(finFlowFromEntityIdColumn);
		sb.append(", ");
		sb.append(finFlowFromEntityTypeColumn);
		sb.append(", ");
		sb.append(finFlowToEntityIdColumn);
		sb.append(", ");
		sb.append(finFlowToEntityTypeColumn);
		sb.append(", ");
		sb.append(finFlowDateColumn);
		sb.append(", ");
		sb.append(finFlowAmountColumn);
		sb.append(" FROM ");
		sb.append(finFlowIntervalTable);
		sb.append(" WHERE ");
		sb.append(finFlowDateColumn);
		sb.append(" BETWEEN '");
		sb.append(getNamespaceHandler().formatDate(startDate));
		sb.append("' AND '");
		sb.append(getNamespaceHandler().formatDate(endDate));
		sb.append("'");
		if (ids.length() > 0) {
			sb.append(" AND ");
			sb.append(finFlowToEntityIdColumn);
			sb.append(" IN (");
			sb.append(ids);
			sb.append(") ");
		}
		if (focusIds.length() > 0) {
			sb.append(" AND ");
			sb.append(finFlowFromEntityIdColumn);
			sb.append(" IN (");
			sb.append(focusIds);
			sb.append(") ");
		}

		return sb.toString();
	}




	@Override
	public Map<String, List<FL_Entity>> getAccounts(
		List<String> entities
	) throws AvroRemoteException {
		Map<String, List<FL_Entity>> map = new HashMap<String, List<FL_Entity>>();
		for (FL_Entity entity : getEntities(entities, FL_LevelOfDetail.SUMMARY)) {
			map.put(entity.getUid(), Collections.singletonList(entity));
		}
		return map;
	}




	public String getClientState(
		String sessionId
	) throws AvroRemoteException {
		String data = null;
		Connection connection = null;
		try {
			connection = _connectionPool.getConnection();

			String clientStateTableName = _applicationConfiguration.getTable(CLIENT_STATE.name(), CLIENT_STATE.name());

			// Create prepared statement
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT data " +
					"FROM " + clientStateTableName + " " +
					"WHERE sessionId = ?"
			);
			stmt.setString(1, sessionId);

			// Log prepared statement
			getLogger().trace(stmt.toString());

			// Execute prepared statement and evaluate results
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				data = rs.getString("data");
			}
			rs.close();

			// Close prepared statement
			stmt.close();

			connection.close();
		} catch (ClassNotFoundException e) {
			throw new AvroRemoteException(e);
		} catch (SQLException e) {
			throw new AvroRemoteException(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return data;
	}




	public void setClientState(
		String sessionId,
		String state
	) throws AvroRemoteException {
		Connection connection = null;
		try {
			connection = _connectionPool.getConnection();

			String clientStateTableName = _applicationConfiguration.getTable(CLIENT_STATE.name(), CLIENT_STATE.name());
			boolean exists = false;

			// Create prepared statement
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT sessionId " +
					"FROM " + clientStateTableName + " " +
					"WHERE sessionId = ?"
			);
			stmt.setString(1, sessionId);

			// Execute prepared statement and evaluate results
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				exists = true;
			}
			rs.close();

			// Close prepared statement
			stmt.close();

			String data = state.replaceAll("'", "''");
			DateTime now = new DateTime();

			if (exists) {
				// Create UPDATE prepared statement
				PreparedStatement pstmt = connection.prepareStatement(
					"UPDATE " + clientStateTableName + " " +
						"SET data = ?, modified = ? " +
						"WHERE sessionId = ?"
				);
				pstmt.setString(1, data);
				pstmt.setString(2, getNamespaceHandler().formatDate(now));
				pstmt.setString(3, sessionId);

				// Log prepared statement
				getLogger().trace(pstmt.toString());

				// Execute prepared statement and evaluate results
				pstmt.executeUpdate();

				// Close prepared statement
				pstmt.close();
			} else {
				// Create INSERT prepared statement
				PreparedStatement pstmt = connection.prepareStatement(
					"INSERT INTO " + clientStateTableName + " (sessionId, created, modified, data)" +
						"VALUES (?, ?, ?, ?) "
				);
				pstmt.setString(2, sessionId);
				pstmt.setString(3, getNamespaceHandler().formatDate(now));
				pstmt.setString(4, getNamespaceHandler().formatDate(now));
				pstmt.setString(5, data);

				// Log prepared statement
				getLogger().trace(pstmt.toString());

				// Execute prepared statement and evaluate results
				pstmt.executeUpdate();

				// Close prepared statement
				pstmt.close();
			}

			connection.close();
		} catch (ClassNotFoundException e) {
			throw new AvroRemoteException(e);
		} catch (SQLException e) {
			throw new AvroRemoteException(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}


	private static String unescapeColumnName(
		String columnName
	) {
		Matcher m = COLUMN_PATTERN.matcher(columnName);
		if (m.find()) {
			return m.group();
		}
		return columnName;
	}




	private String linkEntityTypeClause(
		FL_DirectionFilter direction,
		FL_LinkEntityTypeFilter entityType,
		String finFlowFromEntityTypeColumn,
		String finFlowToEntityTypeColumn
	) {
		if (entityType == FL_LinkEntityTypeFilter.ANY) return " 1=1 ";

		StringBuilder clause = new StringBuilder();
		String type = "A";
		if (entityType == FL_LinkEntityTypeFilter.ACCOUNT_OWNER) {
			type = "O";
		} else if (entityType == FL_LinkEntityTypeFilter.CLUSTER_SUMMARY) {
			type = "S";
		}

		// reverse direction - we are filtering on other entity
		switch (direction) {
			case DESTINATION:
				clause.append(" " + finFlowFromEntityTypeColumn + " = '" + type + "' ");
				break;
			case SOURCE:
				clause.append(" " + finFlowToEntityTypeColumn + " = '" + type + "' ");
				break;
			case BOTH:
				clause.append(" " + finFlowFromEntityTypeColumn + " = '" + type + "' AND " + finFlowToEntityTypeColumn + " = '" + type + "' ");
				break;
		}

		return clause.toString();
	}
}
