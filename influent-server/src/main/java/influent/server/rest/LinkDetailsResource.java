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

import com.google.inject.name.Named;
import influent.idl.*;
import influent.server.configuration.ApplicationConfiguration;
import influent.server.data.PropertyMatchBuilder;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.utilities.UISerializationHelper;

import java.util.List;

import oculus.aperture.common.JSONProperties;
import oculus.aperture.common.rest.ApertureServerResource;

import oculus.aperture.spi.common.Properties;
import org.apache.avro.AvroRemoteException;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LinkDetailsResource extends ApertureServerResource {

    private static final Logger s_logger = LoggerFactory.getLogger(LinkDetailsResource.class);

    private final FL_LinkSearch _transactionsSearcher;
    private final DataNamespaceHandler _namespaceHandler;
	private static FL_PropertyDescriptors _searchDescriptors = null;
	private static ApplicationConfiguration _applicationConfiguration;

	@Inject
	public LinkDetailsResource(FL_LinkSearch transactionsSearcher,
	                           DataNamespaceHandler namespaceHandler,
	                           @Named("aperture.server.config") Properties config) {
		_transactionsSearcher = transactionsSearcher;
		_namespaceHandler = namespaceHandler;
		_applicationConfiguration = ApplicationConfiguration.getInstance(config);
	}

    @Post("json")
    public JsonRepresentation getTransactionDetails(String jsonData) {

    	String transactionId = "";
		
		try {	
			JSONProperties request = new JSONProperties(jsonData);

			// Get the descriptors if we don't have them yet
			if (_searchDescriptors == null) {
				_searchDescriptors = _transactionsSearcher.getDescriptors();
			}

			transactionId = request.getString("transactionId", "").trim();

			StringBuilder query = new StringBuilder();
			query.append(FL_RequiredPropertyKey.ID.name() + ":\"");
			query.append(_namespaceHandler.localFromGlobalEntityId(transactionId) + "\" ");
			query.append(FL_ReservedPropertyKey.TYPE.name() + ":\"");
			query.append(_namespaceHandler.entityTypeFromGlobalEntityId(transactionId) + "\"");

			final PropertyMatchBuilder terms = new PropertyMatchBuilder(query.toString(), _searchDescriptors, false, _applicationConfiguration.hasMultipleEntityTypes());

			FL_SearchResults sResponse = _transactionsSearcher.search(
				terms.getDescriptorMap(),
				_searchDescriptors.getOrderBy(),
				0,
				1,
				FL_LevelOfDetail.FULL
			);
			
			List<FL_SearchResult> searchResults = sResponse.getResults();
	    	if (searchResults.size() > 0) {
	    		FL_Link link = (FL_Link) searchResults.get(0).getResult();

	    		JSONObject jo = UISerializationHelper.toUIJson(link);

	    		return new JsonRepresentation(jo);
	    	} else {
	    		return null;
	    	}
        }  
		catch (AvroRemoteException e) {
			s_logger.info(String.format("AvroRemoteException occurred Getting transaction details for transaction id: %s; %s", transactionId, e.getMessage()));
            throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Unable to create JSON object from supplied options string",
                    e
            );
        } catch (JSONException je) {
        	s_logger.info(String.format("JSONException occurred Getting transaction details for transaction id: %s; %s", transactionId, je.getMessage()));
			throw new ResourceException(
				Status.CLIENT_ERROR_BAD_REQUEST,
				"JSON parse error.",
				je
			);
		}
    }
}
