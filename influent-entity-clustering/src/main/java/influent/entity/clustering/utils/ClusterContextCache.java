package influent.entity.clustering.utils;

import influent.idl.FL_Cluster;
import influent.idl.FL_Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterContextCache {

//	private static final Logger s_logger = LoggerFactory.getLogger(ClusterContextCache.class);
	public static final ClusterContextCache instance;
	
	static {
		instance = new ClusterContextCache();
	}
	
	
	private Map<String, Set<String>> _contextContentMap = new HashMap<String, Set<String>>();
	private Map<String, Map<String,FL_Cluster>> _contextFileMap = new HashMap<String , Map<String, FL_Cluster>>();
	private Map<String, Map<String,FL_Cluster>> _contextClusterMap = new HashMap<String , Map<String, FL_Cluster>>();
	private Map<String, Map<String,FL_Entity>> _contextEntityMap = new HashMap<String , Map<String, FL_Entity>>();
	private Map<String, Map<String, Boolean>> _summariesMap = new HashMap<String, Map<String, Boolean>>();
	
	private ClusterContextCache() {
		
	}

	/**
	 * Returns true if an id (entity or cluster) is represented in a particular context.
	 * @param entityId
	 * @param contextId
	 * @return
	 */
	public boolean isIdInContext(String id, String contextId) {
		Set<String> contents = _contextContentMap.get(contextId);
		if (contents == null) return false;
		return contents.contains(id);
	}
	
	/**
	 * Returns true if the set of ids are all contained in the given context
	 * @param ids
	 * @param contextId
	 * @return
	 */
	public boolean areIdsInContext(Collection<String> ids, String contextId) {
		Set<String> contents = _contextContentMap.get(contextId);
		if (contents == null) return false;
		return contents.containsAll(ids);
	}
	
	/**
	 * Given an id and context, return the associated file.
	 * @param id
	 * @param contextId
	 * @return
	 */
	public FL_Cluster getFile(String id, String contextId) {
		Map<String, FL_Cluster> fileMap = _contextFileMap.get(contextId);
		if (fileMap == null) return null;
		return fileMap.get(id);
	}
	
	/**
	 * Given an id and context, return the associated cluster.
	 * @param id
	 * @param contextId
	 * @return
	 */
	public FL_Cluster getCluster(String id, String contextId) {
		Map<String, FL_Cluster> clusterMap = _contextClusterMap.get(contextId);
		if (clusterMap == null) return null;
		return clusterMap.get(id);
	}
	
	/**
	 * Given an id and context, return the associated file.
	 * @param id
	 * @param contextId
	 * @return
	 */
	public FL_Entity getEntity(String id, String contextId) {
		Map<String, FL_Entity> entityMap = _contextEntityMap.get(contextId);
		if (entityMap == null) return null;
		return entityMap.get(id);
	}
	
	/**
	 * Given a set of ids and a context, return the list of clusters that exist in the cache,
	 * but do not have their summaries computed.  If an id does not exist in the context, then it will not
	 * be returned.  This method is intended to inform the calling class which clusters need to be fetched
	 * to have their summaries computed.
	 * @param ids
	 * @param contextId
	 * @return
	 */
	public List<String> getClusterIdsWithoutSummaries(List<String> ids, String contextId) {
		Map<String, Boolean> summaryMap = _summariesMap.get(contextId);
		if (summaryMap == null) return Collections.emptyList();
		List<String> nosumids = new ArrayList<String>();
		for (String id : ids) {
			Boolean summary = summaryMap.get(id);
			if (summary != null && summary==false) nosumids.add(id);
		}
		return nosumids;
		
	}

	public List<String> getContentIds(String contextId) {
		if(_contextClusterMap.containsKey(contextId)) {
			return new ArrayList<String>(_contextContentMap.get(contextId));
		}
		else {
			return Collections.<String>emptyList();
		}
	}
	
	public List<FL_Cluster> getFiles(Collection<String> ids, String contextId) {
		Map<String, FL_Cluster> fileMap = _contextFileMap.get(contextId);
		if (fileMap == null) return Collections.emptyList();
		List<FL_Cluster> results = new ArrayList<FL_Cluster>();
		for (String id : ids) {
			FL_Cluster c = fileMap.get(id);
			if (c != null) {
				results.add(c);
			}
		}
		return results;
	}
	
	public List<FL_Cluster> getClusters(Collection<String> ids, String contextId) {
		Map<String, FL_Cluster> clusterMap = _contextClusterMap.get(contextId);
		if (clusterMap == null) return Collections.emptyList();
		List<FL_Cluster> results = new ArrayList<FL_Cluster>();
		for (String id : ids) {
			FL_Cluster c = clusterMap.get(id);
			if (c != null) {
				results.add(c);
			}
		}
		return results;
	}
	
	public List<FL_Entity> getEntities(Collection<String> ids, String contextId) {
		Map<String, FL_Entity> entityMap = _contextEntityMap.get(contextId);
		if (entityMap == null) return Collections.emptyList();
		List<FL_Entity> results = new ArrayList<FL_Entity>();
		for (String id : ids) {
			FL_Entity c = entityMap.get(id);
			if (c != null) {
				results.add(c);
			}
		}
		return results;
	}
	
	public List<FL_Cluster> getFiles(String contextId) {
		if(_contextFileMap.containsKey(contextId)) {
			return new ArrayList<FL_Cluster>(_contextFileMap.get(contextId).values());
		}
		else {
			return Collections.emptyList();
		}
	}
	
	public List<FL_Cluster> getClusters(String contextId) {
		if(_contextClusterMap.containsKey(contextId)) {
			return new ArrayList<FL_Cluster>(_contextClusterMap.get(contextId).values());
		}
		else {
			return Collections.emptyList();
		}
	}
	
	public void mergeIntoContext(Collection<FL_Cluster> clusters, String contextId, boolean summariesComputed, boolean updateMembers) {
		mergeIntoContext(Collections.<FL_Cluster>emptyList(), clusters, Collections.<FL_Entity>emptyList(), contextId, summariesComputed, updateMembers);
	}
	
	public void mergeIntoContext(Collection<FL_Cluster> files, Collection<FL_Cluster> clusters, Collection<FL_Entity> entities, String contextId, boolean summariesComputed, boolean updateMembers) {
		Map<String, FL_Cluster> fileMap = _contextFileMap.get(contextId);
		if (fileMap == null) {
			fileMap = new HashMap<String, FL_Cluster>();
			_contextFileMap.put(contextId, fileMap);
		}
		
		Map<String, FL_Cluster> clusterMap = _contextClusterMap.get(contextId);
		if (clusterMap == null) {
			clusterMap = new HashMap<String, FL_Cluster>();
			_contextClusterMap.put(contextId, clusterMap);
		}
		
		Map<String, FL_Entity> entityMap = _contextEntityMap.get(contextId);
		if (entityMap == null) {
			entityMap = new HashMap<String, FL_Entity>();
			_contextEntityMap.put(contextId, entityMap);
		}
		
		Map<String, Boolean> summaryMap = _summariesMap.get(contextId);
		if (summaryMap == null) {
			summaryMap = new HashMap<String, Boolean>();
			_summariesMap.put(contextId, summaryMap);
		}
		
		Set<String> contentSet = _contextContentMap.get(contextId);
		if (contentSet == null) {
			contentSet = new HashSet<String>();
			_contextContentMap.put(contextId, contentSet);
		}
		
		for (FL_Cluster file : files) {
			if (fileMap.containsKey(file.getUid())) {
				FL_Cluster oldFile = fileMap.get(file.getUid());		
				oldFile.setProperties(file.getProperties());
				oldFile.setMembers(file.getMembers());
			} else {
				fileMap.put(file.getUid(), file);
				contentSet.add(file.getUid());
				contentSet.addAll(file.getMembers());
			}
		}
		
		for (FL_Cluster cluster : clusters) {
			
			//if the cluster already exists, just replace the properties
			//DON'T replace the whole thing, or the simplification pointers will be overwritten
			if (clusterMap.containsKey(cluster.getUid())) {
				FL_Cluster oldCluster = clusterMap.get(cluster.getUid());		
				oldCluster.setProperties(cluster.getProperties());
				oldCluster.setUncertainty(cluster.getUncertainty());
				oldCluster.setProvenance(cluster.getProvenance());
				
				if (updateMembers) {
					oldCluster.setMembers(cluster.getMembers());
					oldCluster.setSubclusters(cluster.getSubclusters());
					contentSet.addAll(cluster.getMembers());
				}
			} else {
				clusterMap.put(cluster.getUid(), cluster);
				contentSet.add(cluster.getUid());
				//Not putting in subclusters, they will be handled on their own interation through the clusters list
				contentSet.addAll(cluster.getMembers());
			}
			
			
			summaryMap.put(cluster.getUid(), summariesComputed);
			

		}
		
		for (FL_Entity entity : entities) {
			if (entityMap.containsKey(entity.getUid())) {
				FL_Entity oldEntity = entityMap.get(entity.getUid());		
				oldEntity.setProperties(entity.getProperties());
			} else {
				entityMap.put(entity.getUid(), entity);
				contentSet.add(entity.getUid());
			}
		}
	}
	
	public void removeFromContext(String contextId, Collection<String> ids) {
		Map<String, FL_Cluster> clusterMap = _contextClusterMap.get(contextId);
		Set<String> contentSet = _contextContentMap.get(contextId);

		for(String id : ids) {
			clusterMap.remove(id);
			contentSet.remove(id);
		}
	}
	
	public void clearContext(String contextId) {
		_contextFileMap.remove(contextId);
		_contextClusterMap.remove(contextId);
		_contextEntityMap.remove(contextId);
		_contextContentMap.remove(contextId);
	}
	
}
