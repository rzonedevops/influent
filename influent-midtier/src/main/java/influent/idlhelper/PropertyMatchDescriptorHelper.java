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
package influent.idlhelper;

import influent.idl.FL_BoundedRange;
import influent.idl.FL_Constraint;
import influent.idl.FL_ListRange;
import influent.idl.FL_PropertyMatchDescriptor;
import influent.idl.FL_PropertyType;
import influent.idl.FL_SingletonRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.util.ClientUtils;

public class PropertyMatchDescriptorHelper extends FL_PropertyMatchDescriptor {
	
	public static PropertyMatchDescriptorHelper from(FL_PropertyMatchDescriptor descriptor) {
		if (descriptor instanceof PropertyMatchDescriptorHelper) return (PropertyMatchDescriptorHelper)descriptor;
		
		PropertyMatchDescriptorHelper helper = new PropertyMatchDescriptorHelper();
		helper.setKey(descriptor.getKey());
		helper.setConstraint(descriptor.getConstraint());
		helper.setVariable(descriptor.getVariable());
		helper.setInclude(descriptor.getInclude());
		helper.setRange(descriptor.getRange());
		return helper;
	}
	
	public String toJson() throws IOException {
		return SerializationHelper.toJson(this);
	}
	
	public static String toJson(FL_PropertyMatchDescriptor descriptor) throws IOException {
		return SerializationHelper.toJson(descriptor);
	}
	
	public static String toJson(List<FL_PropertyMatchDescriptor> descriptors) throws IOException {
		return SerializationHelper.toJson(descriptors, FL_PropertyMatchDescriptor.getClassSchema());
	}

	public static String toJson(Map<String, List<FL_PropertyMatchDescriptor>> map) throws IOException {
		return SerializationHelper.toJson(map, FL_PropertyMatchDescriptor.getClassSchema());
	}
	
	public static FL_PropertyMatchDescriptor fromJson(String json) throws IOException {
		return SerializationHelper.fromJson(json, FL_PropertyMatchDescriptor.getClassSchema());
	}
	
	public static List<FL_PropertyMatchDescriptor> listFromJson(String json) throws IOException {
		return SerializationHelper.listFromJson(json, FL_PropertyMatchDescriptor.getClassSchema());
	}
	
	public static Map<String, List<FL_PropertyMatchDescriptor>> mapFromJson(String json) throws IOException {
		return SerializationHelper.mapFromJson(json, FL_PropertyMatchDescriptor.getClassSchema());
	}

	/**
	 * Returns a Solr query clause to represent the descriptor supply. Since descriptors do not
	 * yet include weights the weight is supplied here as a separate parameter.
	 * 
	 * @param descriptor
	 * 		The match specification
	 * 
	 * @param weight
	 * 		The weight to give the parameter, where 1.0 is the default if unspecified
	 * 
	 * @return
	 */
	public static String toSolrClause(FL_PropertyMatchDescriptor descriptor, Double weight) {
		final PropertyMatchDescriptorHelper pst = from(descriptor);
		
		String k = pst.getKey();
		String v = pst.getStringValue();

		// validate it
		if (k == null || k.trim().isEmpty() ||  v == null || v.trim().isEmpty()) {
			return null;
		}
		
		FL_Constraint pstp = pst.getConstraint();
		
		// TODO : add the other constraint types
		if (pstp == FL_Constraint.NOT) {
			k="-"+k;
			
		} else if (pstp == FL_Constraint.FUZZY_PARTIAL_OPTIONAL) {
			
			// have to match every token fuzzily. TODO: research what Solr considers tokens!
			final String vs[] = v.split("\\s+");

			// divide the weight by the number of contributing matches
			double dweight = 0.01*(int)(((weight != null)? weight : 1.0) / (0.01*vs.length));

			
			StringBuilder sb = new StringBuilder();
			
			for (String iv : vs) {
				if (sb.length() != 0) {
					sb.append(" OR ");
				} 
				
				sb.append(k);
				sb.append(":(");
				sb.append(ClientUtils.escapeQueryChars(iv));
				sb.append("~)^");
				sb.append(dweight);
			}

			return sb.toString();
		}
		
		// Add check for weight boost here
		String boost = (weight != null && weight.doubleValue() != 1.0)? boost="^"+ weight : "";
		
		return k+":(" + ClientUtils.escapeQueryChars(v) +")"+boost;
	}
	
