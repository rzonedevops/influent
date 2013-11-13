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
package influent.entity.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.AvroRemoteException;

import influent.entity.clustering.utils.EntityClusterFactory;
import influent.entity.clustering.utils.PropertyManager;
import influent.idl.FL_Cluster;
import influent.idl.FL_Entity;
import influent.idl.FL_Frequency;
import influent.idl.FL_GeoData;
import influent.idl.FL_Geocoding;
import influent.idl.FL_Property;
import influent.idl.FL_PropertyTag;
import influent.idl.FL_SingletonRange;
import influent.idlhelper.ClusterHelper;
import influent.idlhelper.EntityHelper;
import influent.idlhelper.PropertyHelper;
import influent.midtier.IdGenerator;
import influent.midtier.api.Context;
import influent.midtier.api.EntityClusterer;

public class GeneralEntityClusterer extends BaseEntityClusterer {
	private FL_Geocoding geoCoder;
	private IdGenerator idGenerator;
	private PropertyManager pMgr;
	private List<EntityClusterer> clusterStages;
	private int MIN_CLUSTER_SIZE = 10;

	@Override
	public void init(Object[] args) {
		try {
			idGenerator = (IdGenerator)args[0];
			geoCoder = (FL_Geocoding)args[1];
			pMgr = (PropertyManager)args[2];
			MIN_CLUSTER_SIZE = Integer.parseInt(pMgr.getProperty(PropertyManager.MIN_CLUSTER_SIZE, "10"));
			clusterStages = createClusterStages(pMgr);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Invalid initialization parameters.", e);
		}
	}
	
	private List<EntityClusterer> createClusterStages(PropertyManager pMgr){
		List<EntityClusterer> clusterStages = new ArrayList<EntityClusterer>();
		
		String[] clusterFields = pMgr.getProperty(PropertyManager.CLUSTER_FIELDS, "GEO:geo,TYPE:categorical,LABEL:label").split(",");
	
		// create a cluster factory each stage will use to create FL_Cluster objects
		EntityClusterFactory clusterFactory = new EntityClusterFactory(idGenerator, geoCoder);
		
		for (String field : clusterFields) {
			String[] tokens = field.split(":");  // TagOrFieldName, Type, Fuzzy (if label feature)
			if (tokens.length > 1) {
				String tagOrFieldName = tokens[0];
				String type = tokens[1];
				
				if (type.equalsIgnoreCase("categorical")) {
					CategoricalEntityClusterer clusterer = new CategoricalEntityClusterer();
					Object[] params = {clusterFactory, tagOrFieldName}; 
					clusterer.init(params);
					clusterStages.add(clusterer);
				}
				else if (type.equalsIgnoreCase("label")) {
					String clusterType = tokens.length == 3 ? tokens[2] : "fingerprint";  // default to fingerprint if none
					
					// first group alphabetically
					LabelEntityClusterer clusterer = new LabelEntityClusterer();
					Object[] params = {clusterFactory, tagOrFieldName, "alpha", pMgr}; 
					clusterer.init(params);
					clusterStages.add(clusterer);
					
					// next fuzzy or finger print cluster
					clusterer = new LabelEntityClusterer();
					Object[] params2 = {clusterFactory, tagOrFieldName, clusterType, pMgr}; 
					clusterer.init(params2);
					clusterStages.add(clusterer);
				}
				else if (type.equalsIgnoreCase("geo")) {
					GeoEntityClusterer clusterer = new GeoEntityClusterer();
					Object[] params = {clusterFactory, tagOrFieldName, geoCoder, GeoEntityClusterer.GEO_LEVEL.Continent}; 
					clusterer.init(params);
					clusterStages.add(clusterer);
					
					clusterer = new GeoEntityClusterer();
					Object[] params2 = {clusterFactory, tagOrFieldName, geoCoder, GeoEntityClusterer.GEO_LEVEL.Region}; 
					clusterer.init(params2);
					clusterStages.add(clusterer);
					
					clusterer = new GeoEntityClusterer();
					Object[] params3 = {clusterFactory, tagOrFieldName, geoCoder, GeoEntityClusterer.GEO_LEVEL.Country}; 
					clusterer.init(params3);
					clusterStages.add(clusterer);
					
					clusterer = new GeoEntityClusterer();
					Object[] params4 = {clusterFactory, tagOrFieldName, geoCoder, GeoEntityClusterer.GEO_LEVEL.LatLon}; 
					clusterer.init(params4);
					clusterStages.add(clusterer);
				}
				else if (type.equalsIgnoreCase("numeric")) {
					Integer numBins = tokens.length == 3 ? Integer.parseInt(tokens[2]) : 5;  // default to 5 bins
					NumericEntityClusterer clusterer = new NumericEntityClusterer();
					Object[] params = {clusterFactory, tagOrFieldName, numBins}; 
					clusterer.init(params);
					clusterStages.add(clusterer);
				}
				else {
					log.warn("Invalid cluster field type specified in clusterer.properties - verify the value of entity.clusterer.clusterfields");
				}
			}
			else {
				log.warn("Invalid cluster field specified in clusterer.properties - verify the value of entity.clusterer.clusterfields");
			}
		}
		
		return clusterStages;
	}
	
