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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.AvroRemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oculusinfo.ml.Instance;
import com.oculusinfo.ml.distance.semantic.EditDistance;
import com.oculusinfo.ml.feature.BagOfWordsFeature;
import com.oculusinfo.ml.feature.Feature;
import com.oculusinfo.ml.feature.GeoSpatialFeature;
import com.oculusinfo.ml.feature.NumericVectorFeature;
import com.oculusinfo.ml.feature.centroid.Centroid;
import com.oculusinfo.ml.unsupervised.Cluster;

public class GeneralEntityInstanceFactory extends EntityInstanceFactory {
	
	private static Logger s_logger = LoggerFactory.getLogger(GeneralEntityInstanceFactory.class);
	
	private FL_Geocoding geocoder;
	private IdGenerator idGenerator;
	
	public GeneralEntityInstanceFactory(IdGenerator idGen, FL_Geocoding geocoding) {
		this.idGenerator = idGen;
		this.geocoder = geocoding;
	}
	
	private static Set<String> getStopWords() {
		Set<String> stopwords = new HashSet<String>();
		
		try {
			// load property file
			Properties props = new Properties();
			InputStream in = GeneralEntityInstanceFactory.class.getResourceAsStream("/clusterer.properties");
			props.load(in);
			
			// retrieve the stopwords property
			String stopwordsProp = props.getProperty("entity.clusterer.stopwords");
			
			// close the property file
			in.close();
			
			for (String word : stopwordsProp.split(",")) {
				stopwords.add(word.toLowerCase());
			}
			
		} catch (Exception e) {
			s_logger.error("", e);
		}
		
		return stopwords;
	}
	
	private static final Set<String> stopwords = getStopWords(); 
	
	protected List<String> tokenizeString(String label) {
		List<String> candidates = new LinkedList<String>();

		String cleaned = label.replaceAll("[.,!@#$%^&*()-_=+}{;:'\"?/<>\\[\\]\\\\]", " ");

		Pattern regex = Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}");
		
		//String[] tokens = regex.split(label); //label.split(" ");
		Matcher lexer = regex.matcher(cleaned);
		
		while (lexer.find()) {
			String token = lexer.group().toLowerCase();
			if (stopwords.contains(token) == false) candidates.add(token);
		}

