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
package influent.entity.clustering.utils;

import influent.idl.FL_Cluster;
import influent.idl.FL_DistributionRange;
import influent.idl.FL_Entity;
import influent.idl.FL_EntityTag;
import influent.idl.FL_Frequency;
import influent.idl.FL_GeoData;
import influent.idl.FL_Geocoding;
import influent.idl.FL_Property;
import influent.idl.FL_PropertyTag;
import influent.idl.FL_PropertyType;
import influent.idl.FL_RangeType;
import influent.idl.FL_Uncertainty;
import influent.idlhelper.ClusterHelper;
import influent.idlhelper.EntityHelper;
import influent.idlhelper.FLEntityObject;
import influent.idlhelper.PropertyHelper;
import influent.midtier.IdGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.avro.AvroRemoteException;

public class EntityClusterFactory {
	
	protected FL_Geocoding geocoder;
	protected IdGenerator idGenerator;
	
	public EntityClusterFactory(IdGenerator idGen, FL_Geocoding geocoding) {
		this.idGenerator = idGen;
		this.geocoder = geocoding;
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
	
	protected String getRepresentativeLabel(List<FL_Entity> entities, List<FL_Cluster> clusters) {
		// if we have entities pick the first label we find
		if (entities != null) {
			for (FL_Entity entity : entities) {
				PropertyHelper labelProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.LABEL);
				if (labelProp != null) {
					return (String)labelProp.getValue();
				}
			}
		}
		else if (clusters != null) { // otherwise, if we have clusters then pick the first label we find
			for (FL_Cluster cluster : clusters) {
				PropertyHelper labelProp = ClusterHelper.getFirstPropertyByTag(cluster, FL_PropertyTag.LABEL);
				if (labelProp != null) {
					return (String)labelProp.getValue();
				}
			}
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public FL_Cluster toCluster(List<FL_Entity> entities, List<FL_Cluster> subClusters) {
		List<String> childEntityIds = new LinkedList<String>();
		List<String> childClusterIds = new LinkedList<String>();
		
		// create the cluster entity properties
		List<FL_Property> properties = new LinkedList<FL_Property>();
				
		// initialize decendentCount to number of entities - if there are any
		long decendentCount = entities == null ? 0 : entities.size();
		double avgConfidence = 0;
		
		Map<String, Double> locationSummary = new HashMap<String, Double>();
		Map<String, Double> typeSummary = new HashMap<String, Double>();
		
		// first process the entities
		if (entities != null) {
			for (FL_Entity entity : entities) {
				childEntityIds.add(entity.getUid());
				PropertyHelper typeProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.TYPE);
				if (typeProp != null) {
					increment((String)typeProp.getValue(), 1, typeSummary);
				}
				PropertyHelper geoProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.GEO);
				if (geoProp != null) {
					FL_GeoData geoData = (FL_GeoData)geoProp.getValue(); 
					String cc = geoData.getCc();
					if (cc != null && !cc.isEmpty()) {
						increment(cc, 1, locationSummary);
					}
				}
				FL_Uncertainty pConfidence = entity.getUncertainty();
				double confidence = pConfidence != null? pConfidence.getConfidence() : 1.0;
				
				avgConfidence += confidence;
			}
		}
		
		// next process the sub clusters
		if (subClusters != null) {
			for (FL_Cluster cluster : subClusters) {
				childClusterIds.add(cluster.getUid());
				long count = (Long)ClusterHelper.getFirstProperty(cluster, "count").getValue(); 
				decendentCount += count;
				increment( (List<FL_Frequency>)ClusterHelper.getFirstProperty(cluster, "type-dist").getValue(), typeSummary );
				increment( (List<FL_Frequency>)ClusterHelper.getFirstProperty(cluster, "location-dist").getValue(), locationSummary );
			
				FL_Uncertainty pConfidence = cluster.getUncertainty();
				double confidence = pConfidence != null? pConfidence.getConfidence() : 1.0;
			
				avgConfidence += count * confidence;
			}
		}
		
		if (decendentCount < 1) decendentCount = 1;
		
		avgConfidence /= decendentCount;
		
		FL_Property p = new PropertyHelper(
				"count",
				"count",
				decendentCount,
				FL_PropertyType.LONG,
				FL_PropertyTag.STAT);
		properties.add(p);
		
		// compute the representative label for the cluster and set as name
		String label = getRepresentativeLabel(entities, subClusters);
		p = new PropertyHelper(
				"name",
				"name",
				label,
				FL_PropertyType.STRING,
				FL_PropertyTag.TEXT);
		properties.add(p);
		
		p = new PropertyHelper(
				"label",
				"label",
				label,
				FL_PropertyType.STRING,
				FL_PropertyTag.LABEL);
		properties.add(p);
		
		p = new PropertyHelper(
				"type",
				"type",
				"entitycluster",
				FL_PropertyType.STRING,
				FL_PropertyTag.TYPE);
		properties.add(p);
		
		// create the type distribution range property
		List<FL_Frequency> typeFreqs = new ArrayList<FL_Frequency>();
		for (String key : typeSummary.keySet()) {
			typeFreqs.add( new FL_Frequency(key, typeSummary.get(key)) );
		}
		FL_DistributionRange range = new FL_DistributionRange(typeFreqs, FL_RangeType.DISTRIBUTION, FL_PropertyType.STRING, false);
		FL_Property dist = new FL_Property("type-dist", "type-dist", range, null, null, Collections.singletonList(FL_PropertyTag.TYPE));
		properties.add(dist);
		
		// create the location distribution range property
		List<FL_Frequency> locFreqs = new ArrayList<FL_Frequency>();
		for (String key : locationSummary.keySet()) {
			FL_GeoData geo = new FL_GeoData(null, null, null, key);
			try {
				geocoder.geocode(Collections.singletonList(geo));
			} catch (AvroRemoteException e) { /* ignore */ }
			locFreqs.add( new FL_Frequency(geo, locationSummary.get(key)) );
		}
		range = new FL_DistributionRange(locFreqs, FL_RangeType.DISTRIBUTION, FL_PropertyType.GEO, false);
		dist = new FL_Property("location-dist", "location-dist", range, null, null, Collections.singletonList(FL_PropertyTag.GEO));
		properties.add(dist);

		// add a confidence property
		p = new PropertyHelper(
				"confidence",
				"confidence",
				avgConfidence,
				FL_PropertyType.DOUBLE,
				FL_PropertyTag.STAT);
		properties.add(p);
		
		String clusterId = idGenerator.nextId();

		// create the entity cluster and add to results
		ClusterHelper ch = new ClusterHelper(clusterId, 
											 label,
											 "entitycluster",
											 FL_EntityTag.CLUSTER,
											 properties,
											 new LinkedList<String>(childEntityIds),
											 new LinkedList<String>(childClusterIds),
											 null,
											 null,
											 -1);
		ch.setUncertainty(FL_Uncertainty.newBuilder().setConfidence(avgConfidence).build());
		
		return ch;
	}

	public FL_Cluster toCluster(List<FLEntityObject> members) {
		List<FL_Entity> entities = new LinkedList<FL_Entity>();
		List<FL_Cluster> clusters = new LinkedList<FL_Cluster>();
		
		for (FLEntityObject m : members) {
			if (m.isCluster()) {
				clusters.add(m.cluster);
			}
			else {
				entities.add(m.entity);
			}
		}
		return toCluster(entities, clusters);
	}
}
