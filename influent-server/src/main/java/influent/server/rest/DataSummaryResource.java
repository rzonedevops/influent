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

import influent.idl.FL_DataAccess;
import influent.server.utilities.GuidValidator;
import oculus.aperture.common.JSONProperties;
import oculus.aperture.common.rest.ApertureServerResource;

import org.apache.avro.AvroRemoteException;
import org.json.JSONException;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import com.google.inject.Inject;

/**
 * Ledger Resource is intended to work with a jQuery DataTable on the front end, as such it
 * handles the parameters sent by them, and returns data in the format it expects.
 * 
 *
 */

public class DataSummaryResource extends ApertureServerResource {
	
	private final FL_DataAccess dataAccess;
	
	
	@Inject
	public DataSummaryResource(FL_DataAccess dataAccess) {
		this.dataAccess = dataAccess;
	}
	
	@Post("json")
	public StringRepresentation getDataSummary(String jsonData) throws ResourceException {
		
		try {
			JSONProperties request = new JSONProperties(jsonData);
			
			final String sessionId = request.getString("sessionId", null);
			if (!GuidValidator.validateGuidString(sessionId)) {
				throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, "sessionId is not a valid UUID");
			}
			
			return new StringRepresentation(dataAccess.getDataSummary().toString());
		} catch (AvroRemoteException are) {
			throw new ResourceException(
					Status.CLIENT_ERROR_BAD_REQUEST,
					"Data access error.",
					are
				);
		} catch (JSONException je) {
			throw new ResourceException(
				Status.CLIENT_ERROR_BAD_REQUEST,
				"JSON parse error.",
				je
			);
		}
	}	
}
