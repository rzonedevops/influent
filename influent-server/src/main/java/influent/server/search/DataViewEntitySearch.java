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

package influent.server.search;

import influent.idl.*;
import influent.idlhelper.EntityHelper;
import influent.idlhelper.DataPropertyDescriptorHelper;
import influent.idlhelper.PropertyHelper;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.dataaccess.SearchSQLHelper;
import influent.server.sql.SQLBuilder;
import influent.server.utilities.*;
import influent.server.configuration.ApplicationConfiguration;
import static influent.server.configuration.ApplicationConfiguration.SystemPropertyKey.*;

import oculus.aperture.spi.common.Properties;

import org.apache.avro.AvroRemoteException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by cdickson on 22/10/2014.
 */
public class DataViewEntitySearch implements FL_EntitySearch {

	protected final SQLConnectionPool _connectionPool;
	protected final DataNamespaceHandler _namespaceHandler;
	protected final SQLBuilder _sqlBuilder;
	protected SearchSQLHelper _sqlHelper;

	private final static boolean USE_PREPARED_STATEMENTS = false;

	protected final ApplicationConfiguration _applicationConfiguration;

	protected static final Logger s_logger = LoggerFactory.getLogger(DataViewEntitySearch.class);
	protected Logger getLogger() {
		return s_logger;
	}



	public DataViewEntitySearch(
		Properties config,
		SQLConnectionPool connectionPool,
		DataNamespaceHandler namespaceHandler,
		SQLBuilder sqlBuilder
	) {
		_connectionPool = connectionPool;
		_namespaceHandler = namespaceHandler;
		_sqlBuilder = sqlBuilder;
		_applicationConfiguration = ApplicationConfiguration.getInstance(config);
		_sqlHelper = new SearchSQLHelper(
				_sqlBuilder,
				_connectionPool,
				_namespaceHandler,
				_applicationConfiguration.getEntityDescriptors()
		);
	}




	@Override
	public FL_SearchResults search(
		Map<String, List<FL_PropertyMatchDescriptor>> termMap,
		List<FL_OrderBy> orderBy,
		long start,
		long max
	) throws AvroRemoteException {

		List<FL_SearchResult> matches = populateSearchResults(termMap, orderBy);

		return FL_SearchResults.newBuilder()
			.setTotal((long) matches.size())
			.setResults(matches)
			.setLevelOfDetail(FL_LevelOfDetail.FULL)
			.build();
	}




