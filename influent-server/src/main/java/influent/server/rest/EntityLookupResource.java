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
import influent.idl.FL_DataAccess;
import influent.idl.FL_Entity;
import influent.server.utilities.UISerializationHelper;

import java.util.ArrayList;
import java.util.List;

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

import com.google.inject.Inject;


public class EntityLookupResource extends ApertureServerResource{
	
	private final FL_DataAccess service;
	private final FL_ClusteringDataAccess clusterAccess;
	
	private List<String> flattenEntityCluster(List<String> entityIds, String contextid, String sessionId, boolean isFlattened) throws AvroRemoteException{
		List<String> flatIdList = new ArrayList<String>();
		//First, check to see how many of the ids are clusters without summaries
		List<String> nosumClusters = ClusterContextCache.instance.getClusterIdsWithoutSummaries(entityIds, contextid);
		
		if (!nosumClusters.isEmpty()) {
			List<FL_Cluster> computeClusterSums = clusterAccess.getEntities(entityIds, contextid, sessionId);
			ClusterContextCache.instance.mergeIntoContext(computeClusterSums, contextid, true, false);
		}
		
		List<FL_Cluster> clusterResults = ClusterContextCache.instance.getClusters(entityIds, contextid);
		List<FL_Entity> entityResults = service.getEntities(entityIds);
		if (isFlattened){
			for (FL_Cluster cluster : clusterResults){
				List<String> childIds = cluster.getMembers();
				childIds.addAll(cluster.getSubclusters());
				flatIdList.addAll(flattenEntityCluster(childIds, contextid, sessionId, isFlattened));
			}
			for (FL_Entity entity : entityResults){
				flatIdList.add(entity.getUid());
			}			
		}

		return flatIdList;
	}
	
	@Inject
	public EntityLookupResource(FL_DataAccess service, FL_ClusteringDataAccess clusterDataAccess) {
		this.service = service;
		clusterAccess = clusterDataAccess;
	}
	
	@Post("json")
	public StringRepresentation lookup(String jsonData) throws ResourceException {
		JSONObject jsonObj;
		JSONObject result = new JSONObject();
		try {
			jsonObj = new JSONObject(jsonData);
			
			String sessionId = jsonObj.getString("sessionId").trim();
			
			// Get the query id. This is used by the client to ensure
			// it only processes the latest response.
			String queryId = jsonObj.getString("queryId").trim();

			String contextid = jsonObj.getString("contextid").trim();
			
			Boolean isFlattened = jsonObj.has("isFlattened")?jsonObj.getBoolean("isFlattened"):false;
			
			// get the root node ID from the form
			JSONArray entityNodes = jsonObj.getJSONArray("entities");
			List<String> entityIds = new ArrayList<String>(entityNodes.length());
			
			//Tmp : old-esque clustering lookup
			
			for (int i=0; i < entityNodes.length(); i++){
				String id = entityNodes.getString(i).trim();
				entityIds.add(id);
			}

			
			//First, check to see how many of the ids are clusters without summaries
			List<String> nosumClusters = ClusterContextCache.instance.getClusterIdsWithoutSummaries(entityIds, contextid);
			
			if (!nosumClusters.isEmpty()) {
				List<FL_Cluster> computeClusterSums = clusterAccess.getEntities(entityIds, contextid, sessionId);
				ClusterContextCache.instance.mergeIntoContext(computeClusterSums, contextid, true, false);
			}
			
			List<FL_Cluster> clusterResults = new ArrayList<FL_Cluster>();
			if (isFlattened){
				entityIds.addAll(flattenEntityCluster(entityIds, contextid, sessionId, isFlattened));
			}
			else {
				clusterResults.addAll(ClusterContextCache.instance.getClusters(entityIds, contextid));
			}
			
			//List<FL_Cluster> clusterResults = clusterAccess.getEntities(entityIds, contextid);
		
			List<FL_Entity> entityResults = service.getEntities(entityIds);

			JSONArray ja = new JSONArray();
			
			for (FL_Entity e : entityResults) {
				ja.put(UISerializationHelper.toUIJson(e));	
			}
			for (FL_Cluster ec : clusterResults) {
				ja.put(UISerializationHelper.toUIJson(ec));
			}
			
			
			result.put("data", ja);
			result.put("queryId", queryId);
			result.put("sessionId", sessionId);
			
			return new StringRepresentation(result.toString(), MediaType.APPLICATION_JSON);
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
					"Unable to create JSON object from supplied options string",
					e
				);
		}
		
		
	}
	
}
