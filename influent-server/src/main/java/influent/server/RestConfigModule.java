/**
 * Copyright (c) 2013-2014 Oculus Info Inc.
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
package influent.server;

import influent.server.clustering.utils.ClusterContextCache;
import influent.server.rest.AggregatedLinkResource;
import influent.server.rest.BigChartResource;
// import influent.server.rest.CacheStatsResource; // Temporarily disabled
// import influent.server.rest.ChartResource; // Temporarily disabled
import influent.server.rest.EntityDetailsResource;
import influent.server.rest.EntityLookupResource;
import influent.server.rest.EntitySearchParamsResource;
import influent.server.rest.EntitySearchResource;
// import influent.server.rest.ExportGraphResource; // Temporarily disabled
// import influent.server.rest.ExportTransactionTableResource; // Temporarily disabled
// import influent.server.rest.ImportGraphResource; // Temporarily disabled
import influent.server.rest.LeafEntityLookupResource;
import influent.server.rest.ModifyContextResource;
import influent.server.rest.PatternSearchResource;
import influent.server.rest.RelatedLinkResource;
// import influent.server.rest.RestoreStateResource; // Temporarily disabled
import influent.server.rest.SaveStateResource;
import influent.server.rest.TransactionTableResource;
import influent.server.spi.ExportDataService;
import influent.server.spi.ImportDataService;
import influent.server.spi.impl.graphml.GraphMLExportDataService;
import influent.server.spi.impl.graphml.GraphMLImportDataService;
import oculus.aperture.common.rest.ResourceDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;

public class RestConfigModule extends AbstractModule {

	private static final Logger s_logger = LoggerFactory.getLogger(RestConfigModule.class);

	// not sure if this is the best place for this
	@Provides @Singleton ClusterContextCache getClusterContextCache(
			@Named("influent.midtier.ehcache.config") String ehCacheConfig,
			@Named("influent.dynamic.clustering.cache.name") String cacheName
			) {
		return new ClusterContextCache(ehCacheConfig, cacheName);
	}
	
	@Override
	protected void configure() {

		String encoding = System.getProperty("file.encoding");
		if (!encoding.equalsIgnoreCase("utf-8")) {
			s_logger.warn("*** Default encoding is "+encoding+".  Should be UTF-8.  JSON decoding of messages may fail or produce undefined results with UTF-encoded characters (non-english languages, characters with accents, etc.) ***");
		}
		
		// Bind the export mechanism implementation
		bind(ExportDataService.class).to(GraphMLExportDataService.class);
		bind(ImportDataService.class).to(GraphMLImportDataService.class);
		
	    MapBinder<String, ResourceDefinition> resourceBinder =
			MapBinder.newMapBinder(binder(), String.class, ResourceDefinition.class);

		// REMOVE
		resourceBinder.addBinding("/transactions").toInstance(new ResourceDefinition(TransactionTableResource.class));
		resourceBinder.addBinding("/search").toInstance(new ResourceDefinition(EntitySearchResource.class));
		resourceBinder.addBinding("/searchparams").toInstance(new ResourceDefinition(EntitySearchParamsResource.class));
		resourceBinder.addBinding("/patternsearch").toInstance(new ResourceDefinition(PatternSearchResource.class));
		resourceBinder.addBinding("/relatedlinks").toInstance(new ResourceDefinition(RelatedLinkResource.class));
		resourceBinder.addBinding("/aggregatedlinks").toInstance(new ResourceDefinition(AggregatedLinkResource.class));
		// resourceBinder.addBinding("/chart").toInstance(new ResourceDefinition(ChartResource.class)); // Temporarily disabled
		resourceBinder.addBinding("/bigchart").toInstance(new ResourceDefinition(BigChartResource.class));
		resourceBinder.addBinding("/entities").toInstance(new ResourceDefinition(EntityLookupResource.class));
		// resourceBinder.addBinding("/exporttransactions").toInstance(new ResourceDefinition(ExportTransactionTableResource.class)); // Temporarily disabled
		resourceBinder.addBinding("/savestate").toInstance(new ResourceDefinition(SaveStateResource.class));
		// resourceBinder.addBinding("/restorestate").toInstance(new ResourceDefinition(RestoreStateResource.class)); // Temporarily disabled
		resourceBinder.addBinding("/containedentities").toInstance(new ResourceDefinition(LeafEntityLookupResource.class));
		resourceBinder.addBinding("/modifycontext").toInstance(new ResourceDefinition(ModifyContextResource.class));
		// resourceBinder.addBinding("/export").toInstance(new ResourceDefinition(ExportGraphResource.class)); // Temporarily disabled
		// resourceBinder.addBinding("/import").toInstance(new ResourceDefinition(ImportGraphResource.class)); // Temporarily disabled
		resourceBinder.addBinding("/entitydetails").toInstance(new ResourceDefinition(EntityDetailsResource.class));
		// resourceBinder.addBinding("/cachestats").toInstance(new ResourceDefinition(CacheStatsResource.class)); // Temporarily disabled
	}
}
