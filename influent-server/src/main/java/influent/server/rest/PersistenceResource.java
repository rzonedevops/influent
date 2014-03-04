/**
 * Copyright (c) 2013-2014 Oculus Info Inc.
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

import influent.idl.FL_Cluster;
import influent.idl.FL_ClusteringDataAccess;
import influent.idl.FL_DataAccess;
import influent.idl.FL_LevelOfDetail;
import influent.idl.FL_Persistence;
import influent.idl.FL_PersistenceState;
import influent.idlhelper.FileHelper;
import influent.server.clustering.utils.ClusterContextCache;
import influent.server.clustering.utils.ClusterContextCache.PermitSet;
import influent.server.clustering.utils.ContextReadWrite;

import java.util.ArrayList;
import java.util.Collections;

import oculus.aperture.common.rest.ApertureServerResource;

import org.apache.avro.AvroRemoteException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CacheDirective;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import com.google.inject.Inject;



public class PersistenceResource extends ApertureServerResource{

	private final FL_Persistence persistence;
	private final FL_ClusteringDataAccess clusterAccess;
	private final FL_DataAccess entityAccess;
	private final ClusterContextCache contextCache;

	
	@Inject
	public PersistenceResource(FL_Persistence persistence, FL_ClusteringDataAccess clusterAccess, FL_DataAccess entityAccess, ClusterContextCache contextCache) {
		this.persistence = persistence;
		this.clusterAccess = clusterAccess;
		this.entityAccess = entityAccess;
		this.contextCache = contextCache;
	}
	
	
	
	
	@Post("json")
	public StringRepresentation saveState(String jsonData) throws ResourceException {
		try {
			JSONObject jsonObj = new JSONObject(jsonData);
			
			// Get the query id. This is used by the client to ensure
			// it only processes the latest response.
			String queryId = jsonObj.getString("queryId").trim();
			
			// Get the session id.
			String sessionId = jsonObj.getString("sessionId").trim();
						
			// Get the persistence data
			String data = jsonObj.getString("data").trim();

			JSONObject result = new JSONObject();
			result.put("queryId", queryId);
			
			if (data == null ||
				data.length() < 1 || 
				data.equalsIgnoreCase("null")
			) {
				result.put("persistenceState", FL_PersistenceState.NONE.toString());
			} else {
				FL_PersistenceState state = persistence.persistData(sessionId, data);			
				result.put("persistenceState", state.toString());
			}

			return new StringRepresentation(result.toString(), MediaType.APPLICATION_JSON);		
		
		} catch (JSONException e) {
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
	
	
	
	
	@Get
	public StringRepresentation getState() throws ResourceException {
		try {
			
			Form form = getRequest().getResourceRef().getQueryAsForm(true);
			
			String sessionId = form.getFirstValue("sessionId");
			
			if (sessionId == null || sessionId.length() == 0) {
				throw new ResourceException(
						Status.CLIENT_ERROR_BAD_REQUEST,
						"No sessionId found in persistence request"
					);
			}
			
			sessionId = sessionId.trim();

			String data = persistence.getData(sessionId);

			if(data != null && data.length() != 0) {
				initializeClusterContextCache(sessionId, data);
			}
			
			JSONObject result = new JSONObject();
			result.put("sessionId", sessionId);
			result.put("data", data);
			
			getResponse().setCacheDirectives(
				Collections.singletonList(
					CacheDirective.noCache()
				)
			);

			return new StringRepresentation(result.toString(), MediaType.APPLICATION_JSON);
			
		} catch (JSONException e) {
			throw new ResourceException(
				Status.SERVER_ERROR_INTERNAL,
				"Unable to create JSON object from persistence data",
				e
			);
		} catch (AvroRemoteException e) {
			throw new ResourceException(
				Status.SERVER_ERROR_INTERNAL,
				"Exception during AVRO persistence state deserialization",
				e
			);
		}
	}
	
	private void initializeClusterContextCache(String sessionId, String data) throws JSONException, AvroRemoteException {
		final JSONObject initState = new JSONObject(data);
		
		ArrayList<FL_Cluster> files = new ArrayList<FL_Cluster>();
		ArrayList<String> clusterIds = new ArrayList<String>();
		ArrayList<String> entityIds = new ArrayList<String>();
		
		final JSONArray columns = initState.getJSONArray("children");
		
		for(int c=0; c< columns.length(); c++) {
			final JSONObject columnSpec = columns.getJSONObject(c);
			files.clear();
			clusterIds.clear();
			entityIds.clear();
			final JSONArray columnChildren = columnSpec.getJSONArray("children");
			final String contextId = columnSpec.getString("xfId");
			
			for (int k=0; k < columnChildren.length(); k++) {
				final JSONObject objectSpec = columnChildren.getJSONObject(k);
				final String objectType = objectSpec.getString("UIType");
				
				if(objectType.equals("xfFile")) {
					ArrayList<String> clusterChildren = new ArrayList<String>();

					JSONObject clusterUIObject = null;
					
					try {
						clusterUIObject = objectSpec.getJSONObject("clusterUIObject");
					} catch (Exception e) {
					}
					
					if (clusterUIObject != null) {
						final JSONArray clusterObjectChildren = clusterUIObject.getJSONArray("children");
						
						for (int ck=0; ck < clusterObjectChildren.length(); ck++) {
							clusterChildren.add(clusterObjectChildren.getJSONObject(ck).getString("xfId"));
						}
					}
					
					final FL_Cluster fileCluster = new FileHelper(objectSpec.getString("xfId"), clusterChildren);
					files.add(fileCluster);
				}
				else if(
				    objectType.equals("xfImmutableCluster") ||
				    objectType.equals("xfMutableCluster") ||
				    objectType.equals("xfSummaryCluster")
				) {
					clusterIds.add(objectSpec.getJSONObject("spec").getString("dataId"));
				}
				else if(objectType.equals("xfCard")) {
					entityIds.add(objectSpec.getJSONObject("spec").getString("dataId"));
					
				}
			}
			
			if(files.size() > 0 || clusterIds.size() > 0 || entityIds.size() > 0) {
				PermitSet permits = new PermitSet();
				
				try {
					final ContextReadWrite contextRW = contextCache.getReadWrite(contextId, permits);
					
					contextRW.merge(files, 
						clusterAccess.getClusters(clusterIds, contextId, sessionId), 
						entityAccess.getEntities(entityIds, FL_LevelOfDetail.SUMMARY), 
						false, true);
					
				} finally {
					permits.revoke();
				}
			}
		}
	}
}
