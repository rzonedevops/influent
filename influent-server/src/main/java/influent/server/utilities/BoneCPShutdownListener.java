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
package influent.server.utilities;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoneCPShutdownListener implements ServletContextListener {

	private static final Logger s_logger = LoggerFactory.getLogger(BoneCPShutdownListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// do nothing
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		List<BoneCPConnectionPool> allConnectionPools = BoneCPConnectionPool.ALL_POOLS;

		while (!allConnectionPools.isEmpty()) {
			try {
				(allConnectionPools.get(0)).shutdownConnectionPool();
			} catch (SQLException e) {
				s_logger.error("Failed to shutdown connection pool", e);
			}
		}

		// This manually deregisters JDBC driver, which prevents Tomcat 7 from complaining about memory leaks wrto this class
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
			} catch (SQLException e) {
				s_logger.error("Error deregistering driver %s", driver, e);
			}
		}
	}

}
