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
package influent.server.rest;

import influent.idl.FL_ClusteringDataAccess;
import influent.idl.FL_DataAccess;
import influent.idl.FL_Persistence;
import influent.idl.FL_PersistenceState;
import influent.server.clustering.utils.ClusterContextCache;
import influent.server.utilities.GuidValidator;
import oculus.aperture.common.JSONProperties;
import oculus.aperture.common.rest.ApertureServerResource;

import org.apache.avro.AvroRemoteException;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import com.google.inject.Inject;



public class SaveStateResource extends ApertureServerResource{

	private final FL_Persistence persistence;
	@SuppressWarnings("unused")
	private final FL_ClusteringDataAccess clusterAccess;
	@SuppressWarnings("unused")
	private final FL_DataAccess entityAccess;
	@SuppressWarnings("unused")
	private final ClusterContextCache contextCache;

	
	
	
	@Inject
	public SaveStateResource(FL_Persistence persistence, FL_ClusteringDataAccess clusterAccess, FL_DataAccess entityAccess, ClusterContextCache contextCache) {
		this.persistence = persistence;
		this.clusterAccess = clusterAccess;
		this.entityAccess = entityAccess;
		this.contextCache = contextCache;
	}
	
	
	
	
	@Post("json")
	public StringRepresentation saveState(String jsonData) throws ResourceException {
		try {
			JSONProperties request = new JSONProperties(jsonData);

			// Get the session id.
			final String sessionId = request.getString("sessionId", null);
			if (!GuidValidator.validateGuidString(sessionId)) {
				throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, "sessionId is not a valid UUID");
			}
			
			// Get the persistence data
			String data = request.getString("data", "").trim();

			JSONObject result = new JSONObject();
			
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
}
