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
import influent.idlhelper.PropertyDescriptorHelper;
import influent.server.configuration.ApplicationConfiguration;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.utilities.PropertyField;
import influent.server.utilities.InfluentId;
import oculus.aperture.spi.common.Properties;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result object for iterating through the results of a search.  Does paging, etc, behind the scenes.
 * @author msavigny
 *
 */

public class SolrEntitySearchIterator extends SolrBaseSearchIterator {

	public SolrEntitySearchIterator(
			DataNamespaceHandler namespaceHandler,
			SolrServer server,
			SolrQuery q,
			Properties config,
			FL_LevelOfDetail levelOfDetail,
			ApplicationConfiguration applicationConfiguration,
			PropertyField.Provider propertyFieldProvider
	) {
		_namespaceHandler = namespaceHandler;
		_server = server;
		_query = q;
		_config = config;
		_totalResults = -1;
		_levelOfDetail = levelOfDetail;
		_applicationConfiguration = applicationConfiguration;
		_fieldProvider = propertyFieldProvider;

		_curResults = new ArrayList<FL_SearchResult>(REFRESH_SIZE);
		for (int i=0;i<REFRESH_SIZE;i++) {
			_curResults.add(null);
		}
	}




	protected FL_Entity buildResultFromDocument(SolrDocument sd) {
		
		FL_Entity.Builder entityBuilder = FL_Entity.newBuilder();

		FL_PropertyDescriptors entityDescriptors = _applicationConfiguration.getEntityDescriptors();

		final String type = getTypeFromDocument(sd, entityDescriptors);

		String uid = sd.getFieldValue(PropertyDescriptorHelper.mapKey(FL_RequiredPropertyKey.ID.name(), entityDescriptors.getProperties(), type)).toString();

		// if multitype system, we parse the typed id to get the native id
		if (entityDescriptors.getTypes().size() > 1) {
			String[] splits = uid.split("\\.", 3);

			if (splits.length == 2) {
				uid = splits[1];
			}
		}

		entityBuilder.setProvenance(null);
		entityBuilder.setUncertainty(null);

		entityBuilder.setType(type);
		
		List<FL_Property> props = getPropertiesFromDocument(sd, type, entityDescriptors.getProperties());

		// Common tags
		List<FL_EntityTag> etags = new ArrayList<FL_EntityTag>();
		etags.add(FL_EntityTag.ACCOUNT);
		entityBuilder.setUid(_namespaceHandler.globalFromLocalEntityId(InfluentId.ACCOUNT, type, uid));
		entityBuilder.setTags(etags);
		entityBuilder.setProperties(props);
		
		return entityBuilder.build();
	}
}