	private List<FL_Entity> getClusterMembers(FL_Cluster cluster, Context context) {
		List<FL_Entity> members = new LinkedList<FL_Entity>();
		
		for (String id : cluster.getMembers()) {
			members.add(context.entities.get(id));
		}
		return members;
	}
	
	private List<FL_Cluster> getSubClusters(FL_Cluster cluster, Context context) {
		List<FL_Cluster> subClusters = new LinkedList<FL_Cluster>();
		
		for (String id : cluster.getSubclusters()) {
			subClusters.add( context.clusters.get(id) );
		}
		return subClusters;
	}

	@Override
	public Context clusterEntities(Collection<FL_Entity> entities) {
		return this.clusterEntities(entities, new Context());
	}
	
	@Override
	public Context clusterEntities(Collection<FL_Entity> entities, Collection<FL_Cluster> clusters, Context context) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Context clusterEntities(Collection<FL_Entity> entities, Context context) {
		if (clusterStages == null || clusterStages.isEmpty()) return null;
	
		// create a default context if the passed in one is empty
		if (context == null) {
			context = new Context();
		}
		
		// add the entities to the context
		context.addEntities(entities);
		
		// first stage generates root objects in entity cluster hierarchy
		Context results = clusterStages.get(0).clusterEntities(entities, context.roots.values(), context);
		
		// add the new/modified roots to the context roots
		context.roots.putAll(results.roots);
		
		// keep track of the modified roots - only they need to have their summaries recomputed
		Map<String, FL_Cluster> modifiedRoots = new HashMap<String, FL_Cluster>(results.roots);
		
		// find the candidate clusters to split
		Collection<FL_Cluster> clustersToSplit = new LinkedList<FL_Cluster>();
		findClustersToSplit(results.roots, clustersToSplit);
		
		// add the roots to the allClusters map in context
		context.clusters.putAll(results.roots);
		
		//
		// Top down Divisive hierarchical clustering using binning and k-means clustering - each stage clusters by a distinct feature
		//
		for (int i=1; i < clusterStages.size(); i++) {
			EntityClusterer clusterer = clusterStages.get(i); 
					
			Map<String, FL_Cluster> stageResults = new HashMap<String, FL_Cluster>();
			
			for (FL_Cluster cluster : clustersToSplit) {
				// sub-cluster the entity cluster
				results = clusterer.clusterEntities( getClusterMembers(cluster, context), getSubClusters(cluster, context), context );
				
				// update the cluster children to be the sub-clustering results
				// and update the root and parent of the children
				updateClusterReferences(cluster, results.roots);	

				// store the new/modified clusters in the context
				context.clusters.putAll(results.roots);
					
				// schedule the cluster for further splitting on another stage
				stageResults.putAll(results.roots);
			}
			// update the clusters to split for next stage
			clustersToSplit.clear();
			findClustersToSplit(stageResults, clustersToSplit);
		}
		
		// lastly update the modified clusters summaries
		updateClusterSummaries(modifiedRoots, context);
		
		// return back the modified context
		return context;
	}
	
	private void findClustersToSplit(Map<String, FL_Cluster> candidates, Collection<FL_Cluster> splitQueue) {
		for (String id : candidates.keySet()) {
			FL_Cluster c = candidates.get(id);
			if (c.getMembers().size() > MIN_CLUSTER_SIZE || (c.getMembers().size() > 0 && c.getSubclusters().size() > 0))  {
				splitQueue.add(c);
			}
		}
	}

	private void updateClusterReferences(FL_Cluster cluster, Map<String, FL_Cluster> children) {
		List<String> childClusterIds = new LinkedList<String>();
		
		String parentId = cluster.getUid();
		String rootId = cluster.getRoot() == null ? parentId : cluster.getRoot();
		
		for (String id : children.keySet()) {
			FL_Cluster c = children.get(id);
			c.setParent(parentId);
			c.setRoot(rootId);
			childClusterIds.add(c.getUid());
		}
		cluster.getMembers().clear();
		Set<String> uniqueIds = new HashSet<String>(cluster.getSubclusters());
		uniqueIds.addAll(childClusterIds);
		cluster.setSubclusters(new LinkedList<String>(uniqueIds));
	}
	
	private void updateClusterSummaries(Map<String, FL_Cluster> clusters, Context context) {
		for (String id : clusters.keySet()) {
			FL_Cluster cluster = clusters.get(id);
			updateClusterSummaries(cluster, context);
		}
	}
	
	protected void increment(String key, double increment, Map<String, Double> index) {
		double count = 0;
		
		if (index.containsKey(key)) {
			count = index.get(key);
		}
		count += increment;
		index.put(key, count);
	}
	
