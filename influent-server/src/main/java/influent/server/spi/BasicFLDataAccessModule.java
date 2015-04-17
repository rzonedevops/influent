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
package influent.server.spi;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import influent.idl.FL_DataAccess;
import influent.idl.FL_EntitySearch;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.dataaccess.DataViewDataAccess;
import influent.server.dataaccess.MSSQLDataNamespaceHandler;
import influent.server.utilities.SQLConnectionPool;
import oculus.aperture.spi.common.Properties;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BasicFLDataAccessModule extends AbstractModule {

	private static Logger s_logger = LoggerFactory.getLogger(BasicFLDataAccessModule.class);



	@Override
	protected void configure() {
		bind(FL_DataAccess.class).to(DataViewDataAccess.class);
	}




	@Provides @Singleton
	public DataNamespaceHandler namespaceHandler() {
		return new MSSQLDataNamespaceHandler();
	}




	/*
	 * Provide the database service
	 */
	@Provides @Singleton
	public DataViewDataAccess connect (
		@Named("aperture.server.config") Properties config,
		SQLConnectionPool connectionPool,
		FL_EntitySearch search,
		DataNamespaceHandler namespaceHandler
	) {
		try {
			return new DataViewDataAccess(
				config,
				connectionPool,
				search,
				namespaceHandler
			);
		} catch (Exception e) {
			addError("Failed to load Data Access", e);
			return null;
		}
	}
}
