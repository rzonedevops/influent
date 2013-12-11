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
package influent.cluster;

import influent.entity.clustering.utils.EntityAggregatedLinks;
import influent.entity.clustering.utils.PropertyManager;
import influent.idl.FL_Cluster;
import influent.idl.FL_Clustering;
import influent.idl.FL_ClusteringDataAccess;
import influent.idl.FL_DataAccess;
import influent.idl.FL_DateRange;
import influent.idl.FL_DirectionFilter;
import influent.idl.FL_Entity;
import influent.idl.FL_Geocoding;
import influent.idl.FL_Link;
import influent.idl.FL_LinkTag;
import influent.idl.FL_SortBy;
import influent.midtier.IdGenerator;
import influent.midtier.TypedId;
import influent.midtier.api.Context;
import influent.midtier.api.DataAccessException;
import influent.midtier.api.EntityClusterer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.avro.AvroRemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 * Dynamic clustering impl.
 * @author msavigny
 *
 */
@Singleton
public class DynamicClustering implements FL_Clustering, FL_ClusteringDataAccess {

	private static Logger s_logger = LoggerFactory.getLogger(DynamicClustering.class);
	
	private final FL_DataAccess _entityAccess;
	private final FL_Geocoding _geoCoder;
	private final EntityClusterer _clusterer;

	private final Ehcache cache;
	
	
	
