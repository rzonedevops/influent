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
package influent.bitcoin.server.spi;

import influent.bitcoin.server.clustering.BitCoinEntityClusterer;
import influent.server.dataaccess.EntityClustering;
import influent.bitcoin.server.clustering.BitcoinClusteringDataAccess;
import influent.bitcoin.server.data.BitcoinNamespaceHandler;
import influent.idl.FL_Clustering;
import influent.idl.FL_ClusteringDataAccess;
import influent.idl.FL_Geocoding;
import influent.midtier.api.EntityClusterer;
import influent.server.utilities.SQLConnectionPool;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


/**
 *
 */
public class BitcoinFLClusteringModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(FL_Clustering.class).to(EntityClustering.class);
		bind(FL_ClusteringDataAccess.class).to(BitcoinClusteringDataAccess.class);
		bind(EntityClusterer.class).to(BitCoinEntityClusterer.class);
	}
	
	/*
	 * Provide the database service
	 */
	@Provides @Singleton
	public EntityClustering clustering (
			SQLConnectionPool connectionPool,
			@Named("influent.data.view.tables") String tableNamesJson
	) {

		try {
			return new EntityClustering(connectionPool, new BitcoinNamespaceHandler(tableNamesJson));
		} catch (Exception e) {
			addError("Failed to load Clustering", e);
			return null;
		}
	}
	
	/*
	 */
	@Provides @Singleton
	public BitcoinClusteringDataAccess dataAccess (
			SQLConnectionPool connectionPool,
			EntityClustering kivaCluster,
			FL_Geocoding geocoding, 
			@Named("influent.data.view.tables") String tableNamesJson
	) {

		try {
			return new BitcoinClusteringDataAccess(connectionPool, kivaCluster, geocoding, new BitcoinNamespaceHandler(tableNamesJson));
		} catch (Exception e) {
			addError("Failed to load Clustering Data Access", e);
			return null;
		}
	}
}
