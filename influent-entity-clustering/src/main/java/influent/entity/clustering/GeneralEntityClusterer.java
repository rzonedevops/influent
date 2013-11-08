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

import influent.idl.FL_Entity;
import influent.idlhelper.FLEntityObject;
import influent.midtier.api.ClusterResults;
import influent.midtier.api.EntityClusterer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oculusinfo.ml.DataSet;
import com.oculusinfo.ml.unsupervised.Cluster;
import com.oculusinfo.ml.unsupervised.ClusterResult;
import com.oculusinfo.ml.unsupervised.Clusterer;

public class GeneralEntityClusterer implements EntityClusterer {	
	protected static final int PARTITION_SIZE = 10000;
	protected static final int MAX_ITERATIONS = 3;
	protected List<Clusterer> clusterStages;
	protected EntityInstanceFactory instanceFactory;
	
	protected static final Logger s_logger = LoggerFactory.getLogger(GeneralEntityClusterer.class);
	
	
	@Override
	public void init(Object[] args) throws IllegalArgumentException {
		 try {
			 init((EntityInstanceFactory) instanceFactory, (List<Clusterer>) clusterStages);
		 }
		 catch (Exception e) {
			 throw new IllegalArgumentException("Invalid initialization parameters. Parameters must be init(EntityInstanceFactory instanceFactory, List<Clusterer> clusterStages", e);
		 }
	}
	
	protected void init(EntityInstanceFactory instanceFactory, List<Clusterer> clusterStages) {
		this.instanceFactory = instanceFactory;
		this.clusterStages = clusterStages;
	}
	
	protected Map<String, FLEntityObject> getNonClusters(Map<String, FLEntityObject> index) {
		Map<String, FLEntityObject> clusters = new HashMap<String, FLEntityObject>();
		for (String id : index.keySet()) {
			FLEntityObject e = index.get(id);
			if (e.isCluster()) {
				clusters.put(e.getUid(), e);
			}
		}
		return clusters;
	}
	
	protected List<List<FLEntityObject>> partitionDataSet(List<FLEntityObject> entities, int partitionSize) {
		List<List<FLEntityObject>> partitions = new LinkedList<List<FLEntityObject>>();
		int sIdx = 0;
		int eIdx = 0;
		while (eIdx < entities.size()) {
			eIdx = (int)Math.min(sIdx+partitionSize, entities.size());
			partitions.add( entities.subList(sIdx, eIdx) );
			sIdx = eIdx;
		}
		return partitions;
	}
	
	protected void stageInit(int stage, int interation, Clusterer clusterer, DataSet ds) {
		// Nothing special to do
	}
	
	@Override
	public ClusterResults clusterEntities(List<FL_Entity> entities) {
		// create an index of entities
		Map<String, FLEntityObject> index = createEntityIndex(entities);
		
		List<List<FLEntityObject>> partitions = partitionDataSet(new LinkedList<FLEntityObject>(index.values()), PARTITION_SIZE);
		List<FLEntityObject> stageResults = new LinkedList<FLEntityObject>(); 
		
		s_logger.info("Initiating entity clustering for {} entities", entities.size());
		
		int stage = 1;
		
		// Process cluster stages
		for (Clusterer clusterer : clusterStages) {
			s_logger.info("Beginning stage {} clustering", stage);
			
			// initialize the stage clusterer
			clusterer.init();
			
 			int numClusters = -1;
 			int iteration = 0;
			while (numClusters != stageResults.size() && iteration < MAX_ITERATIONS) {
				s_logger.info("Beginning iteration {}", (iteration+1));
				// keep track of the previous iteration numClusters generated
				numClusters = stageResults.size();
				
				// initialize the stage results
				stageResults = new LinkedList<FLEntityObject>();
				
				for (List<FLEntityObject> items : partitions) {
					// Generate a dataset for the clusterer to process
					DataSet ds = EntityDataSetFactory.createDataSetfromEntities(items, instanceFactory);
		
					// hook for special initialization of stage
					stageInit(stage, iteration, clusterer, ds);
					
					// cluster the entities
					ClusterResult result = clusterer.doCluster(ds);
					
					List<Cluster> clusters = new LinkedList<Cluster>();
					for (Cluster cluster : result) {
						clusters.add(cluster);
					}
			
					// convert cluster to entity clusters
					List<FLEntityObject> clusterItems = instanceFactory.toEntity(clusters, index);
		
					// add the resulting clusters to the stageResults
					stageResults.addAll(clusterItems);
				
					// add the generated entity clusters to the entity index
					updateEntityIndex(clusterItems, index);
				}
				// randomly shuffle the stage results to avoid re clustering the same partition of items each iteration
//				Collections.shuffle(stageResults);
				
				// partition the stage results for the next stage
				partitions = partitionDataSet(stageResults, PARTITION_SIZE);
				
				// increment the iteration number
				iteration++;
			}
			s_logger.info("Stage {} completed with {} clusters", stage, stageResults.size());
			
			// terminate the stage clusterer
			clusterer.terminate();
			
			// increment the stage number
			stage++;
		}
		
		s_logger.info("Entity clustering generated {} clusters top level clusters", stageResults.size());
		
		List<FLEntityObject> rootClusters = stageResults;
//		List<FL_Cluster> rootClusters = new LinkedList<FL_Cluster>();
//		for (FLEntityObject e : stageResults) {
//			if (e.isCluster()) {  // HACK we should return root entities too otherwise they are lost!
//				rootClusters.add(e.cluster);
//			}
//		}
		
		return new ClusterResults(rootClusters, getNonClusters(index));
	}

	protected Map<String, FLEntityObject> createEntityIndex(List<FL_Entity> entities) {
		Map<String, FLEntityObject> index = new HashMap<String, FLEntityObject>();
		
		for (FL_Entity entity : entities) {
			FLEntityObject obj = new FLEntityObject(entity);
			index.put(obj.getUid(), obj);
		}
		return index;
	}
	
	protected void updateEntityIndex(List<FLEntityObject> entities, Map<String, FLEntityObject> index) {
		for (FLEntityObject entity : entities) {
			index.put(entity.getUid(), entity);
		}
	}
}
