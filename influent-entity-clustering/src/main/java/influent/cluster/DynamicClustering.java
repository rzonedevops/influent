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
import influent.idlhelper.FLEntityObject;
import influent.midtier.IdGenerator;
import influent.midtier.api.ClusterResults;
import influent.midtier.api.DataAccessException;
import influent.midtier.api.EntityClusterer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.avro.AvroRemoteException;

import com.google.inject.Singleton;

/**
 * Dynamic clustering impl.
 * @author msavigny
 *
 */
@Singleton
public class DynamicClustering implements FL_Clustering, FL_ClusteringDataAccess {

	private final FL_DataAccess _entityAccess;
	
	private final FL_Geocoding _geoCoder;
	
	private final EntityClusterer _clusterer;
	
	private final Map<String, Map<String,FL_Cluster>> _context;
	
	
	public DynamicClustering(FL_DataAccess entityAccess, FL_Geocoding geocoding, EntityClusterer clusterer) {
		_entityAccess = entityAccess;
		_clusterer = clusterer;
		_geoCoder = geocoding;
		_clusterer.init(new Object[] {new IdGenerator() {
			@Override
			public String nextId() {
				return 'Z'+UUID.randomUUID().toString();
			}
		}, _geoCoder
		});
		_context = new HashMap<String, Map<String, FL_Cluster>>();
	}
	
	@Override
	public String createContext() throws AvroRemoteException {
		String randContext = UUID.randomUUID().toString();
		_context.put(randContext, new HashMap<String, FL_Cluster>());
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

		List<String> roots = new ArrayList<String>();
		
		ClusterResults cr = _clusterer.clusterEntities(entities);
				
		for (FLEntityObject c : cr.getRoots()) {
			roots.add(c.getUid());
		}
		
		insertIntoContext(cr, contextId);
		
		return roots;
	}

	@Override
	public List<FL_Cluster> getEntities(List<String> entities, String contextId, String sessionId)
			throws AvroRemoteException {
		Map<String, FL_Cluster> clusters = _context.get(contextId);
		if (clusters == null) return Collections.emptyList();
		
		List<FL_Cluster> results = new ArrayList<FL_Cluster>();
		for (String id : entities) {
			FL_Cluster c = clusters.get(id);
			if (c!= null) {
				results.add(c);
			}
		}
		
		return results;
	}

	@Override
	public long removeMembers(List<String> entities, String contextId, String sessionId)
			throws AvroRemoteException {

		Map<String, FL_Cluster> clusters = _context.get(contextId);
		if (clusters == null) return 0;
		
		int count = 0;
		for (String id : entities) {
			FL_Cluster c = clusters.get(id);
			if (c!= null) {
				clusters.remove(id);
				count++;
			}
		}
		
		return count;
		
	}

	@Override
	public List<FL_Cluster> getContext(String contextId, String sessionId,
			boolean computeSummaries) throws AvroRemoteException {
		Map<String, FL_Cluster> clusters = _context.get(contextId);
		
		if (clusters == null) return Collections.emptyList();
		
		List<FL_Cluster> results = new ArrayList<FL_Cluster>();
		for (String id : clusters.keySet()) {
			FL_Cluster c = clusters.get(id);
			if (c!= null) {
				results.add(c);
			}
		}
		return results;
	}

	@Override
	public boolean clearContext(String contextId, String sessionId) throws AvroRemoteException {
		if (_context.containsKey(contextId)) {
			_context.remove(contextId);
			return true;
		}
		return false;
		
	}
		

	@Override
	public Map<String, List<FL_Cluster>> getAccounts(List<String> entities,
			String contextId, String sessionId) throws AvroRemoteException {
		return null;
	}

	@Override
	public Map<String, List<FL_Link>> getFlowAggregation(List<String> entities,
			List<String> focusEntities, FL_DirectionFilter direction,
			FL_LinkTag tag, FL_DateRange date, String entitiesContextId,
			String focusContextId, String sessionId) throws AvroRemoteException {
		
		try {
			if (focusEntities == null || focusEntities.isEmpty()) {
				Map<String, List<FL_Link>> relatedLinks = EntityAggregatedLinks.getRelatedAggregatedLinks(entities, direction, tag, date, _entityAccess, focusEntities, true, null,this,this,entitiesContextId, focusContextId, sessionId);
				return relatedLinks;
			} else {
				Map<String, List<FL_Link>> relatedLinks = EntityAggregatedLinks.getAggregatedLinks(entities, focusEntities, direction, tag, date, null, _entityAccess, this, entitiesContextId, focusContextId, sessionId);
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
	
	public void insertIntoContext(ClusterResults cr, String contextId) {
		Map<String, FLEntityObject> clusters = cr.getAllClusters();
		Map<String, FL_Cluster> contextClusters = _context.get(contextId);
		if (contextClusters == null) {
			contextClusters = new HashMap<String, FL_Cluster>();
			_context.put(contextId, contextClusters);
		}
		
		for (String ckey : clusters.keySet()) {
			FLEntityObject c = clusters.get(ckey);
			if (c.isCluster()) {  // TODO Should only cluster objects be stored in context??
				contextClusters.put(c.getUid(), c.cluster);
			}
		}
	}
	
	/**
	 * @param clusterIds
	 * @param context
	 * @return
	 * @throws AvroRemoteException
	 */
	private Set<String> getLeafIds(List<String> clusterIds, String context, String sessionId) throws AvroRemoteException {
		
		if (clusterIds == null || clusterIds.isEmpty()) return Collections.emptySet();
		
		
		Set<String> lids = new HashSet<String>();
		
		List<FL_Cluster> toSearch = getEntities(clusterIds, context, sessionId);
		
		List<String> probablyEntities = new ArrayList<String>(clusterIds); 
		
		for (FL_Cluster flc : toSearch) {
			probablyEntities.remove(flc.getUid());
			lids.addAll(flc.getMembers());
			if (flc.getSubclusters()!= null && !flc.getSubclusters().isEmpty()) {
				lids.addAll(getLeafIds(flc.getSubclusters(),context, sessionId));
			}
		}
		
		lids.addAll(probablyEntities);
		
		return lids;
	}
	

}
