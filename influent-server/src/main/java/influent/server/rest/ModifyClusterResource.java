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

import influent.idl.FL_ClusteringDataAccess;
import influent.idl.FL_DataAccess;
import influent.idlwrapper.TmpClusterIDGen;
import influent.midtier.IdGenerator;
import oculus.aperture.common.rest.ApertureServerResource;

import org.apache.avro.AvroRemoteException;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ModifyClusterResource extends ApertureServerResource{

//	private final FL_DataAccess entityAccess;
//	private final FL_ClusteringDataAccess clusterAccess;
	
	@SuppressWarnings("unused")
	private static final IdGenerator idGen;
	static {
		idGen = new TmpClusterIDGen();
	}
	
	@SuppressWarnings("unused")
	private static final Logger s_logger = LoggerFactory.getLogger(ModifyClusterResource.class);

	@Inject
	public ModifyClusterResource(FL_DataAccess entityAccess, FL_ClusteringDataAccess clusterAccess) {
//		this.entityAccess = entityAccess;
//		this.clusterAccess = clusterAccess;
	}


	
	@Get 
	public StringRepresentation modifyCluster () {
		
		Form form = getRequest().getResourceRef().getQueryAsForm();
		String sessionId = form.getFirstValue("sessionId").trim();
		String childId = form.getFirstValue("childId");			//Must be non-null for insert and remove
		String edit = form.getFirstValue("edit");				//Allowed values, 'insert', 'remove', 'create'
		String mainParentId = form.getFirstValue("parentId");	//Must be non-null for insert and remove
		String contextId = form.getFirstValue("contextid");
		try {
			if ("insert".equals(edit)) {
				return new StringRepresentation(insert(mainParentId, childId).toString(), MediaType.APPLICATION_JSON);
			} else if ("create".equals(edit)) {
				return new StringRepresentation(create().toString(), MediaType.APPLICATION_JSON);
			} else if ("remove".equals(edit)) {
				return new StringRepresentation(remove(mainParentId, childId, contextId, sessionId).toString(), MediaType.APPLICATION_JSON);
			}
			
			
			return new StringRepresentation("", MediaType.APPLICATION_JSON);
		} catch (JSONException e) {
			throw new ResourceException(e);
		} catch (AvroRemoteException e) {
			throw new ResourceException(e);
		}
	}
	
	private JSONObject insert(String mainParentId, String childId) throws JSONException{
		return new JSONObject();
	}
	
	private JSONObject create() throws JSONException{
		return new JSONObject();
	}
	
	private JSONObject remove(String mainParentId, String childId, String contextId, String sessionId) throws JSONException, AvroRemoteException{
		JSONObject result = new JSONObject();
		
		/*long removed =*/ //clusterAccess.removeMembers(Collections.singletonList(childId), contextId);

		
		
		return result;
	}
	
	
	
//	private JSONObject modifyCluster(String edit, String childId, String mainParentId) throws JSONException {
//		JSONObject result = new JSONObject();
//		
//		if (edit != null) {
//			edit = edit.trim();
//		} else {
//			s_logger.warn("No edit command specified in modifyCluster (must be one of 'create', 'insert' or 'remove'");
//			return result;
//		}
//		try {
//			//Handle the create case... insert and remove handled below
//			//TODO : Move into separate method.
//			if (edit.equals("create")) {
//				
//				String id = idGen.nextId();			
//				EntityClusterImpl newCluster = new EntityClusterImpl(id,
//						null,
//						new ArrayList<Property>(),
//						ItemTag.FILE,
//						new HashSet<String>(),
//						null,
//						null);
//				
//				MemoryTransientClusterStore.getInstance().getMap().put(id, newCluster);
//				
//				result.put(id, FLWrapHelper.fromEntity(newCluster));
//				return result;
//			}
//			
//			
//			if (mainParentId != null) {
//				mainParentId = mainParentId.trim();
//			} else {
//				return result;
//			}
//			
//			//UI can also pass the string 'null'
//			if (mainParentId.equals("null")) {
//				//No parent to remove from, so return
//				return result;
//			}
//			
//			
//			s_logger.info("Persisting: " + edit + " of " + childId+" from "+mainParentId);
//			// Do we need to return a new result or id from the data store?
//			
//			
//			// First, need to get the entity cluster referred to.
//		
//			String parentId = mainParentId;
//				
//			EntityCluster parent = MemoryTransientClusterStore.getInstance().getMap().get(parentId);
//				
//			if (parent == null) {
//				s_logger.warn("Could not find parent cluster for id "+parentId);
//				return result;
//			}
//				
//			//Edit the member in question from the parent.
//			Set<String> mids = parent.getMemberIds();
//			if (edit.equals("remove")) {
//				mids.remove(childId);
//			} else if (edit.equals("insert")) {
//				//Copy the child object, it it is a cluster
//				
//				List<FL_Entity> child = service.getEntities(Collections.singletonList(childId));
//				if (child == null || child.size()==0) {
//					//Child was not found in the FL_DataAccess call, so it is a cluster.  Or should be anyway.
//					
//					//Copy - needs to be a deep copy unfortunately.
//					EntityCluster childCluster = (EntityCluster)child;
//					
//					EntityCluster deepCopy = CloneClusterUtil.cloneCluster(childCluster,  false);
//					
//					mids.add(deepCopy.getId());
//					
//				} else {
//					//Don't copy the object, just re-use the id.
//					mids.add(childId);
//				}
//			}
//			
//			
//			while (parent != null) {
//				//Update the properties for this subcluster
//				List<Property> newProps = updateSubClusterProperties(parent);
//				
//				// copy the parent with revised properties
//				EntityClusterImpl newParent = new EntityClusterImpl(
//					parent.getId(),
//					parent.getProvenance(),
//					newProps,
//					parent.getTag(),
//					mids,
//					parent.getRootId(),
//					parent.getParentId()
//				);
//				
//				//Store updated parent object in cache
//				MemoryTransientClusterStore.getInstance().getMap().put(newParent.getId(), newParent);
//				
//				//Put the updated cluster into the result map (as the properties are now updates)
//				result.put(newParent.getId(), FLWrapHelper.fromEntity(newParent));
//				
//				//Go up the cluster hierarchy, and update any properties that need it
//				if (newParent.getParentId()!=null && !newParent.getParentId().isEmpty()) {
//					//Not at the root, so keep going up
//					parent = MemoryTransientClusterStore.getInstance().getMap().get(newParent.getParentId());
//					if (parent != null) {
//						mids = parent.getMemberIds();			//Don't want to overwrite anymore.
//					}
//				} else {
//					//Everything up to the root has been processed, break out and return.
//					break;
//				}
//			} 
//			
//			return result;
//		} catch (DataAccessException e) {
//			s_logger.error("Exception while persisting cluster edit "+e.getMessage(), e);
//			throw new ResourceException(e);
//		} catch (AvroRemoteException e) {
//			s_logger.error("Exception while persisting cluster edit "+e.getMessage(), e);
//			throw new ResourceException(e);
//		}
//		
//		
//	}
	

	
//	private List<Property> updateSubClusterProperties(EntityCluster subCluster) throws DataAccessException, AvroRemoteException {
//		//Grab the subclusters of the subcluster.
//		//Creates a new list of properties, which is a combination of the original subcluster, and the updated
//		//cluster properties.  Cluster properties overwrite old values instead of appending.
//		List<Object> subObjects = new ArrayList<Object>();
//		
//		List<String> subids = new ArrayList<String>();
//		for (String id : subCluster.getMemberIds()) {
//			EntityCluster ec = MemoryTransientClusterStore.getInstance().getMap().get(id);
//			if (ec != null) {
//				subObjects.add(ec);
//			} else {
//				subids.add(id);
//			}
//		}
//		
//		subObjects.addAll(service.getEntities(subids));
//		
//		List<Property> updatedProperties = createEntityClusterProperties(subCluster, subObjects);
//
//		return updatedProperties;
//	}
//	
//	
//	///////////////////////////////////////////////////////////////////////////////////
//	// TODO : remove this once the cluster code has been refactored to make this callable
//	
//	@SuppressWarnings({"unchecked"})
//	private List<Property> createEntityClusterProperties(EntityCluster cluster, List<Object> subEntity) {
//		List<Property> properties = new ArrayList<Property>();
//		
//		Map<String, Integer> locationSummary = new HashMap<String, Integer>();
//		Map<String, Integer> typeSummary = new HashMap<String, Integer>();
//		
//		/*Map<String, Centroid> centroids = cluster.getCentroids();
//		
//		// Add the lat/lon
//		Centroid<? extends Feature> location = centroids.get("location");
//		Collection<GeoSpatialFeature> lfeatures = (Collection<GeoSpatialFeature>)location.getCentroid();
//		for (GeoSpatialFeature feature : lfeatures) {
//			Property latProp = new Property(Collections.singletonList(PropertyTag.GEO_LAT), "lat", PropertyType.REAL, feature.getLatitude(), "Latitude");
//			properties.add(latProp);
//			Property lonProp = new Property(Collections.singletonList(PropertyTag.GEO_LON), "lon", PropertyType.REAL, feature.getLongitude(), "Longitude");
//			properties.add(lonProp);
//		}*/
//		
//		// Add the total decendents count
//		int decendentCount = 0;
//		for (Object childc : subEntity) {
//			
//			if (childc instanceof EntityCluster) {
//				EntityCluster child = (EntityCluster)childc;
//				decendentCount += (Integer)child.getFirstProperty("count").getValue();
//				increment( (Map<String, Integer>)child.getFirstProperty("type-dist").getValue(), typeSummary );
//				increment( (Map<String, Integer>)child.getFirstProperty("location-dist").getValue(), locationSummary );
//			}
//			else {
//				FL_Entity child = (FL_Entity)childc;
//				decendentCount++;
//				
//				FL_Property typeProp = EntityHelper.getFirstPropertyByTag(child, FL_PropertyTag.TYPE);		
//				
//				String type = (typeProp == null || typeProp.getValue()==null) ? "no type":typeProp.getValue().toString();
//				
//				increment(type, 1, typeSummary);
//				
//				FL_Property locations = EntityHelper.getFirstPropertyByTag(child,FL_PropertyTag.GEO);
//				if (locations != null) {
//					if (locations.getValue() instanceof FL_GeoData) {
//						String cc = ((FL_GeoData)locations.getValue()).getCc();
//						increment(cc, 1, locationSummary);	
//					} else if (locations.getValue() instanceof String){
//						String cc = (String)locations.getValue();
//						increment(cc, 1, locationSummary);	
//					}
//					
//				}
//			}
//		}
//		
//		Property p = new Property(Collections.singletonList(PropertyTag.STAT), "count", PropertyType.INTEGER, decendentCount);
//		properties.add(p);
//		
//		// don't recompute the label, even if it technically should change.  It will be confusing to the user.
//		Property nameProp = cluster.getFirstProperty("name");
//		String label = "cluster";		//default name
//		if (nameProp != null) {
//			label = (String)nameProp.getValue();
//		}
//		p = new Property(Collections.singletonList(PropertyTag.TEXT), "name", PropertyType.STRING, label);
//		properties.add(p);
//		
//		// set label to name of the cluster and append "(+NUM_DECENDENTS)" 
//		p = new Property(Collections.singletonList(PropertyTag.LABEL), PropertyTag.LABEL.name(), PropertyType.STRING, label + " (+" + (decendentCount-1) + ")");
//		properties.add(p);
//		
//		p = new Property(Collections.singletonList(PropertyTag.TYPE), PropertyTag.TYPE.name(), PropertyType.STRING, "entitycluster");
//		properties.add(p);
//		
//		p = new Property(Collections.singletonList(PropertyTag.STAT), "type-dist", PropertyType.UNKNOWN, typeSummary);
//		properties.add(p);
//		
//		p = new Property(Collections.singletonList(PropertyTag.STAT), "location-dist", PropertyType.UNKNOWN, locationSummary);
//		properties.add(p);
//		
//		return properties;
//	}
//	
//	private void increment(String key, int increment, Map<String, Integer> index) {
//		int count = 0;
//		
//		if (index.containsKey(key)) {
//			count = index.get(key);
//		}
//		count += increment;
//		index.put(key, count);
//	}
//	
//	private void increment(Map<String, Integer> stats, Map<String, Integer> index) {
//		for (String key : stats.keySet()) {
//			int count = stats.get(key);
//			increment(key, count, index);
//		}
//	}
}
