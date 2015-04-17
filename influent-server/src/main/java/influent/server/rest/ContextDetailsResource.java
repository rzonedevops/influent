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

import influent.idl.FL_Cluster;
import influent.server.clustering.utils.ClusterContextCache;
import influent.server.clustering.utils.ClusterContextCache.PermitSet;
import influent.server.clustering.utils.ContextRead;
import influent.server.utilities.UISerializationHelper;

import java.util.List;

import oculus.aperture.common.rest.ApertureServerResource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import com.google.inject.Inject;

public class ContextDetailsResource extends ApertureServerResource {

	//final Logger logger = LoggerFactory.getLogger(getClass());

	private final ClusterContextCache contextCache;

	@Inject
	public ContextDetailsResource(
            ClusterContextCache contextCache
    ) {
		this.contextCache = contextCache;
	}
	
	@Get
	public StringRepresentation getContextDetails() {
        Form form = getRequest().getResourceRef().getQueryAsForm();

        String contextId = form.getFirstValue("contextId").trim();

        PermitSet permits = new PermitSet();

        try {
            ContextRead contextRO = contextCache.getReadOnly(contextId, permits);

            if (contextRO == null) {
                throw new Exception("Could not get context for " + contextId);
            }

            List<String> childContexts = contextRO.getChildContexts();
            List<FL_Cluster> clusters = contextRO.getClusters();
            JSONArray clusterJSON = new JSONArray();
            for (FL_Cluster c : clusters) {
                clusterJSON.put(UISerializationHelper.toUIJson(c));
            }
            JSONObject result = new JSONObject();
            result.put("contextId", contextId);
            result.put("children", childContexts);
            result.put("clusters", clusterJSON);
            return new StringRepresentation(result.toString(), MediaType.APPLICATION_JSON);

        } catch (Exception e) {
            throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Unable to create JSON object from supplied options string",
                    e
            );
        } finally {
            permits.revoke();
        }
	}
}
