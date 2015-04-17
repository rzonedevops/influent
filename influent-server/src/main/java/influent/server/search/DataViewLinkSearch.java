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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import influent.idl.*;
import influent.idlhelper.*;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.dataaccess.SearchSQLHelper;
import influent.server.sql.SQLBuilder;
import influent.server.utilities.*;
import influent.server.configuration.ApplicationConfiguration;
import static influent.server.configuration.ApplicationConfiguration.SystemPropertyKey.*;

import oculus.aperture.spi.common.Properties;

import org.apache.avro.AvroRemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Created by cdickson on 22/10/2014.
 */
public class DataViewLinkSearch implements FL_LinkSearch {

	protected final SQLConnectionPool _connectionPool;
	protected final DataNamespaceHandler _namespaceHandler;
	protected final SQLBuilder _sqlBuilder;
	protected final FL_ClusteringDataAccess _clusterDataAccess;

	protected SearchSQLHelper _sqlHelper;

	protected final ApplicationConfiguration _applicationConfiguration;

	protected static final Logger s_logger = LoggerFactory.getLogger(DataViewEntitySearch.class);
	protected Logger getLogger() {
		return s_logger;
	}

	protected static FL_PropertyDescriptors _descriptors = null;

	private final static boolean USE_PREPARED_STATEMENTS = false;

	public DataViewLinkSearch(
			Properties config,
			SQLConnectionPool connectionPool,
			DataNamespaceHandler namespaceHandler,
			SQLBuilder sqlBuilder,
			FL_ClusteringDataAccess clusterDataAccess
	) {
		_connectionPool = connectionPool;
		_namespaceHandler = namespaceHandler;
		_sqlBuilder = sqlBuilder;
		_clusterDataAccess = clusterDataAccess;
		_applicationConfiguration = ApplicationConfiguration.getInstance(config);

		try {
			_sqlHelper = new SearchSQLHelper(_sqlBuilder, _connectionPool, _namespaceHandler, getDescriptors()).isLinkHelper(true);
		} catch (AvroRemoteException e) {
			e.printStackTrace();
		}
	}





