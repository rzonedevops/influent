/*
 * Copyright 2013-2016 Uncharted Software Inc.
 *
 *  Property of Uncharted(TM), formerly Oculus Info Inc.
 *  https://uncharted.software/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * Copyright (C) 2013-2015 Uncharted Software Inc.
 *
 * Property of Uncharted(TM), formerly Oculus Info Inc.
 * http://uncharted.software/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package influent.server.clustering.utils;

import influent.idl.FL_Cluster;
import influent.idl.FL_Entity;
import influent.server.clustering.ClusterContext;

import java.util.Collection;
import java.util.List;

public interface ContextRead {
	
	/**
	 * Returns the context id
	 */
	public String getUid();

	/**
	 * Returns the cluster context associated with this context
	 */
	public ClusterContext getContext();

	/**
	 * Given an id, return the associated cluster.
	 */
	public FL_Cluster getCluster(String id);

	/**
	 * Return all clusters.
	 */
	public List<FL_Cluster> getClusters();
	
	/**
	 * Return root level objects in context - either FL_Entity or FL_Cluster objects
	 */
	public List<Object> getRootObjects();
	
	/**
	 * Return all child context ids
	 */
	public List<String> getChildContexts();

	/**
	 * Return all clusters matching the specified ids.
	 */
	public List<FL_Cluster> getClusters(Collection<String> ids);

	/**
	 * Given an id, return the associated entity.
	 */
	public FL_Entity getEntity(String id);

	/**
	 * Return all entities matching the specified ids.
	 */
	public List<FL_Entity> getEntities(Collection<String> ids);
	
	/**
	 * Returns whether any entities or clusters exist in context
	 */
	public boolean isEmpty();
	
	/**
	 * Returns the version of the context; which is updated whenever it is modified
	 */	
	public int getVersion();
}