	public DynamicClustering(
		FL_DataAccess entityAccess, 
		FL_Geocoding geocoding, 
		EntityClusterer clusterer, 
		PropertyManager config,
		String ehCacheConfig,
		String cacheName
	) throws ClassNotFoundException, SQLException {

		_entityAccess = entityAccess;
		_clusterer = clusterer;
		_geoCoder = geocoding;
		_clusterer.init(new Object[] {new IdGenerator() {
			@Override
			public String nextId() {
				return TypedId.fromNativeId(TypedId.CLUSTER, UUID.randomUUID().toString()).getTypedId();
			}
		}, _geoCoder, config
		});
		
		CacheManager cacheManager = (ehCacheConfig != null) ? CacheManager.create(ehCacheConfig) : null;
		if (cacheManager == null) {
			s_logger.warn("ehcache property not set, persistence data won't be cached");
		}
		
		this.cache = cacheManager.getEhcache(cacheName);
	}
	
	
	
	
	@Override
	public String createContext() throws AvroRemoteException {
		String randContext = UUID.randomUUID().toString();
		insertIntoContext(new Context(), randContext);
		return randContext;
	}	
	
	
	
	
	@Override
	public List<String> clusterEntitiesById(List<String> entityIds,
			String contextId, String sessionId) throws AvroRemoteException {
		return clusterEntities(_entityAccess.getEntities(entityIds),contextId,sessionId);
	}
	
	
	
	
	@Override
	public List<String> clusterEntities(List<FL_Entity> entities,
			String contextId, String sessionId) throws AvroRemoteException {

		List<String> rootIds = new ArrayList<String>();
		
		// fetch the current context
		Context context = getContext(contextId);
		
		List<FL_Entity> clusterEntities = entities;
		
		// if there is an existing context then filter out all entities that already belong to this context
		if (context != null) {
			clusterEntities = new ArrayList<FL_Entity>(entities.size());
			
			for (FL_Entity entity : entities) {
				if (!context.entities.containsKey(entity.getUid())) {
					clusterEntities.add(entity);
				}
			}
		}
		
		// nothing to do - return
		if (clusterEntities.isEmpty()) return rootIds;
		
		Context updatedContext = _clusterer.clusterEntities(clusterEntities, context);
				
		rootIds.addAll(updatedContext.roots.keySet());
		
		insertIntoContext(updatedContext, contextId);
		
		return rootIds;
	}
	
	
	
	
	@Override
	public List<FL_Cluster> getClusters(List<String> entities, String contextId, String sessionId)
			throws AvroRemoteException {
		Context context = getContext(contextId);
		if (context == null) return Collections.emptyList();
		
		List<FL_Cluster> results = new ArrayList<FL_Cluster>();
		for (String id : entities) {
			FL_Cluster c = context.clusters.get(id);
			if (c!= null) {
				results.add(c);
			}
		}
		
		return results;
	}
	
	
	
	
	@Override
	public long removeMembers(List<String> entities, String contextId, String sessionId)
			throws AvroRemoteException {

		Context context = getContext(contextId);
		if (context == null) return 0;
		
		int count = 0;
		for (String id : entities) {
			FL_Cluster c = context.clusters.get(id);
			if (c!= null) {
				context.clusters.remove(id);
				count++;
				
				// remove all entities from the context that were members of this cluster
				for (String eId : c.getMembers()) {
					context.entities.remove(eId);
				}
				
				// remove all sub-clusters from the context too!
				count += removeMembers(c.getSubclusters(), contextId, sessionId);
			}
		}
		
		return count;
	}
	
	
	
	
	@Override
	public List<FL_Cluster> getContext(String contextId, String sessionId,
			boolean computeSummaries) throws AvroRemoteException {
		Context context = getContext(contextId);
		
		if (context == null) return Collections.emptyList();
		
		List<FL_Cluster> results = new ArrayList<FL_Cluster>();
		for (String id : context.clusters.keySet()) {
			FL_Cluster c = context.clusters.get(id);
			if (c!= null) {
				// return copies of the cluster to avoid tampering with the internal context!
				FL_Cluster copy = FL_Cluster.newBuilder(c).build();
				// the default avro builders use a immutable list for members and subclusters
				// set mutable version explicitly to ease cluster simplification
				copy.setMembers( new ArrayList<String>(c.getMembers()) );
				copy.setSubclusters( new ArrayList<String>(c.getSubclusters()) );
				results.add( copy );
			}
		}
		return results;
	}
	
	
	
	
	@Override
	public boolean clearContext(String contextId, String sessionId) throws AvroRemoteException {
		if (contextContains(contextId)) {
			removeContext(contextId);
			return true;
		}
		return false;
	}
	
	
	
	
	@Override
	public Map<String, List<FL_Link>> getFlowAggregation(List<String> entities,
			List<String> focusEntities, FL_DirectionFilter direction,
			FL_LinkTag tag, FL_DateRange date, String entitiesContextId,
			String focusContextId, String sessionId) throws AvroRemoteException {
		
		try {
			if (focusEntities == null || focusEntities.isEmpty()) {
				Map<String, List<FL_Link>> relatedLinks = EntityAggregatedLinks.getRelatedAggregatedLinks(entities, direction, tag, date, _entityAccess, true, null,this,this,entitiesContextId, focusContextId, sessionId);
				return relatedLinks;
			} else {
				Map<String, List<FL_Link>> relatedLinks = EntityAggregatedLinks.getAggregatedLinks(entities, focusEntities, direction, tag, date, _entityAccess, this, entitiesContextId, focusContextId, sessionId);
				return relatedLinks;
			}
		} catch (DataAccessException e) {
			throw new AvroRemoteException(e);
		}
	}
	
	
	
	
	@Override
	public Map<String, List<FL_Link>> getTimeSeriesAggregation(
			List<String> entities, List<String> focusEntities, FL_LinkTag tag,
			FL_DateRange date, String entitiesContextId, String focusContextId, String sessionId)
			throws AvroRemoteException {

		//Find leaf nodes for focii, and keep track of leaf id ->original cluster id
		
		Map<String,String> fociiIdMap = new HashMap<String, String>();
		List<String> fociiList = new ArrayList<String>();
		for (String focusid : focusEntities) {
			Set<String> flids = getLeafIds(Collections.singletonList(focusid), focusContextId, sessionId);
			fociiList.addAll(flids);
			for (String fli : flids) {
				fociiIdMap.put(fli, focusid);
			}
		}
		
		//Find leaf nodes for entities, and keep track of leaf id ->original cluster id
		Map<String,String> entIdMap = new HashMap<String, String>();
		List<String> leafList = new ArrayList<String>();
		for (String entid : entities) {
			Set<String> elids = getLeafIds(Collections.singletonList(entid), entitiesContextId, sessionId);
			leafList.addAll(elids);
			for (String eli : elids) {
				entIdMap.put(eli, entid);
			}
		}
		
		Map<String, List<FL_Link>> links = _entityAccess.getTimeSeriesAggregation(leafList, fociiList, tag, date);	
		
		//Change the ids on the links, so the map to the original clusters passed in, not the underlying leaf nodes.
		for (List<FL_Link> linklist : links.values()) {
			for (FL_Link link : linklist) {
				if (link.getSource()!=null && fociiIdMap.containsKey(link.getSource())) {
					link.setSource(fociiIdMap.get(link.getSource()));
				}
				if (link.getTarget()!=null && fociiIdMap.containsKey(link.getTarget())) {
					link.setTarget(fociiIdMap.get(link.getTarget()));
				}
			}
		}
		
		return links;
	}
	
	
	
	
	@Override
	public Map<String, List<FL_Link>> getAllTransactions(List<String> entities,
			FL_LinkTag tag, FL_DateRange date, FL_SortBy sort,
			List<String> linkFilter, long max, String contextId, String sessionId)
			throws AvroRemoteException {
		
		return null;
	}
	
	
	
	
	public void insertIntoContext(Context updatedContext, String contextId) {
		Element element = new Element(contextId, updatedContext);
		
		// explicitly remove the item from the cache first if it exists - this works around an EOFException thrown by ehcache
		cache.remove(contextId);
		cache.put(element);
	}
	
	
	
	
	private Context getContext(String contextId) {
		
		Context data = null;
		
		Element element = cache.get(contextId);
		if (element != null) {
			data = (Context)element.getObjectValue();
		}
		
		return data;
	}
	
	
	
	
	private Boolean contextContains(String contextId) {
		return cache.isKeyInCache(contextId);
	}
	
	
	
	
	private void removeContext(String contextId) {
		cache.remove(contextId);
	}
	
	
	
	
	private Set<String> getLeafIds(List<String> ids, String context, String sessionId) throws AvroRemoteException {
		if (ids == null || ids.isEmpty()) return Collections.emptySet();
		
		Set<String> lids = new HashSet<String>();

		lids.addAll(TypedId.filterTypedIds(ids, TypedId.ACCOUNT));
		lids.addAll(getClusterLeafIds(TypedId.filterTypedIds(ids, TypedId.CLUSTER), context, sessionId));
		
		return lids;
	}
	
	
	
	
	private Set<String> getClusterLeafIds(List<String> clusterIds, String context, String sessionId) throws AvroRemoteException {
		
		if (clusterIds == null || clusterIds.isEmpty()) return Collections.emptySet();
		
		Set<String> lids = new HashSet<String>();

		List<FL_Cluster> toSearch = getClusters(clusterIds, context, sessionId);
		
		for (FL_Cluster flc : toSearch) {
			lids.addAll(flc.getMembers());
			if (flc.getSubclusters()!= null && !flc.getSubclusters().isEmpty()) {
				lids.addAll(getClusterLeafIds(flc.getSubclusters(), context, sessionId));
			}
		}
		
		return lids;
	}
}