	/**
	 * Returns an OR'd series of Solr clauses to represent the list of terms specified.
	 * 
	 * @param basicQuery
	 * 		A search over default fields supplied by basicQueryFieldWeights
	 * 
	 * @param basicQueryFieldWeights
	 * 		A map of default field names to weightings, where 1.0 is the default weight.
	 * 
	 * @param advancedTerms
	 * 		A list of explicitly defined terms
	 * 
	 * @return
	 */
	public static String toSolrQuery(String basicQuery, Map<String, Double> basicQueryFieldWeights, List<FL_PropertyMatchDescriptor> advancedTerms) {
		
		// copy terms to merge basic with advanced.
		final List<FL_PropertyMatchDescriptor> terms = advancedTerms != null?
				new ArrayList<FL_PropertyMatchDescriptor>(advancedTerms) : new ArrayList<FL_PropertyMatchDescriptor>();
	
		// first add basic query terms.
		if (basicQuery != null) {
			basicQuery = basicQuery.trim();
			
			while(!basicQuery.isEmpty()) {
				
				// quoted?
				if (basicQuery.charAt(0) == '\"') {
					int endquote = basicQuery.indexOf('"', 1);

					// found the end quote
					if (endquote != -1) {
						String exactMatch = basicQuery.substring(1, endquote);
						
						for (String key : basicQueryFieldWeights.keySet()) {
							terms.add(
								FL_PropertyMatchDescriptor.newBuilder()
									.setConstraint(FL_Constraint.REQUIRED_EQUALS)
									.setKey(key)
									.setRange(new SingletonRangeHelper(exactMatch, FL_PropertyType.STRING))
									.build()
							);
						}
						
					} else {
						
						// otherwise process as unquoted next cycle
						endquote = 0;
					}
					
					// break if at the end, otherwise the subsequent line will index off the end.
					if (endquote+1 == basicQuery.length()) {
						break;
					}
					
					// set up next block
					basicQuery = basicQuery.substring(endquote+1).trim();
					
				} else {
					String fuzzyMatch = basicQuery;
					final int startquote = basicQuery.indexOf('"');

					// found the start of a quote, which is our end then
					if (startquote != -1) {
						fuzzyMatch = basicQuery.substring(0, startquote).trim();
						
						basicQuery = basicQuery.substring(startquote);
						
					} else {
						
						// else we will be done after this
						basicQuery = "";
					}
					
					for (String key : basicQueryFieldWeights.keySet()) {
						terms.add(
							FL_PropertyMatchDescriptor.newBuilder()
								.setConstraint(FL_Constraint.FUZZY_PARTIAL_OPTIONAL)
								.setKey(key)
								.setRange(new SingletonRangeHelper(fuzzyMatch, FL_PropertyType.STRING))
								.build()
						);
					}
				}
			}
		}
		
		// now form it into a query
		StringBuilder query = new StringBuilder();
		
		for (FL_PropertyMatchDescriptor term : terms) {
			final String clause = toSolrClause(term, basicQueryFieldWeights.get(term.getKey()));
			
			if (clause != null) {
				if (query.length() > 0) {
					query.append(" OR ");
				}
				query.append(clause);
			}
		}
				
		return query.toString();
	}

	
	
	
	public FL_PropertyType getType() {
		Object range = getRange();
		if (range instanceof FL_SingletonRange)
			return ((FL_SingletonRange)range).getType();
		else if (range instanceof FL_ListRange)
			return ((FL_ListRange)range).getType();
		else if (range instanceof FL_BoundedRange)
			return ((FL_BoundedRange)range).getType();
		return null;
	}

	public Object getValue() {
		Object range = getRange();
		if (range instanceof FL_SingletonRange) {
			return ((FL_SingletonRange)range).getValue();
		}
		else if (range instanceof FL_ListRange) {
			return ((FL_ListRange)range).getValues().iterator().next();
		}
		else if (range instanceof FL_BoundedRange) {
			FL_BoundedRange bounded = (FL_BoundedRange)range;
			return bounded.getStart() != null ? bounded.getStart() : bounded.getEnd();
		}
		return null;
	}

	public String getStringValue() {
		Object value = getValue();
		return (value != null) ? value.toString() : "";
	}
}
