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

package influent.kiva.server.search;

import influent.idl.*;
import influent.idlhelper.EntityHelper;
import influent.idlhelper.LinkHelper;
import influent.idlhelper.PropertyHelper;
import influent.idlhelper.SingletonRangeHelper;
import influent.server.dataaccess.DataNamespaceHandler;
import influent.server.search.DataViewLinkSearch;
import influent.server.sql.SQLBuilder;
import influent.server.utilities.SQLConnectionPool;
import org.apache.avro.AvroRemoteException;

import java.util.Collections;
import java.util.List;

import oculus.aperture.spi.common.Properties;
public class KivaTransactionsSearch extends DataViewLinkSearch implements FL_LinkSearch {

	private final FL_DataAccess service;

	//----------------------------------------------------------------------
	
	
	public KivaTransactionsSearch(
		Properties config,
		SQLConnectionPool connectionPool,
		DataNamespaceHandler namespaceHandler,
		SQLBuilder sqlBuilder,
		FL_ClusteringDataAccess clusterDataAccess,
		FL_DataAccess service
	) {
		super(config, connectionPool, namespaceHandler, sqlBuilder, clusterDataAccess);
		this.service = service;
	}




	@Override
	protected FL_Link createLink(String uid, String from, String to, String type, List<FL_Property> props) {

		FL_Property fromProp = null;
		FL_Property toProp = null;

		for (FL_Property prop : props) {
			if (prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.FROM.name())) {
				fromProp = prop;
			} else if (prop.getKey().equalsIgnoreCase(FL_RequiredPropertyKey.TO.name())) {
				toProp = prop;
			}
		}

		if (fromProp != null) {
			try {
				List<FL_Entity> entityResults = service.getEntities(Collections.singletonList(from), FL_LevelOfDetail.SUMMARY);
				if (entityResults != null && entityResults.size() > 0) {
					for (FL_Entity entity : entityResults) {
						if (entity.getUid().equalsIgnoreCase(from)) {
							fromProp.setRange(
								SingletonRangeHelper.from(
									EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.NAME).getValue().toString() + " (" + PropertyHelper.getValue(fromProp) + ")"
								)
							);
						}
					}
				}
			} catch (AvroRemoteException are) {
				// do nothing
			}
		}

		if (toProp != null) {
			try {
				List<FL_Entity> entityResults = service.getEntities(Collections.singletonList(to), FL_LevelOfDetail.SUMMARY);
				if (entityResults != null && entityResults.size() > 0) {
					for (FL_Entity entity : entityResults) {
						if (entity.getUid().equalsIgnoreCase(to)) {
							toProp.setRange(
								SingletonRangeHelper.from(
									EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.NAME).getValue().toString() + " (" + PropertyHelper.getValue(toProp) + ")"
								)
							);
						}
					}
				}
			} catch (AvroRemoteException are) {
				// do nothing
			}
		}

		return new LinkHelper(uid, from, to, type, props, null);
	}
}
