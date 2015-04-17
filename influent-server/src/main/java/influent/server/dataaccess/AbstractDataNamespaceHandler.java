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
package influent.server.dataaccess;

import influent.server.configuration.ApplicationConfiguration;
import influent.server.utilities.InfluentId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author djonker
 *
 */
public abstract class AbstractDataNamespaceHandler implements DataNamespaceHandler {



	/**
	 * Constructs a handler which uses standard table names.
	 */
	public AbstractDataNamespaceHandler() {}
	
	
	
	
	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#globalFromLocalEntityId(java.lang.char, java.lang.String, java.lang.String)
	 */
	@Override
	public String globalFromLocalEntityId(char entityClass, String entityType, String localEntityId) {
		return InfluentId.fromNativeId(entityClass, entityType, localEntityId).getInfluentId();
	}




	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#typedFromLocalEntityId(java.lang.String, java.lang.String)
	 */
	@Override
	public String typedFromLocalEntityId(String entityType, String localEntityId) {
		return entityType + "." + localEntityId;
	}




	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#localFromGlobalEntityId(java.lang.String)
	 */
	@Override
	public String localFromGlobalEntityId(String globalEntityId) {
		return InfluentId.fromInfluentId(globalEntityId).getNativeId();
	}




	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#typedFromGlobalEntityId(java.lang.String)
	 */
	@Override
	public String typedFromGlobalEntityId(String globalEntityId) {
		return InfluentId.fromInfluentId(globalEntityId).getTypedId();
	}





	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public String globalFromTypedEntityId(char entityClass, String typedEntityId) {
		return null;
	}




	@Override
	public String localFromTypedEntityId(String typedEntityId) {
		return null;
	}




	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#namespaceFromGlobalEntityId(java.lang.String)
	 */
	@Override
	public String entityTypeFromGlobalEntityId(String globalEntityId) {
		return InfluentId.fromInfluentId(globalEntityId).getIdType();
	}




	@Override
	public String entityTypeFromTypedEntityId(String typedEntityId) {
		return null;
	}




	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#entitiesByNamespace(java.util.List)
	 */
	@Override
	public Map<String, List<String>> entitiesByType(List<String> entities) {
		Map<String, List<String>> entityTypes = new LinkedHashMap<String, List<String>>();
		
		// global to local conversion, organized by namespace.
		if (entities != null) {
			for (String id : entities) {
				final String entityType = entityTypeFromGlobalEntityId(id);
	
				// get the list of entities for this namespace.
				findOrCreateNamespace(entityType, entityTypes).add(id);
			}
		}

		return entityTypes;
	}




	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#toSQLId(java.lang.String, java.lang.String)
	 */
	@Override
	public String toSQLId(String id, ApplicationConfiguration.SystemColumnType type) {
		String sqlId = id;
		if (type == ApplicationConfiguration.SystemColumnType.HEX) {
			sqlId = idToBinaryFromHex(id);
		}

		if (type == ApplicationConfiguration.SystemColumnType.STRING ||
			type == ApplicationConfiguration.SystemColumnType.HEX) {
			sqlId = "'" + sqlId + "'";
		}

		return sqlId;
	}
	

	
	/**
	 * Returns binary id sql from a hexadecimal string id. Called by toSQLId.
	 * 
	 * @param id
	 * @return
	 */
	protected abstract String idToBinaryFromHex(String id);




    /**
     * Returns binary-converted column sql. Called by toSQLIdColumn.
     *
     * @param columnName
     * @return
     */
    protected abstract String columnToHex(String columnName);




	/**
	 * Finds or creates a namespace in the existing map of namespaces.
	 */
	private List<String> findOrCreateNamespace(String namespace, Map<String, List<String>> namespaces) {
		List<String> ns = namespaces.get(namespace);

		if (ns == null) {
			ns = new ArrayList<String>();
			namespaces.put(namespace, ns);
		}
		
		return ns;
	}
}