	@Override
	public FL_SearchResults search(
		Map<String, List<FL_PropertyMatchDescriptor>> termMap,
		List<FL_OrderBy> orderBy,
		long start,
		long max,
		FL_LevelOfDetail levelOfDetail
	) throws AvroRemoteException {

		FL_SearchResults results = FL_SearchResults.newBuilder()
			.setTotal(0)
			.setResults(new ArrayList<FL_SearchResult>())
			.setLevelOfDetail(FL_LevelOfDetail.FULL)
			.build();

		FL_PropertyDescriptors linkDescriptors = _applicationConfiguration.getLinkDescriptors();
		FL_PropertyDescriptors entityDescriptors = _applicationConfiguration.getEntityDescriptors();

		Boolean isMultitype = (entityDescriptors.getTypes().size() > 1);

		ArrayList<FL_SearchResult> matches = new ArrayList<FL_SearchResult>();

		// utility classes
		final ArrayList<Pair<String, Object>> fieldValues = new ArrayList<Pair<String, Object>>();
		
		int totalResults = 0;

		// Query by type
		for (Map.Entry<String, List<FL_PropertyMatchDescriptor>> entry : termMap.entrySet()) {

			if (USE_PREPARED_STATEMENTS) {
				// TODO: Prepared statement version
				s_logger.error("Prepared statements not yet supported!");
			} else {
				final String type = entry.getKey();
				List<FL_PropertyMatchDescriptor> term = entry.getValue();
				List<String> columns = getColumnNamesForLinks(type);
				String table = _applicationConfiguration.getTable(type, FIN_LINK.name(), FIN_LINK.name());

				// TODO: extract duplicate code between entity and link search, and DataViewDataAccess, and push into new class.
				// TODO: hash map is an expensive way to represent this, which is why db interfaces never do it. typically you
				//       want to lookup the column index by name once, or if not just do a linear search for the column name each time.
				List<Map<String, Object>> queryResults = _sqlHelper.fetchColumnsForTerms(table, columns, term, type, orderBy);
				totalResults = queryResults.size();

				if (queryResults.size() < start) {
					break;
				}
				long end = start + max < queryResults.size() ? start + max : queryResults.size();
				for (long i = start; i < end; i++) {

					Map<String, Object> resultMap = queryResults.get((int)i);
					String fromUID = null;
					String toUID = null;
					String linkUID = null;
					List<FL_Property> props = new ArrayList<FL_Property>();

					for (FL_PropertyDescriptor prop : linkDescriptors.getProperties()) {

						if (prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.ENTITY.name()) ||
							prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.LINKED.name())
						) {
							continue;
						}

						for (FL_TypeMapping map : prop.getMemberOf()) {
							if (map.getType().equalsIgnoreCase(type)) {
								boolean isHidden = prop.getLevelOfDetail().equals(FL_LevelOfDetail.HIDDEN);

								if (prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.FROM.name())) {

									String from = resultMap.get(map.getMemberKey()).toString();
									char fromClass = InfluentId.ACCOUNT;
									String fromType;
									if (isMultitype) {
										fromType = InfluentId.fromTypedId(fromClass, from).getIdType();
									} else {
										fromType = _applicationConfiguration.getEntityDescriptors().getTypes().get(0).getKey();
									}
									fromUID = _namespaceHandler.globalFromLocalEntityId(fromClass, fromType, from);

								} else if (prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.TO.name())) {

									String to = resultMap.get(map.getMemberKey()).toString();
									char toClass = InfluentId.ACCOUNT;
									String toType;
									if (isMultitype) {
										toType = InfluentId.fromTypedId(toClass, to).getIdType();
									} else {
										toType = _applicationConfiguration.getEntityDescriptors().getTypes().get(0).getKey();
									}
									toUID = _namespaceHandler.globalFromLocalEntityId(toClass, toType, to);

								} else if (prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.ID.name())) {

									String id = resultMap.get(map.getMemberKey()).toString();
									linkUID = _namespaceHandler.globalFromLocalEntityId(InfluentId.LINK, type, id);

								} else if (prop.getPropertyType() == FL_PropertyType.GEO) {

									List<PropertyField> fields = getPropertyFieldProvider().getFields(prop.getKey());

									if (fields != null) {
										fieldValues.clear();

										for (PropertyField field : fields) {
											final String fieldKey = DataPropertyDescriptorHelper.getFieldname(field.getProperty(), type, null);

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
									props.add(new PropertyHelper(prop.getKey(), prop.getFriendlyText(), valueObject, prop.getTags(), isHidden));
								}
							}
						}
					}

					FL_Link link = createLink(linkUID, fromUID, toUID, type, props);
					matches.add(FL_SearchResult.newBuilder()
						.setResult(link)
						.build()
					);
				}
			}
		}

		results.setResults(matches);
		results.setTotal((long) totalResults);
		return results;
	}




	private List<String> getColumnNamesForLinks(String type) throws AvroRemoteException {
		Set<String> columns = new HashSet<String>();

		for (FL_PropertyDescriptor prop : getDescriptors().getProperties()) {
			if (prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.ENTITY.name()) ||
				prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.LINKED.name())
			) {
				continue;
			}

			for (FL_TypeMapping map : prop.getMemberOf()) {
				if (map.getType().equalsIgnoreCase(type)) {

					// non-primitive objects need decomposition
					if (prop.getPropertyType() == FL_PropertyType.GEO) {
						List<PropertyField> fields = getPropertyFieldProvider().getFields(prop.getKey());

						if (fields != null) {
							for (PropertyField field : fields) {
								final String fieldKey = DataPropertyDescriptorHelper.getFieldname(field.getProperty(), type, null);

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

		return new ArrayList<String>(columns);
	}




	@Override
	public FL_PropertyDescriptors getDescriptors() throws AvroRemoteException {
		if (_descriptors == null) {
			_descriptors = _applicationConfiguration.getLinkDescriptors();
		}
		
		return _descriptors;
	}

	public PropertyField.Provider getPropertyFieldProvider() {
		return _applicationConfiguration;
	}



	protected FL_Link createLink(String uid, String from, String to, String type, List<FL_Property> props) {
		return new LinkHelper(uid, from, to, type, props, null);
	}




	/*
	 * (non-Javadoc)
	 * @see influent.idl.FL_LinkSearch#getPropertyHeaders(influent.idl.FL_SearchResults, java.util.List)
	 */
	@Override
	public FL_PropertyDescriptors getKeyDescriptors(FL_SearchResults results, List<FL_OrderBy> resultOrder) throws AvroRemoteException {
		final FL_PropertyDescriptors all = _applicationConfiguration.getLinkDescriptors();
		final List<FL_PropertyDescriptor> props = all.getProperties();
		final List<FL_PropertyDescriptor> keyFields= new ArrayList<FL_PropertyDescriptor>();

		if (!props.isEmpty()) {
			for (FL_PropertyDescriptor pd : props) {
				if (FL_LevelOfDetail.KEY.equals(pd.getLevelOfDetail())) {
					keyFields.add(pd);
				}
			}
	
			if (keyFields.isEmpty()) {
				addIfNotNull(keyFields, DataPropertyDescriptorHelper.find(FL_RequiredPropertyKey.FROM.name(), props));
				addIfNotNull(keyFields, DataPropertyDescriptorHelper.find(FL_RequiredPropertyKey.TO.name(), props));
				addIfNotNull(keyFields, DataPropertyDescriptorHelper.find(FL_RequiredPropertyKey.DATE.name(), props));
				addIfNotNull(keyFields, DataPropertyDescriptorHelper.find(FL_RequiredPropertyKey.AMOUNT.name(), props));
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
		if (item != null && !list.contains(item)) {
			list.add(item);
		}
	}
}
