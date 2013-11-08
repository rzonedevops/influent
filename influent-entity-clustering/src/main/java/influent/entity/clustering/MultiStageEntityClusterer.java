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

import influent.idl.FL_Geocoding;
import influent.midtier.IdGenerator;

import java.util.LinkedList;
import java.util.List;

import com.oculusinfo.ml.unsupervised.Clusterer;

public class MultiStageEntityClusterer extends GeneralEntityClusterer {
	
	private GeneralEntityInstanceFactory instanceFactory;
	
	private static List<Clusterer> createClusterStages() {
		List<Clusterer> clusterStages = new LinkedList<Clusterer>();
		//
		// STAGE 1: group by highly similar name and geo items with identical type: E.g. Dan's in US
		//
		clusterStages.add(GeneralEntityClustererFactory.createClusterer(1.0, 1.0, 5.0, 0.2, false));
		
		// 
		// STAGE 2: group by somewhat similar name and geo items with identical type: E.g. Dan's and Danielle's in US
		//
		clusterStages.add(GeneralEntityClustererFactory.createClusterer(1.0, 1.0, 5.0, 0.4, false));
		
		// 
		// STAGE 3: group by somewhat similar geo only items with identical type: E.g. Entities in US
		//
		clusterStages.add(GeneralEntityClustererFactory.createClusterer(0, 1.0, 5.0, 0.4, false));
		
		// 
		// STAGE 4: group by broad geo only items with identical type: E.g. Entities in North America
		//
		clusterStages.add(GeneralEntityClustererFactory.createClusterer(0, 1.0, 3.0, 0.4, false));
				
		return clusterStages;
	}
	
	public GeneralEntityInstanceFactory getEntityInstanceFactory() {
		return this.instanceFactory;
	}
	
	@Override
	public void init(Object[] args)  throws IllegalArgumentException {
		try {
			IdGenerator idGen = (IdGenerator)args[0];
			FL_Geocoding geocoder = (FL_Geocoding)args[1];
			this.instanceFactory = new GeneralEntityInstanceFactory(idGen, geocoder);
			super.init( instanceFactory,
					    createClusterStages() );
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Invalid initialization parameters. Parameters must be init(IdGenerator idGenerator)", e);
		}
	}
}
