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
import influent.idlhelper.DataPropertyDescriptorHelper;
import influent.idlhelper.SingletonRangeHelper;
import influent.server.configuration.ApplicationConfiguration;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.dataaccess.SearchSolrHelper;
import influent.server.sql.SQLBuilder;
import influent.server.utilities.PropertyField;
import influent.server.utilities.SQLConnectionPool;
import oculus.aperture.spi.common.Properties;
import org.apache.avro.AvroRemoteException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SolrLinkSearch extends DataViewLinkSearch implements FL_LinkSearch {

	private SolrServer _solr;
	private Properties _config;

	//----------------------------------------------------------------------

	public SolrLinkSearch(
		Properties config,
		SQLConnectionPool connectionPool,
		DataNamespaceHandler namespaceHandler,
		SQLBuilder sqlBuilder,
		FL_ClusteringDataAccess clusterDataAccess
	) {
		super(config, connectionPool, namespaceHandler, sqlBuilder, clusterDataAccess);

		_solr = new HttpSolrServer(config.getString("influent.midtier.solr.url", "http://localhost:8983"));
		_config = config;
	}




	protected DataNamespaceHandler getNamespaceHandler() {
		return _namespaceHandler;
	}




	@Override
	public FL_SearchResults search(
		Map<String, List<FL_PropertyMatchDescriptor>> termMap,
		List<FL_OrderBy> orderBy,
		long start,
		long max,
		FL_LevelOfDetail levelOfDetail
	) throws AvroRemoteException {
		ArrayList<FL_SearchResult> results = new ArrayList<FL_SearchResult>();
		SolrLinkSearchIterator ssr;

		try {
			List<String> ids = new ArrayList<String>();

			// Collect all the ids, for mapping later
			for (Map.Entry<String, List<FL_PropertyMatchDescriptor>> entry : termMap.entrySet()) {
				List<FL_PropertyMatchDescriptor> termsByType = entry.getValue();
				for (FL_PropertyMatchDescriptor term : termsByType) {

					// Find keys that are built in ID types for transactions
					if (term.getKey().equals(FL_RequiredPropertyKey.FROM.name()) ||
						term.getKey().equals(FL_RequiredPropertyKey.TO.name()) ||
						term.getKey().equals(FL_RequiredPropertyKey.ENTITY.name()) ||
						term.getKey().equals(FL_RequiredPropertyKey.LINKED.name())
					) {

						// Strip out the native ID
						Object range = term.getRange();
						if (range instanceof FL_SingletonRange) {
							String val = (String) SingletonRangeHelper.value(range);
							ids.add(val);
						}
						else if (range instanceof FL_ListRange) {

							List<Object> values = ((FL_ListRange)range).getValues();
							List<Object> values2 = new ArrayList<Object>();
							for (Object obj : values) {
								String val = (String)obj;
								values2.add(val);
								ids.add(val);
							}
						}
					}
				}
			}

			// Form the query
			String searchStr = SearchSolrHelper.toSolrQuery(termMap, getDescriptors(), getPropertyFieldProvider());

			// issue the query
			SolrQuery query = new SolrQuery();
			query.setQuery(searchStr);
			query.setFields("*", "score");

			String solrGroupField = getDescriptors().getGroupField();
			if (solrGroupField != null) {
				query.setParam("group", true);
				query.setParam("group.field", solrGroupField);
				query.setParam("group.limit", levelOfDetail == FL_LevelOfDetail.FULL ? "-1" : "3");
				query.setParam("group.ngroups", true);
			}

			// form a union of sort by fields for all types
			orderBy = DataPropertyDescriptorHelper.mapOrderBy(orderBy, getDescriptors().getProperties(), termMap.keySet());

			if (orderBy != null) {
				for (FL_OrderBy ob : orderBy) {
					String key = (ob.getPropertyKey().equals(FL_ReservedPropertyKey.MATCH.name()))? "score": ob.getPropertyKey();
					
					query.addSortField(key, ob.getAscending()? ORDER.asc : ORDER.desc);
				}
			}

			ssr = buildSolrLinkSearchIterator(
				getNamespaceHandler(),
				_solr,
				query,
				_config,
				levelOfDetail,
				_applicationConfiguration,
				getPropertyFieldProvider()
			);

			if (start >= 0) {
				ssr.setStartIndex((int)start);
			} 
			if (max > 0) {
				ssr.setMaxResults((int) max);
			}
			
			// Add results from the matching documents
			while (ssr.hasNext()) {
				FL_SearchResult fsr = ssr.next();
				results.add(fsr);
			}

		} catch (Exception e) {
			throw new AvroRemoteException(e);
		}
		
		return FL_SearchResults.newBuilder().setTotal((long)ssr.getTotalResults()).setResults(results).setLevelOfDetail(levelOfDetail).build();
	}
	
	public SolrLinkSearchIterator buildSolrLinkSearchIterator(
		DataNamespaceHandler namespaceHandler,
		SolrServer solr,
		SolrQuery query,
		Properties config,
		FL_LevelOfDetail levelOfDetail,
		ApplicationConfiguration applicationConfiguration,
		PropertyField.Provider propertyFieldProvider
	) {
		return new SolrLinkSearchIterator(namespaceHandler, solr, query, config, levelOfDetail, applicationConfiguration, propertyFieldProvider);
	}
}
