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
package influent.server.rest;

import influent.entity.clustering.utils.ClusterContextCache;
import influent.idl.FL_Cluster;
import influent.idl.FL_ClusteringDataAccess;
import influent.idl.FL_DateRange;
import influent.idl.FL_DirectionFilter;
import influent.idl.FL_Link;
import influent.idl.FL_LinkTag;
import influent.midtier.utilities.DateRangeBuilder;
import influent.server.utilities.DateTimeParser;
import influent.server.utilities.UISerializationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oculus.aperture.common.rest.ApertureServerResource;

import org.apache.avro.AvroRemoteException;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import com.google.inject.Inject;

public class AggregatedLinkResource extends ApertureServerResource{

	private final FL_ClusteringDataAccess clusterAccess;
	
	@Inject
	public AggregatedLinkResource(FL_ClusteringDataAccess clusterAccess) {
		this.clusterAccess = clusterAccess;
	}

	
	@Post
	public StringRepresentation getLinks(String jsonData) throws ResourceException {
		JSONObject jsonObj;
//		JSONArray clusterArr = null;
		JSONObject result = new JSONObject();
		Map<String, List<FL_Link>> links = new HashMap<String, List<FL_Link>>();
		
		List<String> sEntities;
		List<String> tEntities;
		String type = null, queryId = null/*, filterType = null*/;
		FL_DirectionFilter direction = FL_DirectionFilter.DESTINATION;
		FL_DateRange dateRange = null;
		
		try {
			jsonObj = new JSONObject(jsonData);

			String sessionId = jsonObj.getString("sessionId").trim();
			
			/*
			 * Valid arguments are:
			 *   - source
			 *   - destination
			 *   - both
			 */
			if (jsonObj.has("linktype")) {
				type = jsonObj.getString("linktype");
				
				if (type.equalsIgnoreCase ("source"))
					direction = FL_DirectionFilter.SOURCE;
				else if (type.equalsIgnoreCase ("destination"))
					direction = FL_DirectionFilter.DESTINATION;
				else 
					direction = FL_DirectionFilter.BOTH;
			}

			sEntities = UISerializationHelper.buildListFromJson(jsonObj, "sourceIds");
			tEntities = UISerializationHelper.buildListFromJson(jsonObj, "targetIds");

			
			DateTime startDate = (jsonObj.has("startdate")) ? DateTimeParser.parse(jsonObj.getString("startdate")) : null;
			DateTime endDate = (jsonObj.has("enddate")) ? DateTimeParser.parse(jsonObj.getString("enddate")) : null;
			if (startDate != null && endDate != null) {
				dateRange = DateRangeBuilder.getDateRange(startDate, endDate);
			}
			
			String srcContextId = jsonObj.getString("contextid").trim();
			String dstContextId = jsonObj.getString("targetcontextid").trim();
			
			List<String> srcEntities = new ArrayList<String>();
			List<String> dstEntities = new ArrayList<String>();
			Map<String, String> memberToClusterMap = new HashMap<String, String>();
			// Check if any of the entities are actually mutable clusters (i.e. internal clusters of files).
			// If they are, unroll them and create a map of parent-cluster-to-child-card ids.
			for (String entityId : sEntities){
				FL_Cluster flcluster = ClusterContextCache.instance.getFile(entityId, srcContextId);
				if(flcluster != null) {
					List<String>clusterMembers = new ArrayList<String>();
					clusterMembers.addAll(flcluster.getSubclusters());
					clusterMembers.addAll(flcluster.getMembers());
					for (String memberId : clusterMembers){
						memberToClusterMap.put(memberId, entityId);
					}
					srcEntities.addAll(clusterMembers);
				}
				else {
					srcEntities.add(entityId);
				}
			}

			// Check if any of the entities are actually mutable clusters (i.e. internal clusters of files).
			// If they are, unroll them and create a map of parent-cluster-to-child-card ids.
			for (String entityId : tEntities){
				FL_Cluster flcluster = ClusterContextCache.instance.getFile(entityId, dstContextId);
				if(flcluster != null) {
					List<String>clusterMembers = new ArrayList<String>();
					clusterMembers.addAll(flcluster.getSubclusters());
					clusterMembers.addAll(flcluster.getMembers());
					for (String memberId : clusterMembers){
						memberToClusterMap.put(memberId, entityId);
					}
					dstEntities.addAll(clusterMembers);
				}
				else {
					dstEntities.add(entityId);
				}
			}			
			links = clusterAccess.getFlowAggregation(srcEntities, dstEntities, direction, FL_LinkTag.FINANCIAL, dateRange, srcContextId, dstContextId, sessionId);			
			
			//links = EntityAggregatedLinks.getAggregatedLinks(sEntities, tEntities, direction, FL_LinkTag.FINANCIAL, dateRange, aggregationType, null, service);
			// Get the query id. This is used by the client to ensure
			// it only processes the latest response.
			queryId = jsonObj.getString("queryId").trim();

			if (!links.isEmpty()) {
				JSONObject dmap = new JSONObject();
				for (String key : links.keySet()) {
					JSONArray larr = new JSONArray();
					for (FL_Link link : links.get(key)) {
						// Iterate through the links and re-map the children to
						// the parent cluster where appropriate.
						String sId = link.getSource();
						String tId = link.getTarget();
						if (memberToClusterMap.containsKey(sId)){
							link.setSource(memberToClusterMap.get(sId));
						}
						if (memberToClusterMap.containsKey(tId)){
							link.setTarget(memberToClusterMap.get(tId));
						}

						larr.put(UISerializationHelper.toUIJson(link));
					}
					dmap.put(key, larr);
				}
				result.put("data",dmap);
			}
			

			result.put("queryId", queryId);
			result.put("sessionId", sessionId);

			return new StringRepresentation(result.toString(),MediaType.APPLICATION_JSON);
			
		} catch (JSONException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		} catch (AvroRemoteException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		}
		
		
	}
	

}
