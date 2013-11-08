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
package influent.cluster;

import influent.idl.FL_Entity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.oculusinfo.ml.Instance;
import com.oculusinfo.ml.unsupervised.Cluster;


public abstract class FLEntityInstanceFactory {
	
	/***
	 * Converts entity objects into Instance objects used in the clustering library
	 * 
	 * @param entities to convert to instance objects
	 * @return
	 */
	public Collection<Instance> toInstance(List<FL_Entity> entities) {
		Collection<Instance> instances = new LinkedList<Instance>();
		
		for (FL_Entity entity : entities) {
			instances.add( toInstance(entity) );
		}
		return instances;
	}

	/***
	 * Converts cluster objects returned from the clusterer into entity cluster objects 
	 * 
	 * @param clusters to convert into entity cluster objects
	 * @param entityIndex is a lookup of entity objects keyed by id
	 * @return
	 */
	public List<FL_Entity> toEntity(Collection<Cluster> clusters, Map<String, FL_Entity> entityIndex) {
		List<FL_Entity> entities = new LinkedList<FL_Entity>();
		
		for (Cluster cluster : clusters) {
			entities.add( toEntity(cluster, entityIndex) );
		}
		return entities;
	}
	
	/***
	 * Override to provide an entity to instance object conversion.  
	 * Each instance object is made up of a collection of features. 
	 * This method provides to conversion of entity object properties
	 * to instance features.  
	 * 
	 * @param entity to convert to instance object
	 * @return
	 */
	public abstract Instance toInstance(FL_Entity entity);
	
	/***
	 * Override to provide an instance to entity object conversion.  
	 * Each instance object is made up of a collection of features and 
	 * instance members. This method provides conversion of instance object 
	 * features to entity object properties.  
	 * 
	 * @param instance to convert to entity object
	 * @param entityIndex is a lookup of entity objects keyed by id
	 * @return
	 */
	public abstract FL_Entity toEntity(Instance inst, Map<String, FL_Entity> entityIndex);
	
	/***
	 * Override to provide a cluster to entity cluster object conversion.  
	 * Each cluster object is made up of a collection of features and 
	 * instance members. This method provides to conversion of cluster object 
	 * features to entity cluster object properties + members ids.  
	 * 
	 * @param cluster to convert to entity cluster object
	 * @param entityIndex is a lookup of entity objects keyed by id
	 * @return
	 */
	public abstract FL_Entity toEntity(Cluster cluster, Map<String, FL_Entity> entityIndex);
}
