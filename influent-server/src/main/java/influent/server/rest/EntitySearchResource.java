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
import influent.idl.FL_Clustering;
import influent.idl.FL_ClusteringDataAccess;
import influent.idl.FL_DataAccess;
import influent.idl.FL_Entity;
import influent.idl.FL_EntitySearch;
import influent.idl.FL_EntityTag;
import influent.idl.FL_SearchResult;
import influent.idl.FL_SearchResults;
import influent.midtier.Pair;
import influent.server.data.EntitySearchTerms;
import influent.server.utilities.UISerializationHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oculus.aperture.common.rest.ApertureServerResource;

import org.apache.avro.AvroRemoteException;
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



public class EntitySearchResource extends ApertureServerResource{

	private final FL_DataAccess entityAccess;
	private final FL_EntitySearch entitySearcher;
	private final FL_Clustering clusterer;
	private final FL_ClusteringDataAccess clusterAccess;
	
	protected final static int DEFAULT_MAX_LIMIT = 50;
	
	private static final Logger s_logger = LoggerFactory.getLogger(EntitySearchResource.class);
	
	
	@Inject
	public EntitySearchResource(FL_DataAccess entityAccess, FL_EntitySearch entitySearcher, FL_Clustering cluster, FL_ClusteringDataAccess clusterAccess) {
		this.entityAccess = entityAccess;
		this.entitySearcher = entitySearcher;
		this.clusterer = cluster;
		this.clusterAccess = clusterAccess;
	}
	
	
	
	
	@Post("json")
	public StringRepresentation search(String jsonData) throws ResourceException {
		JSONObject jsonObj;
		Map<String, Double> scores = new HashMap<String, Double>();
		List<FL_Entity> entities = new ArrayList<FL_Entity>();
		try {
			jsonObj = new JSONObject(jsonData);
			
			String sessionId = jsonObj.getString("sessionId").trim();

			// Flag to indicate whether or not to cluster the results.
			boolean doCluster = true;
			
			if (jsonObj.has("cluster")) {
				doCluster = jsonObj.getBoolean("cluster");
			}
			
			String contextId = "UNKNOWN-CONTEXT";
			if (jsonObj.has("contextid")) {
				contextId = jsonObj.getString("contextid").trim();
			}
			
			// Get the query id. This is used by the client to ensure
			// it only processes the latest response.
			String queryId = jsonObj.getString("queryId").trim();

			// Determine the number of results to return.
			int resultLimit = DEFAULT_MAX_LIMIT;
			if (jsonObj.has("limit")){
				resultLimit = jsonObj.getInt("limit") > 0?jsonObj.getInt("limit"):resultLimit;
			}

			// Determine the start index.
			int startIndex = 0;
			if (jsonObj.has("start")){
				startIndex = jsonObj.getInt("start");
			}
			
			// get the search term
			String term = jsonObj.getString("term").trim();
			
			// get the advanced search type
			// TODO: UI doesn't send this yet
			String type = jsonObj.has("type") ? jsonObj.getString("type").trim() : null;
			
			EntitySearchTerms terms = new EntitySearchTerms(term);
			
			if (terms.doCluster() != null) {
				doCluster = terms.doCluster();
			}
			
			long zms = System.currentTimeMillis();
			
			FL_SearchResults sResponse = entitySearcher.search(terms.getExtraTerms(), terms.getTerms(), (long)startIndex, (long)resultLimit, type);
			
			// Process the search results.
			//for (FL_SearchResult sResult : sResponse.getResults()){
			//	FL_Entity entity = (FL_Entity)sResult.getResult();
			//	scores.put(entity.getUid().toString(), sResult.getScore());
			//	entities.add(entity);
			//}
			
			

			List<String> notAccounts = new ArrayList<String>();
			
			for (FL_SearchResult sResult : sResponse.getResults()){
				FL_Entity entity = (FL_Entity)sResult.getResult();

				// not an account? then lookup accounts for it.
				if (!entity.getTags().contains(FL_EntityTag.ACCOUNT)) {
					notAccounts.add(entity.getUid());
				}
			}
			
			
			Map<String, List<FL_Entity>> accountsForNotAccountEntities = entityAccess.getAccounts(notAccounts);
			
			// Ok, now process the entities.  If the entity key is in the accounts map, use the accounts, otherwise use the entity
			for (FL_SearchResult sResult : sResponse.getResults()) {
				FL_Entity entity = (FL_Entity)sResult.getResult();

				// not an account? then lookup accounts for it.
				if (!entity.getTags().contains(FL_EntityTag.ACCOUNT)) {
					List<FL_Entity> accounts = accountsForNotAccountEntities.get(entity.getUid());
					if (accounts != null) {
						entities.addAll(accounts);
					}
				} else {
					entities.add(entity);
				}
			}

			long yms = System.currentTimeMillis();
			

			normalizeScores(scores);
			
			// Perform clustering is required.
			if (doCluster && entities.size()>4){
				
				//Clear the cached context - refs#6300
				
				long ams = System.currentTimeMillis();
				
				//ClusterContextCache.instance.clearContext(contextId);
				
				clusterAccess.clearContext(contextId, sessionId);
				
				long bms = System.currentTimeMillis();
				
				/*List<String> clusterIds =*/ clusterer.clusterEntities(entities, contextId, sessionId);
				
				long cms = System.currentTimeMillis();
				
				//Get the context
				List<FL_Cluster> context = clusterAccess.getContext(contextId, sessionId, false);
				
				long dms = System.currentTimeMillis();
				
				Pair<Collection<String>,Collection<FL_Cluster>> simpleContext = ClusterCollapser.collapse(context,false,false,null);
				
				long ems = System.currentTimeMillis();
				
				
				ClusterContextCache.instance.mergeIntoContext(simpleContext.second, contextId, false, true);
				
				long fms = System.currentTimeMillis();
				
				
				
				JSONObject result = new JSONObject();
				JSONArray rArr = new JSONArray();
				List<String> fetchFullClusters = new ArrayList<String>();
				for (FL_Cluster cluster : simpleContext.second) {
					if (cluster.getParent() == null) {
						fetchFullClusters.add(cluster.getUid());
						//rArr.put(UISerializationHelper.toUIJson(cluster));		//Used if getContext computes summaries
					}
				}
				if (!fetchFullClusters.isEmpty()) {
					List<FL_Cluster> topClusters = clusterAccess.getEntities(fetchFullClusters, contextId, sessionId);
					ClusterContextCache.instance.mergeIntoContext(topClusters, contextId, true, false);			//Re-insert the clusters with the computed summaries
					for (String cid : fetchFullClusters) {
						rArr.put(UISerializationHelper.toUIJson(ClusterContextCache.instance.getCluster(cid, contextId)));
					}
				}
				
				for (FL_Entity entity : entities) {
					if (simpleContext.first.contains(entity.getUid())) {
						rArr.put(UISerializationHelper.toUIJson(entity));
					}
				}
				
				result.put("data",rArr);
				result.put("totalResults",sResponse.getTotal());
				result.put("queryId",queryId);
				result.put("sessionId", sessionId);
				
				StringRepresentation responseSR = new StringRepresentation(result.toString(),MediaType.APPLICATION_JSON);
				
				
				long xms = System.currentTimeMillis();
				
				if ((xms-zms)/1000 > 10) {
					s_logger.error("Slow search, ("+((xms-zms)/1000)+" seconds) logging breakdown");
					s_logger.error("Search and getAccounts "+((yms-zms))+" ms");
					s_logger.error("clear context " + ((bms-ams)) + " ms" );
					s_logger.error("cluster entities " + ((cms-bms)) + " ms" );
					s_logger.error("get context " + ((dms-cms)) + " ms" );
					s_logger.error("collapse context " + ((ems-dms)) + " ms" );
					s_logger.error("merge collapsed context " + ((fms-ems)) + " ms" );
				} else {
					s_logger.info("totals "+((xms-zms)/1000)+" seconds");
				}
				
				return responseSR;
			}
			else {
				JSONObject result = new JSONObject();
				
				JSONArray ja = new JSONArray();
				for (FL_Entity ent : entities) {
					JSONObject jo;
					try {
						jo = UISerializationHelper.toUIJson(ent);
					} catch (NullPointerException npe) {
						s_logger.error("Error serializing entity with uid: " + ent.getUid() + ".   Omitting from results.");
						continue;
					}
					ja.put(jo);
				}
				
				result.put("data", ja);
				result.put("scores", scores);
				result.put("totalResults", sResponse.getTotal());
				result.put("queryId", queryId);
				result.put("sessionId", sessionId);

				return new StringRepresentation(result.toString(),MediaType.APPLICATION_JSON);

			}			
		}
		catch (JSONException e) {
			throw new ResourceException(
				Status.CLIENT_ERROR_BAD_REQUEST,
				"Unable to create JSON object from supplied options string",
				e
			);
		} catch (AvroRemoteException e) {
			throw new ResourceException(
					Status.CLIENT_ERROR_BAD_REQUEST,
					"Exception during AVRO processing",
					e
				);
		}
	}
	
	
	
	
	public static void normalizeScores (Map<String, Double> scores) {
		double maxscore = 0;
		
		for (String entity : scores.keySet()) 
			if (scores.get(entity) > maxscore)
				maxscore = scores.get(entity);
		
		if (maxscore != 0)
			for (String entity : scores.keySet()) 
				scores.put(entity, scores.get(entity)/maxscore);
	}
}
