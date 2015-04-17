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

package influent.kiva.server.search;

import influent.idl.*;
import influent.idlhelper.DataPropertyDescriptorHelper;
import influent.idlhelper.PropertyHelper;
import influent.midtier.kiva.data.KivaTypes;
import influent.server.configuration.ApplicationConfiguration;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.search.SolrEntitySearchIterator;
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

public class KivaEntitySearchIterator extends SolrEntitySearchIterator {



	public KivaEntitySearchIterator(
		DataNamespaceHandler namespaceHandler,
		SolrServer server,
		SolrQuery q,
		Properties config,
		FL_LevelOfDetail levelOfDetail,
		ApplicationConfiguration applicationConfiguration,
		PropertyField.Provider propertyFieldProvider
	) {
		super(namespaceHandler, server, q, config, levelOfDetail, applicationConfiguration, propertyFieldProvider);
	}




	@Override
	protected FL_Entity buildResultFromDocument(SolrDocument sd) {

		FL_Entity.Builder entityBuilder = FL_Entity.newBuilder();

		String type = (String)sd.getFieldValue(FL_RequiredPropertyKey.TYPE.name().toLowerCase());

		FL_PropertyDescriptors entityDescriptors = _applicationConfiguration.getEntityDescriptors();

		String uid = sd.getFieldValue(DataPropertyDescriptorHelper.mapKey(FL_RequiredPropertyKey.ID.name(), entityDescriptors.getProperties(), type)).toString();

		if (entityDescriptors.getTypes().size() > 1) {
			String[] splits = uid.split("\\.", 3);

			if (splits.length == 2) {
				uid = splits[1];
			}
		}

		entityBuilder.setProvenance(null);
		entityBuilder.setUncertainty(null);

		entityBuilder.setType(type);

		List<FL_EntityTag> etags = new ArrayList<FL_EntityTag>();

		List<FL_Property> props = getPropertiesFromDocument(sd, type, entityDescriptors.getProperties());

		// --- FEATURE DEMOS -------------------------------------------------------------------------------------------

		// Prompt for details
		if (uid.equals("b320877")) {
			etags.add(FL_EntityTag.PROMPT_FOR_DETAILS);
		}

		// Multiple image carousel
		if (uid.equals("b320786")) {

			List<Object> imageUrlList = new ArrayList<Object>(3);
			imageUrlList.add("http://www.kiva.org/img/w400/810835.jpg");
			imageUrlList.add("http://www.kiva.org/img/w400/146773.jpg");
			imageUrlList.add("http://www.kiva.org/img/w400/148448.jpg");

			FL_Property imageProp = PropertyHelper.getPropertyByKey(props, "image");
			imageProp.setRange(
				FL_ListRange.newBuilder().setType(FL_PropertyType.IMAGE).setValues(imageUrlList).build()
			);
		}

		// Make lenders parchmenty
		if (type.equals("lender")) {
			final double notVeryConfidentDemonstration = 0.4 * Math.random();

			entityBuilder.setUncertainty(FL_Uncertainty.newBuilder().setConfidence(notVeryConfidentDemonstration).build());
		}

		// -------------------------------------------------------------------------------------------------------------

		etags.add(FL_EntityTag.ACCOUNT);
		char entityClass = type.equals(KivaTypes.TYPE_PARTNER) ? InfluentId.ACCOUNT_OWNER : InfluentId.ACCOUNT;
		entityBuilder.setUid(_namespaceHandler.globalFromLocalEntityId(entityClass, type, uid));
		entityBuilder.setTags(etags);
		entityBuilder.setProperties(props);

		return entityBuilder.build();
	}




	protected void _appendPartnerProperties(List<FL_Property> props) {
		final FL_Property delinquency = PropertyHelper.getPropertyByKey(props, "partner_delinquencyRate");

		if (delinquency != null) {
			final Number number = (Number) PropertyHelper.from(delinquency).getValue();

			if (number != null && number.doubleValue() >= 5.0) {
				props.add(new PropertyHelper(FL_PropertyTag.WARNING, "high delinquency rate"));
			}
		}

		final FL_Property defaultRate = PropertyHelper.getPropertyByKey(props, "partners_defaultRate");

		if (defaultRate != null) {
			final Number number = (Number) PropertyHelper.from(defaultRate).getValue();

			if (number != null && number.doubleValue() >= 1.0) {
				props.add(new PropertyHelper(FL_PropertyTag.WARNING, "high default rate"));
			}
		}
	}
}
