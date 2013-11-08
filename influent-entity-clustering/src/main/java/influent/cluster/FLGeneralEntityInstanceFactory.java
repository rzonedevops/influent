//package influent.cluster;
//
//import influent.idl.FL_Cluster;
//import influent.idl.FL_Entity;
//import influent.idl.FL_EntityTag;
//import influent.idl.FL_GeoData;
//import influent.idl.FL_Property;
//import influent.idl.FL_PropertyTag;
//import influent.idl.FL_PropertyType;
//import influent.idl.helper.EntityHelper;
//import influent.idl.helper.PropertyHelper;
//
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedHashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Set;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import com.oculusinfo.ml.Instance;
//import com.oculusinfo.ml.distance.semantic.EditDistance;
//import com.oculusinfo.ml.feature.BagOfWordsFeature;
//import com.oculusinfo.ml.feature.Feature;
//import com.oculusinfo.ml.feature.GeoSpatialFeature;
//import com.oculusinfo.ml.feature.centroid.Centroid;
//import com.oculusinfo.ml.unsupervised.Cluster;
//import influent.entity.clustering.EntityInstanceFactory;
//
//
//public class FLGeneralEntityInstanceFactory extends FLEntityInstanceFactory {
//	
//	
//	public FLGeneralEntityInstanceFactory() {
//	
//	}
//	
//	private static Set<String> getStopWords() {
//		Set<String> stopwords = new HashSet<String>();
//		
//		try {
//			// load property file
//			Properties props = new Properties();
//			InputStream in = FLGeneralEntityInstanceFactory.class.getResourceAsStream("/clusterer.properties");
//			props.load(in);
//			
//			// retrieve the stopwords property
//			String stopwordsProp = props.getProperty("entity.clusterer.stopwords");
//			
//			// close the property file
//			in.close();
//			
//			for (String word : stopwordsProp.split(",")) {
//				stopwords.add(word.toLowerCase());
//			}
//			
//		} catch (Exception e) {
//			System.err.println(e.getLocalizedMessage());
//			e.printStackTrace();
//		}
//		
//		return stopwords;
//	}
//	
//	private static final Set<String> stopwords = getStopWords(); 
//	
//	private List<String> tokenizeString(String label) {
//		List<String> candidates = new LinkedList<String>();
//
//		String cleaned = label.replaceAll("[.,!@#$%^&*()-_=+}{;:'\"?/<>\\[\\]\\\\]", " ");
//
//		Pattern regex = Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}");
//		
//		//String[] tokens = regex.split(label); //label.split(" ");
//		Matcher lexer = regex.matcher(cleaned);
//		
//		while (lexer.find()) {
//			String token = lexer.group().toLowerCase();
//			if (stopwords.contains(token) == false) candidates.add(token);
//		}
//
//	    return candidates;
//	}
//	
//	
//	
//	@Override
//	public Instance toInstance(FL_Entity entity) {
//		Instance inst = new Instance( entity.getUid().toString() );
//		
//		String label = getLabel(entity);
//		
////		if (!label.equalsIgnoreCase("anonymous")) {			
//			// normalized tokenized string feature
//			BagOfWordsFeature tokens = new BagOfWordsFeature("tokens", "tokens");
//			List<String> tokenStrs = tokenizeString(label);
//			for (String token : tokenStrs) {
//				tokens.incrementValue(token);
//			}
//			inst.addFeature(tokens);
////		}
//		
//		try { /* geospatial feature */
//			GeoSpatialFeature geo = new GeoSpatialFeature("location", "location");
//			
//			FL_Property geoProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.GEO);
//			if (geoProp != null) {
//				FL_GeoData geoData = (FL_GeoData) geoProp.getValue();
//				
//				double lat = geoData.getLat();
//				double lon = geoData.getLon();
//				geo.setValue(lat, lon);
//				inst.addFeature(geo);
//			}
//		}
//		catch (Exception e) { /* ignore */ }
//		
//		
//		// entity type feature
//		BagOfWordsFeature entityType = new BagOfWordsFeature("entityType", "entityType");
//		
//		FL_Property typeProp = EntityHelper.getFirstProperty(entity, "type");
//		
//		entityType.incrementValue( typeProp.getValue().toString() );
//		inst.addFeature(entityType);
//		
//		return inst;
//	}
//	
//	private String getLabel(FL_Entity entity) {
//		FL_Property labelProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.LABEL);
//		return labelProp.getValue().toString();
//	}
//	
//	private String getMostRepresentativeClusterLabel(Cluster cluster, Map<String, FL_Entity> entityIndex) {
//		double min = Double.MAX_VALUE;	
//		String bestLabel = "Anonymous";
//	
//		for (Instance childA : cluster.getMembers()) {
//			double sum = 0;
//			
//			String labelA = getLabel(entityIndex.get(childA.getId()));
//			for (Instance childB : cluster.getMembers()) {
//				if (childA == childB) continue;
//				String labelB = getLabel(entityIndex.get(childB.getId()));
//				sum += EditDistance.getNormLevenshteinDistance(labelA.toLowerCase(), labelB.toLowerCase());
//			}
//			if (sum < min) {
//				min = sum;
//				bestLabel = labelA;
//			}
//		}
//		
//		return bestLabel;
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
//	
//	@SuppressWarnings({"rawtypes", "unchecked"})
//	private List<FL_Property> createEntityClusterProperties(Cluster cluster, Map<String, Object> entityIndex) {
//		List<FL_Property> properties = new ArrayList<FL_Property>();
//		
//		Map<String, Integer> locationSummary = new HashMap<String, Integer>();
//		Map<String, Integer> typeSummary = new HashMap<String, Integer>();
//		
//		Map<String, Centroid> centroids = cluster.getCentroids();
//		
//		// Add the lat/lon
//		Centroid<? extends Feature> location = centroids.get("location");
//		Collection<GeoSpatialFeature> lfeatures = (Collection<GeoSpatialFeature>)location.getCentroid();
//		for (GeoSpatialFeature feature : lfeatures) {
//			
//			FL_GeoData geoData = FL_GeoData.newBuilder().setCc(null).setText(feature.getName())
//				.setLat(feature.getLatitude())
//				.setLon(feature.getLongitude()).build();
//			
//			FL_Property geoProp = new PropertyHelper("geo","Geographic",geoData,FL_PropertyType.GEO, Collections.singletonList(FL_PropertyTag.GEO));
//			properties.add(geoProp);
//		}
//		
//		// Add the total decendents count
//		int decendentCount = 0;
//		double avgConfidence = 0;
//		for (Instance inst : cluster.getMembers()) {
//			Object child = entityIndex.get(inst.getId());
//			if (child instanceof FL_Cluster) {
//				final int count = (Integer)child.getFirstProperty("count").getValue();
//				decendentCount += count;
//				increment( (Map<String, Integer>)child.getFirstProperty("type-dist").getValue(), typeSummary );
//				increment( (Map<String, Integer>)child.getFirstProperty("location-dist").getValue(), locationSummary );
//				
//				final FL_Property pConfidence = child.getFirstProperty(PropertyTag.CONFIDENCE.name());
//				final double confidence = pConfidence != null? (Double)pConfidence.getValue() : 1.0;
//				
//				avgConfidence += count * confidence;
//			}
//			else {
//				FL_Entity echild = (FL_Entity)child;
//				decendentCount++;
//				increment(echild.getType(), 1, typeSummary);
//				
//				FL_Property locations = child.getProperties(FL_PropertyTag.GEO.toString());
//				
//				for (Property l : locations) {
//					String cc = "";
//					if (l.getValue() instanceof ArrayList) {
//						ArrayList<String> codes = (ArrayList<String>)l.getValue();
//						if (codes.size() > 0) cc = codes.get(0);  // For now we pick the first CC for the partner
//					}
//					else {
//						cc = (String)l.getValue();
//					}
//					increment(cc, 1, locationSummary);
//				}
//				
//				final Property pConfidence = child.getFirstProperty(PropertyTag.CONFIDENCE.name());
//				final double confidence = pConfidence != null? (Double)pConfidence.getValue() : 1.0;
//				
//				avgConfidence += confidence;
//			}
//		}
//		
//		avgConfidence /= decendentCount;
//		
//		Property p = new Property(Collections.singletonList(PropertyTag.STAT), "count", PropertyType.INTEGER, decendentCount);
//		properties.add(p);
//		
//		// compute the most representative label for the cluster and set as name
//		String label = getMostRepresentativeClusterLabel(cluster, entityIndex);
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
//		p = new Property(Collections.singletonList(PropertyTag.CONFIDENCE), PropertyTag.CONFIDENCE.name(), PropertyType.REAL, avgConfidence);
//		properties.add(p);
//		
//		
//		return properties;
//	}
//	
//	@Override
//	public FL_Entity toEntity(Cluster cluster, Map<String, FL_Entity> entityIndex) {	
//		// if the cluster is only size of one then return child directly rather than creating an entitycluster
//		if (cluster.size() == 1) {
//			Instance childInst = cluster.getMembers().iterator().next(); 
//			FL_Entity childEntity = toEntity(childInst, entityIndex);
//			
//			// if this is a child cluster make a copy and update the parent and root ids 
//			if (childEntity instanceof EntityCluster) {
//				
//				FL_Cluster childCluster = null;
//				// copy the previous cluster but revise the root and parent
//				childEntity = new EntityClusterImpl(childCluster.getId(), 
//													childCluster.getProvenance(), 
//													childCluster.getAllProperties(), 
//													childCluster.getTag(),
//													childCluster.getMemberIds(),
//													"",
//													"");
//			}
//			return childEntity;
//		}
//		
//		// create an id for this new entity cluster
//		String clusterId = idGenerator.nextId();
//		
//		// update children clusters and keep track of children ids
//		Set<String> childIds = new LinkedHashSet<String>();
//		for (Instance inst : cluster.getMembers()) {
//			childIds.add(inst.getId());
//	
//			FL_Entity childEntity = entityIndex.get(inst.getId());
//			
//			// if this is a cluster then update the root and parent
//			if (childEntity instanceof EntityCluster) {
//				EntityCluster childCluster = (EntityCluster)childEntity;
//				// copy the previous cluster but revise the root and parent
//				entityIndex.put(childCluster.getId(), new EntityClusterImpl(childCluster.getId(), 
//																			childCluster.getProvenance(), 
//																			childCluster.getAllProperties(), 
//																			childCluster.getTag(),
//																			childCluster.getMemberIds(),
//																			"",
//																			clusterId));
//			}
//		}
//		
//		// create the cluster entity properties
//		List<FL_Property> properties = createEntityClusterProperties(cluster, entityIndex);
//		
//		List<CharSequence> memberIds = new ArrayList<CharSequence>();
//		for (String cid : childIds) {
//			memberIds.add(cid);
//		}
//		
//		return FL_Cluster.newBuilder().setUid(clusterId).setProvenance(null)
//				.setProperties(properties).setMembers(memberIds)
//				.setParent("").setRoot("").setLevel(0)
//				.setTags(Collections.singletonList(FL_EntityTag.CLUSTER))
//				.setUncertainty(null).build();
//		
//		
//	}
//
//	@Override
//	public FL_Entity toEntity(Instance inst, Map<String, FL_Entity> entityIndex) {
//		return entityIndex.get(inst.getId());
//	}
//}