	    return candidates;
	}
	
	@Override
	public Instance toInstance(FLEntityObject entity) {
		Instance inst = null;
		
		if (entity.isEntity()) {
			inst = toInstance(entity.entity); 
		}
		else {
			inst = toInstance(entity.cluster);
		}
		return inst;
	}
	
	protected Instance toInstance(FL_Entity entity) {
		Instance inst = new Instance( entity.getUid() );
		
		PropertyHelper labelProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.LABEL);
					
		if (labelProp != null) {
			String label = (String)labelProp.getValue();
			// normalized tokenized string feature
			BagOfWordsFeature tokens = new BagOfWordsFeature("tokens", "tokens");
			List<String> tokenStrs = tokenizeString( label.toLowerCase() );
			for (String token : tokenStrs) {
				tokens.incrementValue(token);
			}
			inst.addFeature(tokens);
		}
		
		PropertyHelper geoProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.GEO);
			
		if (geoProp != null) {
			final List<Object> geoVals = geoProp.getValues();
			if (!geoVals.isEmpty()) {
				// taking first only. TODO: what to do if there are more than one?
				FL_GeoData geoVal = (FL_GeoData)geoVals.get(0);
				GeoSpatialFeature geo = new GeoSpatialFeature("location", "location");
				double lat = geoVal.getLat();
				double lon = geoVal.getLon();
				geo.setValue(lat, lon);
				inst.addFeature(geo);
			}
		}
		
		PropertyHelper typeProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.TYPE);
		
		if (typeProp != null) {
			String type = (String)typeProp.getValue();
			// entity type feature
			BagOfWordsFeature entityType = new BagOfWordsFeature("entityType", "entityType");
		
			entityType.incrementValue( type );
			inst.addFeature(entityType);
		}
		return inst;
	}

	protected Instance toInstance(FL_Cluster entity) {
		Instance inst = new Instance( entity.getUid() );
		
		PropertyHelper labelProp = ClusterHelper.getFirstPropertyByTag(entity, FL_PropertyTag.LABEL);
					
		if (labelProp != null) {
			String label = (String)labelProp.getValue();
			// normalized tokenized string feature
			BagOfWordsFeature tokens = new BagOfWordsFeature("tokens", "tokens");
			List<String> tokenStrs = tokenizeString( label.toLowerCase() );
			for (String token : tokenStrs) {
				tokens.incrementValue(token);
			}
			inst.addFeature(tokens);
		}
		
		PropertyHelper geoProp = ClusterHelper.getFirstProperty(entity, "latlon");
			
		if (geoProp != null) {
			FL_GeoData geoVal = (FL_GeoData)geoProp.getValue();
			GeoSpatialFeature geo = new GeoSpatialFeature("location", "location");
			double lat = geoVal.getLat();
			double lon = geoVal.getLon();
			geo.setValue(lat, lon);
			inst.addFeature(geo);
		}
		
		PropertyHelper typeProp = ClusterHelper.getFirstPropertyByTag(entity, FL_PropertyTag.TYPE);
		
		if (typeProp != null) {
			String type = (String)typeProp.getValue();
			// entity type feature
			BagOfWordsFeature entityType = new BagOfWordsFeature("entityType", "entityType");
		
			entityType.incrementValue( type );
			inst.addFeature(entityType);
		}
		return inst;
	}

	private String getLabel(FLEntityObject entity) {
		String label = null;
		
		if (entity.isEntity()) {
			PropertyHelper labelProp = EntityHelper.getFirstPropertyByTag(entity.entity, FL_PropertyTag.LABEL);
			if (labelProp != null) {
				label = (String)labelProp.getValue();
			}
		}
		else {
			PropertyHelper labelProp = ClusterHelper.getFirstPropertyByTag(entity.cluster, FL_PropertyTag.LABEL);
			if (labelProp != null) {
				label = (String)labelProp.getValue();
			}
		}
		return label;
	}
	
	private String getMostRepresentativeClusterLabel(Cluster cluster, Map<String, FLEntityObject> entityIndex) {
		double min = Double.MAX_VALUE;	
		String bestLabel = "Anonymous";
	
		for (Instance childA : cluster.getMembers()) {
			double sum = 0;
			
			String labelA = getLabel(entityIndex.get(childA.getId()));
			for (Instance childB : cluster.getMembers()) {
				if (childA == childB) continue;
				String labelB = getLabel(entityIndex.get(childB.getId()));
				if (labelA == null || labelB == null) continue;
				sum += EditDistance.getNormLevenshteinDistance(labelA.toLowerCase(), labelB.toLowerCase());
			}
			if (sum < min) {
				min = sum;
				bestLabel = labelA;
			}
		}
		
		return bestLabel;
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
		if (stats == null) {
			System.out.println();
		}
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
	
	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public FLEntityObject toEntity(Instance instance, Map<String, FLEntityObject> entityIndex) {
		if ((instance instanceof Cluster) == false) return entityIndex.get(instance.getId());
		
		Cluster cluster = (Cluster)instance;
		
		// if the cluster is only size of one then return child directly rather than creating an entitycluster
		if (cluster.size() == 1) {
			Instance childInst = cluster.getMembers().iterator().next(); 
			FLEntityObject child = toEntity(childInst, entityIndex);
			
			// if this is a child cluster make a copy and update the parent and root ids 
			if (child.isCluster()) {
				FL_Cluster childCluster = child.cluster;
				// copy the previous cluster but revise the root and parent
				childCluster.setParent(null);
				childCluster.setRoot(""); // TODO not sure why this is needed
			}
			return child;
		}
		
		// create an id for this new entity cluster
		String clusterId = idGenerator.nextId();
		
		// update children clusters and keep track of children ids
		Set<String> childEntityIds = new LinkedHashSet<String>();
		Set<String> childClusterIds = new LinkedHashSet<String>();
		
		for (Instance inst : cluster.getMembers()) {	
			FLEntityObject child = entityIndex.get(inst.getId());
			
			// if this is a cluster then update the root and parent
			if (child.isCluster()) {
				FL_Cluster childCluster = child.cluster;
				// copy the previous cluster but revise the root and parent
				childCluster.setParent(clusterId);
				childCluster.setRoot(""); // TODO not sure why this is needed
				
				childClusterIds.add(inst.getId());
			}
			else {
				childEntityIds.add(inst.getId());
			}
		}
		
		// create the cluster entity properties
		List<FL_Property> properties = new LinkedList<FL_Property>();
		
		Map<String, String> countryNames = new HashMap<String, String>();
		Map<String, Double> locationSummary = new HashMap<String, Double>();
		Map<String, Double> typeSummary = new HashMap<String, Double>();
		
		Map<String, Centroid> centroids = cluster.getCentroids();
		
		
		List<String> tempCCs = new ArrayList<String>();
		
		// Add the total decendents count
		long decendentCount = 0;
		double avgConfidence = 0;
		for (Instance inst : cluster.getMembers()) {
			FLEntityObject child = entityIndex.get(inst.getId());
			if (child.isCluster()) {
				FL_Cluster childCluster = child.cluster;
				final long count = (Long)ClusterHelper.getFirstProperty(childCluster, "count").getValue(); 
				decendentCount += count;
				increment( (List<FL_Frequency>)ClusterHelper.getFirstProperty(childCluster, "type-dist").getValue(), typeSummary );
				increment( (List<FL_Frequency>)ClusterHelper.getFirstProperty(childCluster, "location-dist").getValue(), locationSummary );
				
				
				final FL_Uncertainty pConfidence = childCluster.getUncertainty();
				final double confidence = pConfidence != null? pConfidence.getConfidence() : 1.0;
				
				avgConfidence += count * confidence;
			}
			else {
				FL_Entity childEntity = child.entity;
				decendentCount++;
				PropertyHelper typeProp = EntityHelper.getFirstPropertyByTag(childEntity, FL_PropertyTag.TYPE);
				increment((String)typeProp.getValue(), 1, typeSummary);
				
				PropertyHelper geoProp = EntityHelper.getFirstPropertyByTag(childEntity, FL_PropertyTag.GEO);
				if (geoProp != null) {
					final List<Object> geoVals = geoProp.getValues();
							
					for (Object obj : geoVals) {
						final FL_GeoData geoData = (FL_GeoData) obj;
						final String cc = geoData.getCc();
						if (cc != null && !cc.isEmpty() && !tempCCs.contains(cc)) {
							tempCCs.add(cc); // 1 increment means we have it at least once.
							countryNames.put(cc, geoData.getText());
							increment(cc, 1, locationSummary);
						}
					}
					tempCCs.clear();
				}
				
				final FL_Uncertainty pConfidence = childEntity.getUncertainty();
				final double confidence = pConfidence != null? pConfidence.getConfidence() : 1.0;
				
				avgConfidence += confidence;
			}
		}
		
		// Add the lat/lon - used in clustering
		Centroid<? extends Feature> location = centroids.get("location");
		Collection<GeoSpatialFeature> lfeatures = (Collection<GeoSpatialFeature>)location.getCentroid();
		for (GeoSpatialFeature feature : lfeatures) {
			FL_GeoData geo = new FL_GeoData(null, feature.getLatitude(), feature.getLongitude(), null);
			FL_Property p = new PropertyHelper("latlon", "Location", geo, Collections.singletonList(FL_PropertyTag.GEO));
			properties.add(p);
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
		
		// compute the most representative label for the cluster and set as name
		String label = getMostRepresentativeClusterLabel(cluster, entityIndex);
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
		
//		p = new PropertyHelper(
//				"type-dist",
//				"type-dist",
//				typeSummary,
//				FL_PropertyType.OTHER,
//				FL_PropertyTag.STAT);
//		properties.add(p);
		
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
		
//		p = new PropertyHelper(
//				"location-dist",
//				"location-dist",
//				locationSummary,
//				FL_PropertyType.OTHER,
//				FL_PropertyTag.STAT);
//		properties.add(p);

		// TODO this isn't the correct way to handle confidence
		p = new PropertyHelper(
				"confidence",
				"confidence",
				avgConfidence,
				FL_PropertyType.DOUBLE,
				FL_PropertyTag.STAT);
		properties.add(p);
		
		// lastly add in any property that do not require special handling
		for (String prop : centroids.keySet()) {
			if ( (prop.equalsIgnoreCase("location") == false) &&
				 (prop.equalsIgnoreCase("tokens") == false) &&
				 (prop.equalsIgnoreCase("entityType") == false) ) {
				Centroid centroid = centroids.get(prop);
				if (centroid.getType() == NumericVectorFeature.class) {
					NumericVectorFeature v = (NumericVectorFeature)centroid.getCentroid().iterator().next();
					p = new PropertyHelper(
							prop,
							prop,
							v.getValue()[0],
							FL_PropertyType.DOUBLE,
							FL_PropertyTag.STAT);
					properties.add(p);
				}
			}
		}
		
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
		return new FLEntityObject(ch);
	}
}