	protected List<FL_SearchResult> populateSearchResults(
		Map<String, List<FL_PropertyMatchDescriptor>> termMap,
		List<FL_OrderBy> orderBy
	) throws AvroRemoteException {

		ArrayList<FL_SearchResult> matches = new ArrayList<FL_SearchResult>();

		// utility classes
		final ArrayList<Pair<String, Object>> fieldValues = new ArrayList<Pair<String, Object>>();

		// Query by type
		for (Map.Entry<String, List<FL_PropertyMatchDescriptor>> entry : termMap.entrySet()) {

			if (USE_PREPARED_STATEMENTS) {
				// TODO: Prepared statement version
				s_logger.error("Prepared statements not yet supported!");
			} else {

				Set<String> columns = new HashSet<String>();
				for (FL_PropertyDescriptor prop : getDescriptors().getProperties()) {
					if (prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.ENTITY.name())) {
						continue;
					}
					for (FL_TypeMapping map : prop.getMemberOf()) {
						if (map.getType().equalsIgnoreCase(entry.getKey())) {
							// non-primitive objects need decomposition
							if (prop.getPropertyType() == FL_PropertyType.GEO) {
								List<PropertyField> fields = getPropertyFieldProvider().getFields(prop.getKey());

								if (fields != null) {
									for (PropertyField field : fields) {
										final String fieldKey = DataPropertyDescriptorHelper.getFieldname(field.getProperty(), entry.getKey(), null);

										if (fieldKey != null) {
											columns.add(fieldKey);
										}
									}
								}

							} else {
								columns.add(map.getMemberKey());
							}
						}
					}
				}

				String table = _applicationConfiguration.getTable(entry.getKey(), FIN_ENTITY.name(), FIN_ENTITY.name());

				// TODO: extract duplicate code between entity and link search, and DataViewDataAccess, and push into new class.

				// TODO: hash map is an expensive way to represent this, which is why db interfaces never do it. typically you want to lookup the column index by name once, or if not just do a linear search for the column name each time.
				List<Map<String, Object>> queryResults = _sqlHelper.fetchColumnsForTerms(table, new ArrayList<String>(columns), entry.getValue(), entry.getKey(), orderBy);
				for (Map<String, Object> resultMap : queryResults) {

					String id = null;
					String label = "";
					List<FL_Property> props = new ArrayList<FL_Property>();

					for (FL_PropertyDescriptor prop : getDescriptors().getProperties()) {
						if (prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.ENTITY.name())) {
							continue;
						}
						for (FL_TypeMapping map : prop.getMemberOf()) {
							if (map.getType().equalsIgnoreCase(entry.getKey())) {

								boolean isHidden = prop.getLevelOfDetail().equals(FL_LevelOfDetail.HIDDEN);
								if (prop.getPropertyType() == FL_PropertyType.GEO) {
									List<PropertyField> fields = getPropertyFieldProvider().getFields(prop.getKey());

									if (fields != null) {
										fieldValues.clear();

										for (PropertyField field : fields) {
											final String fieldKey = DataPropertyDescriptorHelper.getFieldname(field.getProperty(), entry.getKey(), null);

											if (fieldKey != null) {
												final Object valueObject = resultMap.get(fieldKey);
												if (valueObject != null) {
													fieldValues.add(new Pair<String, Object>(field.getName(), valueObject));
												}
											}
										}

										if (!fieldValues.isEmpty()) {
											FL_GeoData.Builder geoBuilder = FL_GeoData.newBuilder();
											for (Pair<String, Object> value : fieldValues) {
												try {
													if (value.first.equals("text")) {
														geoBuilder.setText(value.second.toString());
													} else if (value.first.equals("cc")) {
														geoBuilder.setCc(value.second.toString());
													} else if (value.first.equals("lat")) {
														geoBuilder.setLat(((Number)value.second).doubleValue());
													} else if (value.first.equals("lon")) {
														geoBuilder.setLon(((Number)value.second).doubleValue());
													}
												} catch (Exception e) {
												}
											}
											try {
												FL_GeoData geoValue = geoBuilder.build();
												props.add(new PropertyHelper(prop.getKey(), prop.getFriendlyText(), geoValue, new ArrayList<FL_PropertyTag>(prop.getTags()), isHidden));
											} catch (Exception e) {
											}
										}
									}

									continue;
								}

								Object valueObject = resultMap.get(map.getMemberKey());
								if (valueObject != null) {
									if (prop.getTags().contains(FL_PropertyTag.ID)) {
										id = valueObject.toString();
									} else if (prop.getTags().contains(FL_PropertyTag.LABEL)) {
										label = valueObject.toString();
									}

									switch (prop.getPropertyType()) {
										case FLOAT:
											try {
												final Float floatValue = ((Number)valueObject).floatValue();
												props.add(new PropertyHelper(prop.getKey(), prop.getFriendlyText(), floatValue, prop.getTags(), isHidden));
											} catch (Exception e) {
												// do nothing
											}
											break;
										case DOUBLE:
											try {
												final Double doubleValue = ((Number)valueObject).doubleValue();
												props.add(new PropertyHelper(prop.getKey(), prop.getFriendlyText(), doubleValue, prop.getTags(), isHidden));
											} catch (Exception e) {
												// do nothing
											}
											break;
										case INTEGER:
											try {
												final Integer intValue = ((Number)valueObject).intValue();
												props.add(new PropertyHelper(prop.getKey(), prop.getFriendlyText(), intValue, prop.getTags(), isHidden));
											} catch (Exception e) {
												// do nothing
											}
											break;
										case LONG:
											try {
												final Long longValue = ((Number)valueObject).longValue();
												props.add(new PropertyHelper(prop.getKey(), prop.getFriendlyText(), longValue, prop.getTags(), isHidden));
											} catch (Exception e) {
												// do nothing
											}
											break;
										case BOOLEAN:
											try {
												final Boolean boolValue = Boolean.parseBoolean(valueObject.toString());
												props.add(new PropertyHelper(prop.getKey(), prop.getFriendlyText(), boolValue, prop.getTags(), isHidden));
											} catch (Exception e) {
												// do nothing
											}
											break;
										case STRING:
											try {
												final String stringValue = valueObject.toString();
												props.add(new PropertyHelper(prop.getKey(), prop.getFriendlyText(), stringValue, prop.getTags(), isHidden));
											} catch (Exception e) {
												// do nothing
											}
											break;
										case DATE:
											try {
												DateTime jodaTime = new DateTime(valueObject, DateTimeZone.UTC);
												String dateString = jodaTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd"));
												props.add(new PropertyHelper(prop.getKey(), prop.getFriendlyText(), dateString, prop.getTags(), isHidden));
											} catch (Exception e) {
												// do nothing
											}
											break;
									default:
										break;
									}
								}
							}
						}
					}

					FL_Entity entity = createEntity(id, label, entry.getKey(), props);
					matches.add(new FL_SearchResult(1.0, entity));
				}
			}
		}

		return matches;
	}




	@Override
	public FL_PropertyDescriptors getDescriptors() throws AvroRemoteException {
		return  _applicationConfiguration.getEntityDescriptors();
	}

	public PropertyField.Provider getPropertyFieldProvider() {
		return _applicationConfiguration;
	}


	protected FL_Entity createEntity(String id, String label, String type, List<FL_Property> props) {
		String uid = _namespaceHandler.globalFromLocalEntityId(InfluentId.ACCOUNT, type, id);
		return new EntityHelper(uid, label, type, FL_EntityTag.ACCOUNT, props);
	}




	protected DataNamespaceHandler getNamespaceHandler() {
		return _namespaceHandler;
	}




	/* (non-Javadoc)
	 * @see influent.idl.FL_EntitySearch#getPropertyHeaders(influent.idl.FL_SearchResults, java.util.List)
	 */
	@Override
	public FL_PropertyDescriptors getKeyDescriptors(FL_SearchResults results,
			List<FL_OrderBy> resultOrder
	) throws AvroRemoteException {
		
		final FL_PropertyDescriptors all = _applicationConfiguration.getEntityDescriptors();
		final List<FL_PropertyDescriptor> props = all.getProperties();
		final List<FL_PropertyDescriptor> keyFields= new ArrayList<FL_PropertyDescriptor>();

		if (!props.isEmpty()) {
			for (FL_PropertyDescriptor pd : all.getProperties()) {
				if (FL_LevelOfDetail.KEY.equals(pd.getLevelOfDetail())) {
					keyFields.add(pd);
				}
			}
	
			if (keyFields.isEmpty()) {
				addIfNotNull(keyFields, DataPropertyDescriptorHelper.find(FL_RequiredPropertyKey.NAME.name(), props));
			}
			if (keyFields.isEmpty()) {
				keyFields.add(props.get(0));
			}
		}
		
		return FL_PropertyDescriptors.newBuilder()
			.setOrderBy(resultOrder)
			.setProperties(keyFields)
			.setTypes(all.getTypes())
			.build();
	}




	private void addIfNotNull(List<FL_PropertyDescriptor> list, FL_PropertyDescriptor item) {
		if (item != null) {
			list.add(item);
		}
	}
}
