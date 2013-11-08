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

import com.oculusinfo.ml.distance.semantic.EditDistance;
import com.oculusinfo.ml.distance.semantic.ExactTokenMatchDistance;
import com.oculusinfo.ml.distance.spatial.HaversineDistance;
import com.oculusinfo.ml.feature.centroid.BagOfWordsCentroid;
import com.oculusinfo.ml.feature.centroid.FastGeoSpatialCentroid;
import com.oculusinfo.ml.unsupervised.Clusterer;
import com.oculusinfo.ml.unsupervised.ThresholdClusterer;

public class GeneralEntityClustererFactory {

	public static Clusterer createClusterer(double tokenWeight, 
											double typeWeight, 
											double geoWeight, 
											double threshold, 
											boolean retainInstances) {
		ThresholdClusterer clusterer = new ThresholdClusterer(retainInstances);
				
		// register mandatory features  
		clusterer.registerFeatureType(
				"tokens",
				"tokens", 
				BagOfWordsCentroid.class, 
				new EditDistance(tokenWeight));
		clusterer.registerFeatureType(
				"entityType",
				"entityType", 
				BagOfWordsCentroid.class, 
				new ExactTokenMatchDistance(typeWeight));
		
		// register optional features - not all entities have a location - treat as a further discriminating feature when available
		clusterer.registerFeatureType(
				"location",
				"location", 
				FastGeoSpatialCentroid.class, 
				new HaversineDistance(geoWeight));
		
		clusterer.setThreshold(threshold);
	
		return clusterer;
	}
}
