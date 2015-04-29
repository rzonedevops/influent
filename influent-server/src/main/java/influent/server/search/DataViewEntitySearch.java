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
import influent.idlhelper.PropertyDescriptorHelper;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.dataaccess.SearchSQLHelper;
import influent.server.sql.SQLBuilder;
import influent.server.utilities.*;
import influent.server.configuration.ApplicationConfiguration;

import oculus.aperture.spi.common.Properties;

import org.apache.avro.AvroRemoteException;
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
	protected final SearchSQLHelper _sqlHelper;

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
				_applicationConfiguration,
				FL_Entity.class
		);
	}




	@Override
	public FL_SearchResults search(
		Map<String, List<FL_PropertyMatchDescriptor>> termMap,
		List<FL_OrderBy> orderBy,
		long start,
		long max,
		FL_LevelOfDetail levelOfDetail
	) throws AvroRemoteException {

		ArrayList<FL_SearchResult> matches = new ArrayList<FL_SearchResult>();

		List<Object> entityList = _sqlHelper.getObjectsFromTerms(termMap, orderBy, levelOfDetail, true);

		long end = start + max < entityList.size() ? start + max : entityList.size();
		for (long i = start; i < end; i++) {
			Object entity = entityList.get((int) i);
			matches.add(new FL_SearchResult(1.0, entity));
		}

		return FL_SearchResults.newBuilder()
			.setTotal((long) matches.size())
			.setResults(matches)
			.setLevelOfDetail(levelOfDetail)
			.build();
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
				addIfNotNull(keyFields, PropertyDescriptorHelper.find(FL_RequiredPropertyKey.NAME.name(), props));
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
