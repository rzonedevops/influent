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

import influent.entity.clustering.utils.ClusterCollapser;
import influent.entity.clustering.utils.ClusterContextCache;
import influent.idl.FL_Cluster;
import influent.idl.FL_ClusteringDataAccess;
import influent.idl.FL_DataAccess;
import influent.idl.FL_DateRange;
import influent.idl.FL_DirectionFilter;
import influent.idl.FL_Entity;
import influent.idl.FL_Link;
import influent.idl.FL_LinkTag;
import influent.idl.FL_Property;
import influent.idl.FL_PropertyType;
import influent.idl.FL_SingletonRange;
import influent.idlhelper.PropertyHelper;
import influent.midtier.Pair;
import influent.midtier.utilities.DateRangeBuilder;
import influent.server.utilities.DateTimeParser;
import influent.server.utilities.UISerializationHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class RelatedLinkResource extends ApertureServerResource{


	private final FL_ClusteringDataAccess clusterAccess;
	private final FL_DataAccess entityAccess;
	
	private static final Logger s_logger = LoggerFactory.getLogger(RelatedLinkResource.class);
	
	@Inject
	public RelatedLinkResource(FL_ClusteringDataAccess clusterAccess, FL_DataAccess entityAccess) {
		this.clusterAccess = clusterAccess;
		this.entityAccess = entityAccess;
	}


	
	@Post
	public StringRepresentation getLinks(String jsonData) throws ResourceException {
		JSONObject result = new JSONObject();
//		Map<String, List<Link>> data = new HashMap<String, List<Link>>();

		// ??? What are these supposed to do ???
		// This list is populated with the entities at the other end of the discovered links, and pass to the UI.
		//This is so the UI doesn't need to make a second call to get the entities at the end of the found links.
		List<Object> targets = new ArrayList<Object>();
		boolean fetchTargetEntities = true;
		
		String queryId = null;
		try {
			JSONObject jsonObj = new JSONObject(jsonData);
			
			String sessionId = jsonObj.getString("sessionId").trim();

			FL_DirectionFilter direction = FL_DirectionFilter.BOTH;
			if (jsonObj.has("linktype")) {
				String linktype = jsonObj.getString("linktype");
				
				if (linktype.equalsIgnoreCase ("source"))
					direction = FL_DirectionFilter.SOURCE;
				else if (linktype.equalsIgnoreCase ("destination"))
					direction = FL_DirectionFilter.DESTINATION;
			}
			
			String contextId = jsonObj.getString("contextid").trim();
			String targetContextId = jsonObj.getString("targetcontextid").trim();
			
			if (jsonObj.has("fetchTargets")) {
				fetchTargetEntities = Boolean.parseBoolean(jsonObj.getString("fetchTargets"));
			}
			
			String entityId = jsonObj.getString("entity");
			List<String> flowEntityIds = new ArrayList<String>();//UISerializationHelper.buildListFromJson(jsonObj, "entities");
			
			FL_Cluster flcluster = ClusterContextCache.instance.getFile(entityId, contextId);
			if(flcluster != null) {					// if it's a file
				flowEntityIds.addAll(flcluster.getSubclusters());
				flowEntityIds.addAll(flcluster.getMembers());
			} else {
				flowEntityIds.add(entityId);
			}
			
			FL_LinkTag tag = FL_LinkTag.OTHER;
			if (jsonObj.has("type")){
				String type = jsonObj.getString("type");
				if (type.equalsIgnoreCase ("communication")) {
					tag = FL_LinkTag.COMMUNICATION;
				} else if (type.equalsIgnoreCase ("financial")) {
					tag = FL_LinkTag.FINANCIAL;
				} else if (type.equalsIgnoreCase ("social")) {
					tag = FL_LinkTag.SOCIAL;
				}
			}

			DateTime startDate = (jsonObj.has("startdate")) ? DateTimeParser.parse(jsonObj.getString("startdate")) : null;
			DateTime endDate = (jsonObj.has("enddate")) ? DateTimeParser.parse(jsonObj.getString("enddate")) : null;
			FL_DateRange dateRange = DateRangeBuilder.getDateRange(startDate, endDate);
			
			long ams = System.currentTimeMillis();
			
			
			List<String> focusIds;
			if (jsonObj.has("targets")) {
				focusIds = UISerializationHelper.buildListFromJson(jsonObj, "targets");
			} else {
				focusIds = new ArrayList<String>(0);
			}

			Map<String, List<FL_Link>> relatedLinks = clusterAccess.getFlowAggregation(flowEntityIds, focusIds, direction, tag, dateRange, contextId, targetContextId, sessionId);	
			
			long bms = System.currentTimeMillis();
			
			//Map<String, List<FL_Link>> relatedLinks = EntityAggregatedLinks.getRelatedAggregatedLinks(entities, direction, tag, dateRange, dataAccess, focusIds, clusterer, clusterAggregation, targets);
			
			//Get the targets
			List<String> targetIds = new ArrayList<String>();
			for (String key : relatedLinks.keySet()) {
				for (FL_Link link : relatedLinks.get(key)) {
					if (flowEntityIds.contains(link.getSource())) {
						if (!targetIds.contains(link.getTarget())) {
							targetIds.add(link.getTarget());
						}
					} else {
						if (!targetIds.contains(link.getSource())) {
							targetIds.add(link.getSource());
						}
					}
				}
			}
			
			//Id->Id redirect map
			Map<String, String> redirect = new HashMap<String, String>();
			Map<String, Integer> prunedClusterCounts = new HashMap<String, Integer>();
			
			//This check is a hack until the id delimiter issue is fixed.
			if (clusterAccess.getClass().getName().contains("Dynamic")) {
				targets.addAll(entityAccess.getEntities(targetIds));
			}
			
			long cms = System.currentTimeMillis();
			
			List<FL_Cluster> currentFiles = ClusterContextCache.instance.getFiles(targetContextId);
			List<FL_Cluster> context = clusterAccess.getContext(targetContextId, sessionId, false);
			
			// context map to ease the pain of cluster adjustments
			Map<String, FL_Cluster> contextMap = new HashMap<String, FL_Cluster>();
			for(FL_Cluster contextCluster : context) {
				contextMap.put(contextCluster.getUid(), contextCluster);
			}

			// prune context for everything that already exists within files
			Set<String> contextClusterMembers, currentFileMembers;
			for(FL_Cluster contextCluster : context) {
				if(contextCluster.getMembers().size() > 0) {
					contextClusterMembers = new HashSet<String>(contextCluster.getMembers());
					for(FL_Cluster currentFile : currentFiles) {
						if(currentFile.getMembers().size() > 0) {
							currentFileMembers = new HashSet<String>(currentFile.getMembers());
							currentFileMembers.retainAll(contextClusterMembers);
							if(currentFileMembers.size() > 0) {
								contextCluster.getMembers().removeAll(currentFile.getMembers());		// prune members and cache counts for entity

								if(prunedClusterCounts.containsKey(contextCluster.getUid())) {
									prunedClusterCounts.put(contextCluster.getUid(), prunedClusterCounts.get(contextCluster.getUid()) + currentFileMembers.size());
								}
								else {
									prunedClusterCounts.put(contextCluster.getUid(), currentFileMembers.size());
								}
							}
						}
					}
				}
			}
			
			long dms = System.currentTimeMillis();
			
			Pair<Collection<String>, Collection<FL_Cluster>> simpleContext = ClusterCollapser.collapse(context, true, !prunedClusterCounts.isEmpty(), redirect);

			ClusterContextCache.instance.mergeIntoContext(simpleContext.second, targetContextId,false, true);
			
			long ems = System.currentTimeMillis(); 
			
			List<String> entids = new ArrayList<String>();
			entids.addAll(simpleContext.first);
			targets.addAll(entityAccess.getEntities(entids));
			
			List<String> fetchFullClusters = new ArrayList<String>();
			for (FL_Cluster cluster : simpleContext.second) {
				if (cluster.getParent() == null) {
					fetchFullClusters.add(cluster.getUid());
					//rArr.put(UISerializationHelper.toUIJson(cluster));		//Used if getContext computes summaries
				}
			}
			if (!fetchFullClusters.isEmpty()) {
				List<FL_Cluster> topClusters = clusterAccess.getEntities(fetchFullClusters, targetContextId, sessionId);
				
				for(FL_Cluster topCluster : topClusters) {				// update context map with the summary-complete clusters
					contextMap.put(topCluster.getUid(), topCluster);
				}
				
				for(String prunedClusterKey : prunedClusterCounts.keySet()) {		// update context sensitive counts with any pruning changes
					FL_Cluster rootCluster = contextMap.get(prunedClusterKey);
					FL_Cluster currCluster = rootCluster;
					while(currCluster.getParent() != null) {						// retrieve the top-most cluster; this should correspond to a topCluster
						if(ClusterContextCache.instance.isIdInContext(currCluster.getParent(), targetContextId)) {
							rootCluster = contextMap.get(currCluster.getParent());
						}
						
						currCluster = contextMap.get(currCluster.getParent());
					}

					for(int i = 0; i < rootCluster.getProperties().size(); i++) {
						if(rootCluster.getProperties().get(i).getKey().equals("count")) {
							FL_Property oldProp = rootCluster.getProperties().get(i);
							rootCluster.getProperties().set(i, new PropertyHelper(oldProp.getKey(), oldProp.getFriendlyText(), ((Integer) ((FL_SingletonRange) oldProp.getRange()).getValue()) - prunedClusterCounts.get(prunedClusterKey), FL_PropertyType.LONG, oldProp.getProvenance(), oldProp.getUncertainty(), oldProp.getTags()));
							break;
						}
					}
				}
				
				ClusterContextCache.instance.mergeIntoContext(topClusters, targetContextId, true, false);			//Re-insert the clusters with the computed summaries
				for (String cid : fetchFullClusters) {
					targets.add(ClusterContextCache.instance.getCluster(cid, targetContextId));
				}
			}
			
			
			long fms = System.currentTimeMillis();
			if ((fms-ams)>5000) {
				s_logger.error("Slow Branch : took "+(fms-ams)+" ms");
				s_logger.error("Get Flow : "+(bms-ams)+" ms");
				s_logger.error("Get Target Entities : "+(cms-bms)+" ms");
				s_logger.error("Get Context : "+(dms-cms)+" ms");
				s_logger.error("Collapse Context : "+(ems-dms)+" ms");
				s_logger.error("Cluster Re-fetch : "+(fms-ems)+" ms");
			}
			
			
			/*for (FL_Cluster cluster : simpleContext.second) {
				if (cluster.getParent() == null) {
					targets.add(cluster);
				}
			}*/
				
			// Get the query id. This is used by the client to ensure
			// it only processes the latest response.
			queryId = jsonObj.getString("queryId").trim();
			
			if (!targets.isEmpty() && fetchTargetEntities) {
				//This will be a mix of EntityClusters and FL_Entities
				JSONArray ja = new JSONArray();
				for (Object o : targets) {
					if (o instanceof FL_Entity) {
						ja.put(UISerializationHelper.toUIJson((FL_Entity)o));
					} else if (o instanceof FL_Cluster) {
						ja.put(UISerializationHelper.toUIJson((FL_Cluster)o));
					}
				}
				
				result.put("targets", ja);
			}
			if (!relatedLinks.isEmpty()) {
				JSONObject dmap = new JSONObject();
				for (String key : relatedLinks.keySet()) {
					JSONArray larr = new JSONArray();
					for (FL_Link link : relatedLinks.get(key)) {
						
						if (redirect.containsKey(link.getSource())) {
							link.setSource(redirect.get(link.getSource()));
						}
						if (redirect.containsKey(link.getTarget())) {
							link.setTarget(redirect.get(link.getTarget()));
						}
						
						larr.put(UISerializationHelper.toUIJson(link));
					}
					dmap.put(key, larr);
				}
				result.put("data",dmap);
			}
			
			result.put("queryId", queryId);
			result.put("sessionId", sessionId);

			return new StringRepresentation(result.toString(), MediaType.APPLICATION_JSON);

		} catch (AvroRemoteException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		} catch (JSONException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		}
		
	}

}
