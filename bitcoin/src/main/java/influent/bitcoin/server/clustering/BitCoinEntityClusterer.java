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
package influent.bitcoin.server.clustering;

import influent.entity.clustering.GeneralEntityClusterer;
import influent.idl.FL_Geocoding;
import influent.midtier.IdGenerator;

import java.util.LinkedList;
import java.util.List;

import com.oculusinfo.ml.DataSet;
import com.oculusinfo.ml.distance.semantic.EditDistance;
import com.oculusinfo.ml.distance.spatial.HaversineDistance;
import com.oculusinfo.ml.distance.vector.EuclideanDistance;
import com.oculusinfo.ml.feature.centroid.BagOfWordsCentroid;
import com.oculusinfo.ml.feature.centroid.FastGeoSpatialCentroid;
import com.oculusinfo.ml.feature.centroid.MeanNumericVectorCentroid;
import com.oculusinfo.ml.unsupervised.Clusterer;
import com.oculusinfo.ml.unsupervised.ThresholdClusterer;

public class BitCoinEntityClusterer extends GeneralEntityClusterer {
	
	private static List<Clusterer> createClusterStages() {
		List<Clusterer> clusterStages = new LinkedList<Clusterer>();
		
		//
		// STAGE 1: cluster by highly similar name + amount
		//
		ThresholdClusterer labelClusterer = new ThresholdClusterer(false); 
		labelClusterer.registerFeatureType(
				"tokens",
				"tokens", 
				BagOfWordsCentroid.class, 
				new EditDistance(1.0));
		labelClusterer.registerFeatureType(
				"NormAvgTrasactionAmount",
				"NormAvgTrasactionAmount", 
				MeanNumericVectorCentroid.class, 
				new EuclideanDistance(0));
		labelClusterer.registerFeatureType(
				"AvgTrasactionAmount",
				"AvgTrasactionAmount", 
				MeanNumericVectorCentroid.class, 
				new EuclideanDistance(0));
		labelClusterer.registerFeatureType(
				"NormCountOfTransactions",
				"NormCountOfTransactions", 
				MeanNumericVectorCentroid.class, 
				new EuclideanDistance(0));
		labelClusterer.registerFeatureType(
				"CountOfTransactions",
				"CountOfTransactions", 
				MeanNumericVectorCentroid.class, 
				new EuclideanDistance(0));
		labelClusterer.registerFeatureType(
				"NormDegree",
				"NormDegree", 
				MeanNumericVectorCentroid.class, 
				new EuclideanDistance(0));
		labelClusterer.registerFeatureType(
				"Degree",
				"Degree", 
				MeanNumericVectorCentroid.class, 
				new EuclideanDistance(0));
		labelClusterer.registerFeatureType(
				"location",
				"location", 
				FastGeoSpatialCentroid.class, 
				new HaversineDistance(0));
		labelClusterer.setThreshold(0.2);
		clusterStages.add( labelClusterer );
		
		// 
		// STAGE 2-4: cluster by amount, # transactions, degree
		//
//		KMeans amountClusterer = new KMeans(false, 5, 10);
		for (int i=0; i < 6; i++) {
			ThresholdClusterer amountClusterer = new ThresholdClusterer(false);
			amountClusterer.registerFeatureType(
						"tokens",
						"tokens", 
						BagOfWordsCentroid.class, 
						new EditDistance(0));
			amountClusterer.registerFeatureType(
						"NormAvgTrasactionAmount",
						"NormAvgTrasactionAmount", 
						MeanNumericVectorCentroid.class, 
						new EuclideanDistance(0.34));
			amountClusterer.registerFeatureType(
						"AvgTrasactionAmount",
						"AvgTrasactionAmount", 
						MeanNumericVectorCentroid.class, 
						new EuclideanDistance(0));
			amountClusterer.registerFeatureType(
						"NormCountOfTransactions",
						"NormCountOfTransactions", 
						MeanNumericVectorCentroid.class, 
						new EuclideanDistance(0.33));
			amountClusterer.registerFeatureType(
						"CountOfTransactions",
						"CountOfTransactions", 
						MeanNumericVectorCentroid.class, 
						new EuclideanDistance(0));
			amountClusterer.registerFeatureType(
						"NormDegree",
						"NormDegree", 
						MeanNumericVectorCentroid.class, 
						new EuclideanDistance(0.33));
			amountClusterer.registerFeatureType(
						"Degree",
						"Degree", 
						MeanNumericVectorCentroid.class, 
						new EuclideanDistance(0));
			amountClusterer.registerFeatureType(
						"location",
						"location", 
						FastGeoSpatialCentroid.class, 
						new HaversineDistance(0));
			amountClusterer.setThreshold(0.1 + 0.1 * (double)i);
			clusterStages.add( amountClusterer );
		}
				
		return clusterStages;
	}
	
	@Override
	public void init(Object[] args)  throws IllegalArgumentException {
		try {
			IdGenerator idGen = (IdGenerator)args[0];
			FL_Geocoding geocoder = (FL_Geocoding)args[1];
			this.instanceFactory = new BitCoinEntityInstanceFactory(idGen, geocoder);
			super.init( instanceFactory,
					    createClusterStages() );
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Invalid initialization parameters. Parameters must be init(IdGenerator idGenerator)", e);
		}
	}
	
	
	@Override
	protected void stageInit(int stage, int interation, Clusterer clusterer, DataSet ds) {
		ds.normalizeInstanceFeature("NormAvgTrasactionAmount", "NormAvgTrasactionAmount");
		ds.normalizeInstanceFeature("NormCountOfTransactions", "NormCountOfTransactions");
		ds.normalizeInstanceFeature("NormDegree", "NormDegree");
	}
}
