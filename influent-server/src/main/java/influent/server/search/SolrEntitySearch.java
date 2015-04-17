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

import influent.idl.FL_EntitySearch;
import influent.idl.FL_LevelOfDetail;
import influent.idl.FL_OrderBy;
import influent.idl.FL_PropertyMatchDescriptor;
import influent.idl.FL_ReservedPropertyKey;
import influent.idl.FL_SearchResult;
import influent.idl.FL_SearchResults;
import influent.idlhelper.DataPropertyDescriptorHelper;
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

public class SolrEntitySearch extends DataViewEntitySearch implements FL_EntitySearch {

	private SolrServer _solr;
	private Properties _config;



	public SolrEntitySearch(
		Properties config,
		SQLConnectionPool connectionPool,
		DataNamespaceHandler namespaceHandler,
		SQLBuilder sqlBuilder
	) {
		super(config, connectionPool, namespaceHandler, sqlBuilder);

		_solr = new HttpSolrServer(config.getString("influent.midtier.solr.url", "http://localhost:8983"));
		_config = config;
	}




	@Override
	public FL_SearchResults search(
		Map<String, List<FL_PropertyMatchDescriptor>> termMap,
		List<FL_OrderBy> orderBy,
		long start,
		long max
	) throws AvroRemoteException {
		return search(termMap, orderBy, start, max, FL_LevelOfDetail.SUMMARY);
	}




	public FL_SearchResults search(
		Map<String, List<FL_PropertyMatchDescriptor>> termMap,
		List<FL_OrderBy> orderBy,
		long start,
		long max,
		FL_LevelOfDetail lod
	) throws AvroRemoteException {

		ArrayList<FL_SearchResult> results = new ArrayList<FL_SearchResult>();
		SolrEntitySearchIterator ssr;

		// Form the query
		String searchStr = SearchSolrHelper.toSolrQuery(termMap, getDescriptors(), getPropertyFieldProvider());

		// issue the query
		SolrQuery query = new SolrQuery();
		query.setQuery(searchStr);
		query.setFields("*","score");

		// form a union of sort by fields for all types
		orderBy = DataPropertyDescriptorHelper.mapOrderBy(orderBy, getDescriptors().getProperties(), termMap.keySet());

		if (orderBy != null) {
			for (FL_OrderBy ob : orderBy) {
				String key= (ob.getPropertyKey().equals(FL_ReservedPropertyKey.MATCH.name()))? "score": ob.getPropertyKey();
				
				query.addSortField(key, ob.getAscending()? ORDER.asc : ORDER.desc);
			}
		}

		ssr = buildSolrEntitySearchIterator(
			getNamespaceHandler(),
			_solr,
			query,
			_config,
			lod,
			_applicationConfiguration,
            getPropertyFieldProvider()
        );

		if (start >= 0) {
			ssr.setStartIndex((int)start);
		}
		if (max > 0) {
			ssr.setMaxResults((int)max);
		}

		// Add results from the matching documents
		while (ssr.hasNext()) {
			FL_SearchResult fsr = ssr.next();
			results.add(fsr);
		}

		return FL_SearchResults.newBuilder()
			.setTotal(ssr.getTotalResults())
			.setResults(results)
			.setLevelOfDetail(lod)
			.build();
	}




	protected SolrEntitySearchIterator buildSolrEntitySearchIterator(
		DataNamespaceHandler namespaceHandler,
		SolrServer solr,
		SolrQuery query,
		Properties config,
		FL_LevelOfDetail levelOfDetail,
		ApplicationConfiguration applicationConfiguration,
		PropertyField.Provider propertyFieldProvider
	) {
		return new SolrEntitySearchIterator(namespaceHandler, solr, query, config, levelOfDetail, applicationConfiguration, propertyFieldProvider);
	}
}
