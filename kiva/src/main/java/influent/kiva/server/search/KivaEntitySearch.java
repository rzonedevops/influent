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
package influent.kiva.server.search;

import influent.idl.FL_Constraint;
import influent.idl.FL_Entity;
import influent.idl.FL_EntitySearch;
import influent.idl.FL_Geocoding;
import influent.idl.FL_Property;
import influent.idl.FL_PropertyDescriptor;
import influent.idl.FL_PropertyMatchDescriptor;
import influent.idl.FL_PropertyType;
import influent.idl.FL_SearchResult;
import influent.idl.FL_SearchResults;
import influent.idlhelper.PropertyMatchDescriptorHelper;
import influent.idlhelper.SingletonRangeHelper;
import influent.midtier.solr.search.ConfigFileDescriptors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.AvroRemoteException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KivaEntitySearch implements FL_EntitySearch {

	private static final Logger s_logger = LoggerFactory.getLogger(KivaEntitySearch.class);
	
	private final FL_Geocoding _geocoding;
	private String _solrURL;
	private SolrServer _solr;
	
	private final ConfigFileDescriptors _cfd = new ConfigFileDescriptors();
	
	private static final Map<String, Double> s_basicSearchKeys;
	static {
		s_basicSearchKeys = new HashMap<String, Double>();
		//s_basicSearchKeys.put("loans_description_texts_en", 1.0);
		//s_basicSearchKeys.put("lenders_loanBecause", 1.0);
		//s_basicSearchKeys.put("teams_description", 1.0);
		
		s_basicSearchKeys.put("lenders_name", 1.0);
		s_basicSearchKeys.put("loans_name", 1.0);
		s_basicSearchKeys.put("partners_name", 1.0);
		//s_basicSearchKeys.put("teams_name", 1.0);
	}
	
	
	public KivaEntitySearch(String solrURL, String solrDescriptor, FL_Geocoding geocoding) {
		_solrURL = solrURL;
		_solr = new HttpSolrServer(_solrURL);
		_geocoding = geocoding;
		
		try {
			_cfd.readDescriptorsFromFile(solrDescriptor);
		} catch (IOException e) {
			s_logger.warn("Exception reading entity descriptors "+e.getMessage(),e);
		}
		
	}
	

	
	private String getPartnerIdsFromBrokers(PropertyMatchDescriptorHelper pst, Map<String, List<String>> brokerIds) {
		String k = (String) pst.getKey();
		String v = pst.getStringValue();
		
		if (k.equals("id")) {
			String sv ="";
			String[] ids = v.split(" ");
			for (String id : ids) {
				if (id.startsWith("p") && id.indexOf('-') > 0) {
					String nv = id.substring(0,id.indexOf('-'));
					List<String> realIds = brokerIds.get(nv);
					if (realIds == null) {
						realIds = new ArrayList<String>();
						brokerIds.put(nv,realIds);
						sv+=nv+" ";
					}
					realIds.add(id);
					
				} else {
					sv+=id+" ";
				}
			}
			v=sv;
		}
		
		
		FL_Constraint pstp = pst.getConstraint();
		if (pstp == FL_Constraint.NOT) {
			k="-"+k;
		} else if (pstp == FL_Constraint.FUZZY_PARTIAL_OPTIONAL) {
			v=v+"~";
		}
		//TODO : add the other parameter values
		
		//Add check for boost here
		String boost = "";
		if (s_basicSearchKeys.containsKey(k)) {
			Double weight = s_basicSearchKeys.get(k);
			if (weight != 1.0) {
				boost="^"+weight;
			}
		}
		return k+":("+v+")"+boost;
	}

	@Override
	public FL_SearchResults search(String searchTerms, List<FL_PropertyMatchDescriptor> terms, long start, long max, String type) throws AvroRemoteException {

		StringBuilder contructedSearch = new StringBuilder();
		if (searchTerms != null && searchTerms.length()>0) {
			if (terms == null) {
				terms= new ArrayList<FL_PropertyMatchDescriptor>();
			}
			for (String key : s_basicSearchKeys.keySet()) {
				terms.add(
						FL_PropertyMatchDescriptor.newBuilder()
							.setConstraint(FL_Constraint.REQUIRED_EQUALS)
							.setKey(key)
							.setRange(new SingletonRangeHelper(searchTerms, FL_PropertyType.STRING))
//							.setWeight(s_basicSearchKeys.get(key))
							.build()
				);
			}
		}
		
		Map<String, List<String>> brokerIds = new HashMap<String, List<String>>();
		
		int i=0;
		if (terms != null && terms.size() > 0) {
			for (;i<terms.size()-1;i++) {
				contructedSearch.append(getPartnerIdsFromBrokers(PropertyMatchDescriptorHelper.from(terms.get(i)),brokerIds));
				contructedSearch.append(" OR ");
			}
			contructedSearch.append(getPartnerIdsFromBrokers(PropertyMatchDescriptorHelper.from(terms.get(i)),brokerIds));
		}
		
		SolrQuery query = new SolrQuery();
		query.setQuery(contructedSearch.toString());
		query.setFields("*", "score");
		
		KivaEntitySearchIterator ssr = new KivaEntitySearchIterator(_solr, query, _geocoding);
		
		if (start >= 0) {
			ssr.setStartIndex((int) start);
		}
		if (max > 0) {
			ssr.setMaxResults((int)max);
		}
		
		List<FL_SearchResult> results = new ArrayList<FL_SearchResult>();
		while (ssr.hasNext()) {
			FL_SearchResult fsr = ssr.next();
			FL_Entity fle = (FL_Entity)fsr.getResult();
			if (brokerIds.containsKey(fle.getUid())) {
				List<String> realIds = brokerIds.get(fle.getUid());
				for (String rid : realIds) {
					List<FL_Property> copyProps = new ArrayList<FL_Property>();
					for (FL_Property oldProp : fle.getProperties()) {
						copyProps.add(FL_Property.newBuilder(oldProp).build());
					}
					
					FL_Entity copy = FL_Entity.newBuilder().setUid(rid).setProperties(copyProps).setTags(fle.getTags()).setProvenance(null).setUncertainty(null).build();
					FL_SearchResult srcopy = FL_SearchResult.newBuilder().setResult(copy).setScore(fsr.getScore()).build();
					
					results.add(srcopy);
				}
			} else {
				results.add(fsr);
			}
		}
		return new FL_SearchResults((long)ssr.getTotalResults(), results);
	}

	@Override
	public Map<String, List<FL_PropertyDescriptor>> getDescriptors() throws AvroRemoteException {
		return _cfd.getEntityDescriptors();
	}

}
