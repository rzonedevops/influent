package influent.entity.clustering.utils;


import influent.idl.FL_BoundedRange;
import influent.idl.FL_Cluster;
import influent.idl.FL_Clustering;
import influent.idl.FL_ClusteringDataAccess;
import influent.idl.FL_DataAccess;
import influent.idl.FL_DateRange;
import influent.idl.FL_DirectionFilter;
import influent.idl.FL_Entity;
import influent.idl.FL_Link;
import influent.idl.FL_LinkTag;
import influent.idl.FL_Property;
import influent.idl.FL_PropertyTag;
import influent.idl.FL_PropertyType;
import influent.idlhelper.LinkHelper;
import influent.idlhelper.PropertyHelper;
import influent.idlhelper.SingletonRangeHelper;
import influent.midtier.api.DataAccessException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.AvroRemoteException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityAggregatedLinks {
	private static final int MIN_CLUSTER_SIZE = 2;
	private static final String PROP_LINK_COUNT = "link-count";
	private static final String PROP_DATE = "cluster-link-date-range";

//	private static final int MAX_ENTITY_BATCH = 10000;
	
	private static final Logger s_logger = LoggerFactory.getLogger(EntityAggregatedLinks.class);
	
	/**
	 *  Test Code @see TestLinkClustering 
	 */
	public static Map<String, List<FL_Link>> getRelatedAggregatedLinks(String entityId,
																	FL_DirectionFilter direction,
																	FL_LinkTag tag,
																	FL_DateRange dateRange,
																	FL_DataAccess da,
																	List<String> aggregationFocus,
																	boolean clusterAggregation,
																	List<Object> targetOutput,
																	FL_Clustering clusterer,
																	FL_ClusteringDataAccess cda,
																	String srcContext,
																	String dstContext,
																	String sessionId) throws DataAccessException, AvroRemoteException {
		return getRelatedAggregatedLinks(Collections.singletonList(entityId), direction, tag, dateRange, da, aggregationFocus, clusterAggregation, targetOutput, clusterer, cda, srcContext, dstContext, sessionId);
	}
	
	public static Map<String, List<FL_Link>> getRelatedAggregatedLinks(List<String> entityIds, 
																	FL_DirectionFilter direction, 
																	FL_LinkTag tag,
																	FL_DateRange dateRange,
																	FL_DataAccess da,
																	List<String> aggregationFocus,
																	
																	boolean clusterAggregation,
																	List<Object> targetOutput,
																	FL_Clustering clusterer,
																	FL_ClusteringDataAccess clusterAccess,
																	String srcContext,
																	String dstContext,
																	String sessionId
																	) throws DataAccessException, AvroRemoteException {
		
		
		// First attempt to get related cluster links for the entity id's
		try {
			s_logger.info("Started getRelatedAggregateLinks for "+entityIds.size()+" entities");
			
			if (entityIds == null || entityIds.isEmpty()) {
				s_logger.warn("Empty entity ID list passed to getRelatedAggregatedLinks");
				return null;
			}
			 
			long startms = System.currentTimeMillis();
			
			// keep track of the srcEntity ancestor of each descendant entity
			Map<String, String> entityAncestorIndex = new HashMap<String, String>();  // entity id -> ancestor id
						
			
			List<String> rawEntityIds = new ArrayList<String>();
			Map<String, FL_Cluster> existingClusters = new HashMap<String, FL_Cluster>();
			//First, split ids into raw and existing clusters
			for (String id : entityIds) {
				List<FL_Cluster> flc = clusterAccess.getEntities(Collections.singletonList(id), srcContext, sessionId);
				if (flc != null && !flc.isEmpty()) {
					existingClusters.put(id, flc.get(0));
				} else {
					rawEntityIds.add(id);
					entityAncestorIndex.put(id, id);
				}
			}
			
			// For non-clusters fetch their raw related links
			Map<String, List<FL_Link>> rawLinks = new HashMap<String, List<FL_Link>>();
			Map<String, List<FL_Link>> relatedLinks =  new HashMap<String, List<FL_Link>>();
			if (!rawEntityIds.isEmpty()) {
				rawLinks.putAll(da.getFlowAggregation(rawEntityIds, aggregationFocus, direction, tag ,dateRange));
			} 
			
			startms = System.currentTimeMillis();
			
			
			// Get all the descendant entity Ids associated with src clusters and create ancestor map
			List<String> srcEntityIds = buildEntityAncestorMap(existingClusters.values(), entityAncestorIndex, clusterAccess, srcContext, sessionId);
			
			s_logger.info("Created ancestor map (total time : "+(System.currentTimeMillis()-startms)/1000+"s)");
			
			startms = System.currentTimeMillis();
			
			// Get the related links for the srcEntities and add to the raw related links already fetched for non-cluster src entities
			rawLinks.putAll(da.getFlowAggregation(srcEntityIds, aggregationFocus, direction, tag ,dateRange));
			
			s_logger.info("Found related raw links (total time : "+(System.currentTimeMillis()-startms)/1000+"s)");
			
			// TODO SBL : Do we really need to ever pass back raw links? The below conditional could be removed
			// if the links are not requested to be aggregated then return the raw links 
					
			//TODO : still need the targets in this case!
			//Also, don't the links need to point to the originating cluster, instead of the raw underlying entity?
			if (!clusterAggregation) {
				// retrieve the dstEntities associated with rawLinks
				if (targetOutput!= null) {
					List<String> dstEntityIds = new ArrayList<String>();
					dstEntityIds.addAll(getDestLinkEntity(rawLinks));
					List<FL_Entity> dstEntities = da.getEntities(dstEntityIds);
					targetOutput.addAll(dstEntities);
				}
				return rawLinks;
			}
			
			// retrieve the dstEntities associated with rawLinks
			List<String> dstEntityIds = new ArrayList<String>();
			dstEntityIds.addAll(getDestLinkEntity(rawLinks));
			List<FL_Entity> dstEntities = da.getEntities(dstEntityIds);
			
			//Handle loop links
			for (String key : rawLinks.keySet()) {
				List<FL_Link> loops = new ArrayList<FL_Link>();
				for (FL_Link link : rawLinks.get(key)) {
					if (link.getSource().equals(link.getTarget())) {
						loops.add(link);
						
						//Find the entity in dstEntities, remove it and put it in targetOutput
						for (FL_Entity fle : dstEntities) {
							if (fle.getUid().equals(key)) {
								if (targetOutput!=null) {
									targetOutput.add(fle);
								}
								dstEntities.remove(fle);
								break;
							}
						}
					}
				}
				
				//Remove from the rawlinks (don't process in clustering)
				rawLinks.get(key).removeAll(loops);
				if (rawLinks.get(key).isEmpty()) {
					rawLinks.remove(key);
				}
				
				//Add to related links
				relatedLinks.put(key, loops);
			}
			
			// We now have loop free raw links from all src entities (whether they are members of a cluster or not) to dst entities
			// Now we can cluster dst entities and aggregate links on src and dst by cluster membership
			
			startms = System.currentTimeMillis();
			
			
			s_logger.info("Found "+dstEntities.size()+" destination entities (total time : "+(System.currentTimeMillis()-startms)/1000+"s)");
			
			// Determine which dst entities are not already clusters and need to be clustered
			// NOTE: They all are currently
			
			// Cluster the dst entities not already clustered
			if (dstEntities.size() > MIN_CLUSTER_SIZE) {
				startms = System.currentTimeMillis();
				
//				List<Entity> wrappedEntities = new ArrayList<Entity>();
//				List<FL_Entity> toRemove = new ArrayList<FL_Entity>();
//				for (FL_Entity fle : dstEntities) {
//					Entity e = FLWrapHelper.fromFLEntity(fle);
//					if (e != null) {
//						wrappedEntities.add(e);
//					} else {
//						toRemove.add(fle);
//					}
//				}
//				
//				for (FL_Entity fle : toRemove) {
//					dstEntities.remove(fle);
//				}
				
				// cluster the dst entities
				List<String> results = clusterer.clusterEntities(dstEntities, dstContext, sessionId);
				s_logger.info("Done clustering link entities (total time : "+(System.currentTimeMillis()-startms)/1000+"s)");
				
				//TODO : either add this method to the FL_ClusteringDataAccess interface (undesirable),
				//or come up with a better solution than this.

				List<FL_Cluster> ctxEnts = clusterAccess.getContext(dstContext, sessionId, false);
				
				Map<String, FL_Cluster> resultClusterMap = new HashMap<String, FL_Cluster>();
				for (FL_Cluster c : ctxEnts) {
					resultClusterMap.put(c.getUid(), c);
				}
				

				
				startms = System.currentTimeMillis();
				updateAncestorIndex(dstEntities, resultClusterMap, entityAncestorIndex);
				s_logger.info("Created link entity ancestor map (total time : "+(System.currentTimeMillis()-startms)/1000+"s)");
				
				//Add the root clusters to the target output list, if it isnt null
				if (targetOutput!=null) {
					targetOutput.addAll(results);
				}
				
			}
			else if (dstEntities.size() > 0) {
				for (FL_Entity dstEntity : dstEntities) {
					if (targetOutput!=null) {
						targetOutput.add(dstEntity);
					}
					entityAncestorIndex.put(dstEntity.getUid(), dstEntity.getUid());
				}
			}
			
			startms = System.currentTimeMillis();
			
			// now we can aggregate on the linkEntities and raw links - add the to the relatedLinks
			
			// Need to merge results, not put, or loop links might get lost.
			
			Map<String, List<FL_Link>> agLinks = aggregateLinks(rawLinks, direction, entityAncestorIndex, da) ;
			for (String agKey : agLinks.keySet()) {
				if (relatedLinks.containsKey(agKey)) {
					relatedLinks.get(agKey).addAll(agLinks.get(agKey));
				} else {
					relatedLinks.put(agKey, agLinks.get(agKey));
				}
			}
			
			s_logger.info("Aggregation complete (aggregation took "+(System.currentTimeMillis()-startms)/1000+"s)");
		
			return relatedLinks;
		
		} catch (Exception e) {
			s_logger.error("Exception caught during getRelatedAggregateLinks : "+e.getMessage(),e);
			throw new DataAccessException("Exception while aggregating links "+e.getMessage(),e);
		}															
	}

	
	public static Map<String, List<FL_Link>> getAggregatedLinks(List<String> srcEntityIds, 
															 List<String> dstEntityIds,
															 FL_DirectionFilter direction,
															 FL_LinkTag tag,
															 FL_DateRange dateRange, 
															 Collection<String> aggregationFocus, 
															 FL_DataAccess da,
															 FL_ClusteringDataAccess clusterAccess,
															 String srcCtx,
															 String dstCtx,
															 String sessionId) throws DataAccessException {
		
		try {
			s_logger.info("Started getAggregatedLinks for "+srcEntityIds.size()+" src entities and "+dstEntityIds.size()+ " dst entities");
			
			if (srcEntityIds == null || srcEntityIds.isEmpty()) {
				s_logger.warn("Empty src entity ID list passed to getAggregatedLinks");
				return null;
			}
			
			if (dstEntityIds == null || dstEntityIds.isEmpty()) {
				s_logger.warn("Empty dst entity ID list passed to getAggregatedLinks");
				return null;
			}
		
			// TODO need to first retrieve aggregate links that already exist
		
			long startms = System.currentTimeMillis();
			
			// retrieve the src entities
			
			List<Object> srcThings = new ArrayList<Object>();
			for (String id : srcEntityIds) {
				List<FL_Cluster> ec = clusterAccess.getEntities(Collections.singletonList(id), srcCtx, sessionId);
				if (ec != null && !ec.isEmpty()) {
					srcThings.add(ec.get(0));
				} 
			}
			
			srcThings.addAll(da.getEntities(srcEntityIds));
			
			s_logger.info("Found "+srcThings.size()+" src entities  (total time : "+(System.currentTimeMillis()-startms)/1000+"s)");
		
			startms = System.currentTimeMillis();
			
			// retrieve the dst entities
			List<Object> dstThings = new ArrayList<Object>(); 
			for (String id : dstEntityIds) {
				List<FL_Cluster> ec = clusterAccess.getEntities(Collections.singletonList(id), dstCtx, sessionId);
				if (ec != null && !ec.isEmpty()) {
					dstThings.add(ec.get(0));
				} 
			}
				
			List<FL_Entity> dstTmp = da.getEntities(dstEntityIds);
			for (FL_Entity o :dstTmp) {
				dstThings.add(o);
			}
			
			
			s_logger.info("Found "+dstThings.size()+" dst entities  (total time : "+(System.currentTimeMillis()-startms)/1000+"s)");
		
			// keep track of the parent of each entity
			Map<String, String> entityAncestorIndex = new HashMap<String, String>();  // entity id -> ancestor id
		
			startms = System.currentTimeMillis();
			
			//TODO : refactoring in progress
			
			// Get all the entity Ids associated with srcEntities and create ancestor map
			List<String> srcChildEntityIds = buildEntityAncestorMap(srcThings, entityAncestorIndex,clusterAccess,srcCtx, sessionId);
		
			// create ancestor map for dstEntities
			List<String> dstChildEntityIds = buildEntityAncestorMap(dstThings, entityAncestorIndex,clusterAccess,dstCtx, sessionId);
			
			s_logger.info("Created ancestor map (total time : "+(System.currentTimeMillis()-startms)/1000+"s)");
		
			startms = System.currentTimeMillis();
			
			// Get the related links for the srcEntities
			// HACK this forces retrieving the raw links each time - we need to fetch aggregate links if they are available
			
			// HACK : src and dst ids are coming inswitched around, it seems, if direction is DESTINATION?
			
			Map<String, List<FL_Link>> rawLinks = null;
			rawLinks = da.getFlowAggregation(srcChildEntityIds, dstChildEntityIds, direction, tag ,dateRange);
					
			
			s_logger.info("Found related raw links (total time : "+(System.currentTimeMillis()-startms)/1000+"s)");
			
			startms = System.currentTimeMillis();
			
			// now we can aggregate on the linkEntities and raw links	
			Map<String, List<FL_Link>> aggregatedLinks = aggregateLinks(rawLinks, direction, entityAncestorIndex, da);
			
			s_logger.info("Aggregation complete (aggregation took "+(System.currentTimeMillis()-startms)/1000+"s)");
			
			return aggregatedLinks;
		} catch (Exception e) {
			s_logger.error("Exception caught during getAggregatedLinks : "+e.getMessage(),e);
			throw new DataAccessException("Exception while aggregating links "+e.getMessage(),e);
		}	
	}
	
	/*private static Map<String, List<FL_Link>> filterNonClusterLinks(Map<String, List<FL_Link>> links) {
		Map<String, List<Link>> filterLinks = new HashMap<String, List<Link>>();
		
		for (String key : links.keySet()) {
			for (FL_Link link : links.get(key)) {
				if (link.getTag() == ItemTag.CLUSTER) {
					List<FL_Link> clusterLinks = filterLinks.get(key);
					if (clusterLinks == null) {
						clusterLinks = new LinkedList<Link>();
						filterLinks.put(key, clusterLinks);
					}
					clusterLinks.add(link);
				}
			}
		}
		return filterLinks;
	}*/
	
	
	
	private static void updateAncestorIndex(List<FL_Entity> entities, Map<String, ? extends Object> index, Map<String, String> ancestorMap) {
		Map<String, Object> searchIndex = new HashMap<String, Object>(index);
		for (FL_Entity entity : entities) {
			String entityId = entity.getUid();
			
			// find the ancestor for the entity and add to ancestor map
			for (String key : searchIndex.keySet()) {
				Object searchEntity = searchIndex.get(key);
				if ((searchEntity instanceof FL_Cluster) && ((FL_Cluster)searchEntity).getMembers().contains(entityId)) {
					FL_Cluster ancestor = (FL_Cluster)searchEntity;
					while ( ancestor.getParent()!=null && !ancestor.getParent().isEmpty() ) {
						ancestor = (FL_Cluster)index.get(ancestor.getParent());
					}
					ancestorMap.put(entityId, ancestor.getUid());
				}
			}
			// if we could not find an ancestor then add the entity to the ancestor map as an ancestor to itself
			if (!ancestorMap.containsKey(entityId)) {
				ancestorMap.put(entityId, entityId);
			}
		}
	}
	
	private static List<String> buildEntityAncestorMap(Collection<? extends Object> clusters, 
			 										   Map<String, String> entityAncestorIndex,
			 										   FL_ClusteringDataAccess clusterAccess,
			 										   String context,
			 										   String sessionId) throws AvroRemoteException {
		List<String> entityIds = new LinkedList<String>();

		for (Object entity : clusters) {
			if (entity instanceof FL_Cluster) {
				// find all the entities that are decendents of this cluster
				List<String> childIds = fetchLeaves( clusterAccess, (FL_Cluster)entity, context, sessionId);

				// keep track of the ancestor for each childId
				for (String childId : childIds) {
					entityIds.add(childId);
					entityAncestorIndex.put(childId, ((FL_Cluster)entity).getUid());
				}
			} else if (entity instanceof FL_Entity) {
				FL_Entity fle = (FL_Entity)entity;
				entityIds.add(fle.getUid());
				entityAncestorIndex.put(fle.getUid(), fle.getUid());
			}
		}
		return entityIds;
	}
	
	private static List<String> fetchLeaves(FL_ClusteringDataAccess clusterAccess, FL_Cluster cluster, String context, String sessionId) throws AvroRemoteException {
		
		List<String> eids = new ArrayList<String>();
		Deque<FL_Cluster> toProcess = new LinkedList<FL_Cluster>();
		toProcess.addLast(cluster);
		
		while(!toProcess.isEmpty()) {
			FL_Cluster procCluster = toProcess.removeFirst();
			if (!procCluster.getSubclusters().isEmpty()) {
				toProcess.addAll(clusterAccess.getEntities(procCluster.getSubclusters(), context, sessionId));
			}
			if (!procCluster.getMembers().isEmpty()) {
				eids.addAll(procCluster.getMembers());
			}
		}
		return eids;
		
	}
	
	public static Map<String, List<FL_Link>> aggregateLinks(Map<String, 
														  List<FL_Link>> links, 
														  FL_DirectionFilter direction, 
														  Map<String, String> ancestorIndex,
														  FL_DataAccess da) {
		Map<String, FL_Link> aggregateLinks = new HashMap<String, FL_Link>();
		
		for (String id : links.keySet()) {
			for (FL_Link link : links.get(id)) {
				String srcId = link.getSource();
				//if (srcId.indexOf("-") > -1) srcId = srcId.substring(0, srcId.indexOf("-"));
				String src = ancestorIndex.get(srcId);
				String destId = link.getTarget();
				//if (destId.indexOf("-") > -1) destId = destId.substring(0, destId.indexOf("-"));
				String dst = ancestorIndex.get(destId);
				
				// only process links that are in the ancestorIndex
				if (src == null || dst == null) continue;
			
				String key = src + "_:_" + dst;  // generate unique key for this cluster link

				FL_Link aggregateLink = aggregateLinks.get(key);
			
				if (aggregateLink == null) {
					List<FL_Property> props = new ArrayList<FL_Property>();
				
					DateTime minDate = null;
					DateTime maxDate = null;
					
					for (FL_Property prop : link.getProperties()) {
						PropertyHelper p = PropertyHelper.from(prop);
						props.add(p);
						if (p.getType() == FL_PropertyType.DATE) {
							DateTime pdate = new DateTime(p.getValue());
							if (minDate== null || minDate.isAfter(pdate)) {
								minDate = pdate;
							}
							if (maxDate == null || maxDate.isBefore(pdate)) {
								maxDate = pdate;
							}
						}
					}
					FL_Property linkCount = new PropertyHelper(PROP_LINK_COUNT, PROP_LINK_COUNT, 1, FL_PropertyType.LONG, Collections.singletonList(FL_PropertyTag.CONSTRUCTED));
					props.add(linkCount);
					
					if (minDate != null && maxDate != null) {
						List<FL_PropertyTag> datePropTags = new ArrayList<FL_PropertyTag>();
						datePropTags.add(FL_PropertyTag.CONSTRUCTED);
						datePropTags.add(FL_PropertyTag.STAT);
						datePropTags.add(FL_PropertyTag.DATE);
						FL_Property dateProp = new PropertyHelper(PROP_DATE,PROP_DATE, minDate, maxDate, FL_PropertyType.DATE, datePropTags);
	
						props.add(dateProp);
					}
					
					aggregateLink = FL_Link.newBuilder().setDirected(link.getDirected()).setProperties(props).setProvenance(null)
							.setSource(src).setTags(Collections.singletonList(FL_LinkTag.FINANCIAL)).setTarget(dst).setUncertainty(null).build();
					
					//aggregateLink = new FL_Link(src, dst, "", ItemTag.CLUSTER, link.getType(), props, link.isDirected());
					aggregateLinks.put(key, aggregateLink);
				}
				else {
					// otherwise increment the link count for the cluster link
					PropertyHelper linkCount = LinkHelper.getFirstProperty(aggregateLink, PROP_LINK_COUNT);
					PropertyHelper dateProp = LinkHelper.getFirstProperty(aggregateLink, PROP_DATE);

					int count = ((Integer)linkCount.getValue())+1;
					linkCount.setRange(new SingletonRangeHelper(count, FL_PropertyType.LONG));
				
					DateTime minDate = null;
					DateTime maxDate = null;
					if (dateProp != null) {
						FL_BoundedRange range = (FL_BoundedRange) dateProp.getRange();
						minDate = new DateTime(range.getStart());
						maxDate = new DateTime(range.getEnd());
					}
					
					// aggregate properties
					for (FL_Property prop : link.getProperties()) {
						PropertyHelper p = PropertyHelper.from(prop);
						//First, handle dates and update min-max date
						if (p.getType() == FL_PropertyType.DATE && minDate != null && maxDate!=null) {
							DateTime pdate = new DateTime(p.getValue());
							if (minDate== null || minDate.isAfter(pdate)) {
								minDate = pdate;
								((FL_BoundedRange)dateProp.getRange()).setStart(minDate);
							}
							if (maxDate == null || maxDate.isBefore(pdate)) {
								maxDate = pdate;
								((FL_BoundedRange)dateProp.getRange()).setEnd(maxDate);
							}
							
						}
						
						
						List<PropertyHelper> a = LinkHelper.getProperties(aggregateLink, p.getKey());
						if (a != null && a.size() > 0) {
							switch (a.get(0).getType()) {
							case LONG:
								try {
									int oldival = (Integer)a.get(0).getValue();
									int ival = (Integer)p.getValue();
									a.get(0).setRange(new SingletonRangeHelper(oldival + ival, FL_PropertyType.LONG));
								} catch (Exception e) { /* ignore */ }
								break;
							case DOUBLE:
								try {
									double olddval = (Double)a.get(0).getValue();
									double dval = (Double)p.getValue();
									a.get(0).setRange(new SingletonRangeHelper(olddval + dval, FL_PropertyType.DOUBLE));
								} catch (Exception e) { /* ignore */ }
								break;
							default:
								continue;
							}
						}
						else {
							link.getProperties().add(p);
						}
					}
				}
			}
		}
		
		Map<String, List<FL_Link>> results = new HashMap<String, List<FL_Link>>();
		
//		List<Item> saveLinks = new LinkedList<Item>();
		// create the resulting links for each src entity
		for (String key : aggregateLinks.keySet()) {
			FL_Link link = aggregateLinks.get(key);
		
			String[] tokens = key.split("_:_");
				
			String src = (direction == FL_DirectionFilter.SOURCE) ? tokens[0] : tokens[1];
			
			List<FL_Link> linkset = results.get(src);
			
			if (linkset == null) {
				linkset = new LinkedList<FL_Link>();
				results.put(src, linkset);
			}
			linkset.add(link);
			//saveLinks.add(link);
		}
		
		// save the links to the transient datastore
		//da.storeItems(saveLinks,false);
		
		return results;
	}
	
	
	
	private static Set<String> getDestLinkEntity(Map<String, List<FL_Link>> links) {
		// Get all the dst entity Ids associated with links
		Set<String> dstEntityIds = new HashSet<String>();
		for (String src : links.keySet()) {
			for (FL_Link link : links.get(src)) {
				if (link.getTarget().equalsIgnoreCase(src)) {
					dstEntityIds.add(link.getSource());
				}
				else {
					dstEntityIds.add(link.getTarget());
				}
			}
		}
		return dstEntityIds;
	}
}
