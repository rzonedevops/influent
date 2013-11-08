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
import influent.idl.FL_EntityTag;
import influent.idl.FL_GeoData;
import influent.idl.FL_Property;
import influent.idl.FL_PropertyTag;
import influent.idl.FL_PropertyType;
import influent.idlhelper.ClusterHelper;
import influent.idlhelper.EntityHelper;
import influent.idlhelper.PropertyHelper;
import influent.server.utilities.UISerializationHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import oculus.aperture.common.rest.ApertureServerResource;

import org.apache.avro.AvroRemoteException;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ModifyContextResource extends ApertureServerResource{
	
	private final FL_ClusteringDataAccess clusterAccess;
	private final FL_DataAccess entityAccess;
	
	private static final Logger s_logger = LoggerFactory.getLogger(ModifyContextResource.class);

	
	
	@Inject
	public ModifyContextResource(
		FL_ClusteringDataAccess clusterAccess, 
		FL_DataAccess entityAccess
	) {
		this.clusterAccess = clusterAccess;
		this.entityAccess = entityAccess;
	}
	
	
	
	
	@Post 
	public StringRepresentation modifyContext (String jsonData) throws ResourceException {
		
		try {
			JSONObject jsonObj = new JSONObject(jsonData);

			String erroneousArgument = null;
			String sessionId = jsonObj.getString("sessionId");
			String contextId = jsonObj.getString("contextId");
			String edit = jsonObj.getString("edit"); //Allowed values, 'insert', 'remove', 'create'
			String fileId = jsonObj.getString("fileId"); //Must be non-null
			List<String> childIds = UISerializationHelper.buildListFromJson(jsonObj, "childIds"); //Must be non-null for insert and remove
			
			if (sessionId == null) {
				erroneousArgument = "'sessionId'";
			}
			else if (contextId == null) {
				erroneousArgument = "'contextId'";
			}
			else if (fileId == null) {
				erroneousArgument = "'mainParentId'";
			}
			else {
				if ("insert".equals(edit)) {
					if (childIds == null || childIds.size() == 0) {
						erroneousArgument = "'childIds'";
					} else {
						insert(fileId, childIds, contextId, sessionId);
						return new StringRepresentation("insert complete", MediaType.APPLICATION_JSON);
					}
				} else if ("remove".equals(edit)) {
					if (childIds == null || childIds.size() == 0) {
						erroneousArgument = "'childIds'";
					} else {
						remove(fileId, childIds, contextId, sessionId);
						return new StringRepresentation("remove complete", MediaType.APPLICATION_JSON);
					}
				} else if ("create".equals(edit)) {
					create(fileId, contextId, sessionId);
					return new StringRepresentation("create complete", MediaType.APPLICATION_JSON);
				}
				else {
					erroneousArgument = "edit";
				}
			}
			
			s_logger.error("Argument" + erroneousArgument + " is invalid; unable to modify context");
			return new StringRepresentation("", MediaType.APPLICATION_JSON);
		} catch (JSONException e) {
			throw new ResourceException(e);
		} catch (AvroRemoteException e) {
			throw new ResourceException(e);
		}
	}
	
	
	
	
	private FL_Cluster updateFileCluster(FL_Cluster file) throws AvroRemoteException {
		List<FL_Property> entityProps = new LinkedList<FL_Property>();
		
		List<FL_Entity> entities = entityAccess.getEntities(file.getMembers());
		
		String label = entities.size() > 0 ? (String)EntityHelper.getFirstPropertyByTag(entities.get(0), FL_PropertyTag.LABEL).getValue() : "";
	
		Map<String,Integer> typeDist = new HashMap<String, Integer>();
		Map<String,Integer> ccDist = new HashMap<String, Integer>();
		List<String> tempCCs = new ArrayList<String>();
		
		
		for (FL_Entity entity : entities) {
			PropertyHelper typeProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.TYPE);
			PropertyHelper locationProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.GEO);
			
			String type = (String)typeProp.getValue();
			Integer typeCount = typeDist.get(type);
			if (typeCount == null) {
				typeCount = 1;
			}
			else {
				typeCount++;
			}
			typeDist.put(type, typeCount);
			
			if (locationProp != null) {
				final List<Object> geoVals = locationProp.getValues();

				for (Object obj : geoVals) {
					final FL_GeoData geoData = (FL_GeoData) obj;
					final String cc = geoData.getCc();
					if (cc != null && !cc.isEmpty() && !tempCCs.contains(cc)) {
						tempCCs.add(cc); // 1 increment means we have it at least once.

						Integer ccCount = ccDist.get(cc);
						if (ccCount == null) {
							ccCount = 1;
						}
						else {
							ccCount++;
						}
						ccDist.put(cc, ccCount);
					}
				}
				tempCCs.clear();
			}
		}
		
		// add the file cluster type dist property
		entityProps.add(new PropertyHelper("type-dist", "distribution of entity types", typeDist, FL_PropertyType.OTHER, Collections.singletonList(FL_PropertyTag.STAT)));
		// add the file cluster count property
		entityProps.add(new PropertyHelper("count", "membership count", entities.size(), FL_PropertyType.LONG, Collections.singletonList(FL_PropertyTag.COUNT)));
		// add the file cluster cc dist property 
		if (ccDist.size() > 0) {
			entityProps.add(new PropertyHelper("location-dist", "country code distribution", ccDist, FL_PropertyType.OTHER, Collections.singletonList(FL_PropertyTag.STAT)));
		}
		
		return new ClusterHelper(file.getUid(), label, "cluster", FL_EntityTag.CLUSTER, entityProps, file.getMembers(), file.getSubclusters(), file.getParent(), file.getRoot(), file.getLevel());
	}
	
	
	
	
	private void create(
		String fileId, 
		String contextId,
		String sessionId
	) {
		FL_Cluster newFile = new FL_Cluster();
		newFile.setUid(fileId);
		newFile.setMembers(new ArrayList<String>());
		newFile.setSubclusters(new ArrayList<String>());
		
		ClusterContextCache.instance.mergeIntoContext(
			Collections.<FL_Cluster>singletonList(newFile), 
			Collections.<FL_Cluster>emptyList(), 
			Collections.<FL_Entity>emptyList(), 
			contextId, 
			false, 
			true
		);
	}
	
	
	
	
	private void insert(
		String fileId, 
		List<String> childIds, 
		String contextId,
		String sessionId
	) throws JSONException, AvroRemoteException {
		
		FL_Cluster file = ClusterContextCache.instance.getFile(fileId, contextId);
		
		if (file == null) {
			file = new FL_Cluster();
			file.setUid(fileId);
			file.setMembers(new ArrayList<String>());
			file.setSubclusters(new ArrayList<String>());
		}
		// Only add ids that haven't already been added.
		for (String id : childIds){
			if (!file.getMembers().contains(id)){
				file.getMembers().add(id);
			}
		}
		
		file = updateFileCluster(file);
		
		List<FL_Entity> entities = new ArrayList<FL_Entity>(ClusterContextCache.instance.getEntities(childIds, contextId));
		List<String> newEntities = new ArrayList<String>(childIds);
		for (FL_Entity entity : entities) {
			newEntities.remove(entity.getUid());
		}
		if (newEntities.size() > 0) {
			entities.addAll(entityAccess.getEntities(newEntities));
		}
		
		ClusterContextCache.instance.mergeIntoContext(Collections.<FL_Cluster>singletonList(file), Collections.<FL_Cluster>emptyList(), entities, contextId, false, true);
	}
	
	private void remove(
		String fileId, 
		List<String> childIds, 
		String contextId,
		String sessionId
	) throws JSONException, AvroRemoteException {

		if (fileId.equals(contextId)) {
		
			clusterAccess.removeMembers(childIds, contextId, sessionId);
			ClusterContextCache.instance.removeFromContext(contextId, childIds);
			
		} else {
		
			FL_Cluster file;
			
			List<FL_Cluster> files = ClusterContextCache.instance.getFiles(Collections.<String>singletonList(fileId), contextId);
			
			if (files.size() > 1) {
				throw new AvroRemoteException("cluster context cache contained more than one file with the same id");
			}
			
			if (files.size() > 0) {
				
				file = files.get(0);
				file.getMembers().removeAll(childIds);
				file = updateFileCluster(file);
			
				ClusterContextCache.instance.mergeIntoContext(Collections.<FL_Cluster>singletonList(file), Collections.<FL_Cluster>emptyList(), Collections.<FL_Entity>emptyList(), contextId, false, true);
			}
		}
	}
}