	protected void increment(List<FL_Frequency> stats, Map<String, Double> index) {
		for (FL_Frequency freq : stats) {
			String key = "";
			if (freq.getRange() instanceof FL_GeoData) {
				key = ((FL_GeoData)freq.getRange()).getCc();
			}
			else {
				key = (String)freq.getRange();
			}
			double count = freq.getFrequency();
			increment(key, count, index);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void updateClusterSummaries(FL_Cluster cluster, Context context) {
		// depth first search to update cluster summaries
		if (cluster.getSubclusters().isEmpty()) {
			// revise the cluster count
			FL_Property prop = ClusterHelper.getFirstProperty(cluster, "count");
			FL_SingletonRange range = (FL_SingletonRange) prop.getRange();
			range.setValue( cluster.getMembers().size() );
			
			Map<String, Double> locationSummary = new HashMap<String, Double>();
			Map<String, Double> typeSummary = new HashMap<String, Double>();
			
			for (String memberId : cluster.getMembers()) {
				FL_Entity entity = context.entities.get(memberId);
				
				// revise the type distribution
				PropertyHelper typeProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.TYPE);
				if (typeProp != null) {
					increment((String)typeProp.getValue(), 1, typeSummary);
				}

				// revise the location distribution
				PropertyHelper geoProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.GEO);
				if (geoProp != null) {
					FL_GeoData geoData = (FL_GeoData)geoProp.getValue(); 
					String cc = geoData.getCc();
					if (cc != null && !cc.isEmpty()) {
						increment(cc, 1, locationSummary);
					}
				}
			}	
			List<FL_Frequency> typeFreqs = (List<FL_Frequency>) ClusterHelper.getFirstProperty(cluster, "type-dist").getValue();
			
			// update the type distribution range property
			typeFreqs.clear();
			for (String key : typeSummary.keySet()) {
				typeFreqs.add( new FL_Frequency(key, typeSummary.get(key)) );
			}		
			
			List<FL_Frequency> locFreqs = (List<FL_Frequency>) ClusterHelper.getFirstProperty(cluster, "location-dist").getValue();
			
			// update the location distribution range property
			locFreqs.clear();
			for (String key : locationSummary.keySet()) {
				FL_GeoData geo = new FL_GeoData(null, null, null, key);
				try {
					geoCoder.geocode(Collections.singletonList(geo));
				} catch (AvroRemoteException e) { /* ignore */ }
				locFreqs.add( new FL_Frequency(geo, locationSummary.get(key)) );
			}
		}
		else {
			int count = 0;
			Map<String, Double> locationSummary = new HashMap<String, Double>();
			Map<String, Double> typeSummary = new HashMap<String, Double>();
			
			// update each child cluster first 
			for (String subClusterId : cluster.getSubclusters()) {
				FL_Cluster subCluster = context.clusters.get(subClusterId);
				updateClusterSummaries(subCluster, context);
				FL_Property prop = ClusterHelper.getFirstProperty(subCluster, "count");
				FL_SingletonRange range = (FL_SingletonRange) prop.getRange();
				count += (Integer)range.getValue();
				
				List<FL_Frequency> typeFreqs = (List<FL_Frequency>) ClusterHelper.getFirstProperty(subCluster, "type-dist").getValue();
				for (FL_Frequency freq : typeFreqs) {
					String type = (String)freq.getRange();
					if (typeSummary.containsKey(type)) {
						Double f = typeSummary.get(type);
						typeSummary.put(type, f + freq.getFrequency());
					}
					else {
						typeSummary.put(type, freq.getFrequency());
					}
				}
				
				List<FL_Frequency> locationFreqs = (List<FL_Frequency>) ClusterHelper.getFirstProperty(subCluster, "location-dist").getValue();
				for (FL_Frequency freq : locationFreqs) {
					FL_GeoData geo = (FL_GeoData)freq.getRange();
					if (locationSummary.containsKey(geo.getCc())) {
						Double f = locationSummary.get(geo.getCc());
						locationSummary.put(geo.getCc(), f + freq.getFrequency());
					}
					else {
						locationSummary.put(geo.getCc(), freq.getFrequency());
					}
				}
			}
			// now update this cluster
			FL_Property prop = ClusterHelper.getFirstProperty(cluster, "count");
			FL_SingletonRange range = (FL_SingletonRange) prop.getRange();
			range.setValue( count );
			
			List<FL_Frequency> typeFreqs = (List<FL_Frequency>) ClusterHelper.getFirstProperty(cluster, "type-dist").getValue();
			
			// update the type distribution range property
			typeFreqs.clear();
			for (String key : typeSummary.keySet()) {
				typeFreqs.add( new FL_Frequency(key, typeSummary.get(key)) );
			}
			
			List<FL_Frequency> locFreqs = (List<FL_Frequency>) ClusterHelper.getFirstProperty(cluster, "location-dist").getValue();
			
			// update the location distribution range property
			locFreqs.clear();
			for (String key : locationSummary.keySet()) {
				FL_GeoData geo = new FL_GeoData(null, null, null, key);
				try {
					geoCoder.geocode(Collections.singletonList(geo));
				} catch (AvroRemoteException e) { /* ignore */ }
				locFreqs.add( new FL_Frequency(geo, locationSummary.get(key)) );
			}
		}
	}
}
