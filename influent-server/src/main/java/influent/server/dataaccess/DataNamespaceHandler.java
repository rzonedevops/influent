package influent.server.dataaccess;

import java.util.List;
import java.util.Map;

public interface DataNamespaceHandler {

	/**
	 * Creates a globally unique entity id given a local entity id and a namespace.
	 * The default implementation here simply returns the local entity id.
	 * An example override might look like this:
	 * <code>
	 * 		return namespace + "." + localEntityId;
	 * </code>
	 * 
	 * @param localEntityId
	 * 		the entity id as it exists in the data store
	 * 
	 * @return
	 * 		a globally unique id used for reference in the system
	 */
	public String globalFromLocalEntityId(String namespace, String localEntityId);

	/**
	 * Translates an entity id from global to local namespace for querying in
	 * the Data View. The default implementation returns the global id, since
	 * by default namespaces are not used.
	 * 
	 * An example override might look like this:
	 * <code>
	 *     final int i = globalEntityId.indexOf('.');
	 *
	 *     if (i >= 0) {
	 *         return globalEntityId.substring(i+1);
	 *     }
	 *     return super.localFromGlobalEntityId(globalEntityId);
	 * </code>
	 * 
	 * @param globalEntityId
	 * 		the globally unique entity id
	 * 
	 * @return
	 * 		the entity id as it exists in the data store
	 */
	public String localFromGlobalEntityId(String globalEntityId);

	/**
	 * Extracts and returns a namespace from a global entity id, typically formed
	 * by a call to createGlobalEntityId. The default implementation here returns
	 * null.
	 * 
	 * An example override might look like this:
	 * <code>
	 *     final int i = globalEntityId.indexOf('.');
	 *
	 *     if (i >= 0) {
	 *         return globalEntityId.substring(0, i);
	 *     }
	 *     return namespace + "." + localEntityId;
	 * </code>
	 * 
	 * @param globalEntityId
	 * 		the globally unique entity id
	 * 
	 * @return
	 */
	public String namespaceFromGlobalEntityId(String globalEntityId);

	/**
	 * Returns a data view table name, given a namespace to consider. The
	 * default implementation here maps the name using any mappings defined
	 * in construction, then prepends the namespace as a db schema in the form
	 * namespace.tableName if a namespace is provided. 
	 * @param namespace TODO
	 * @param standardTableName
	 * 		The standard data view table name.
	 * 
	 * @return
	 * 		The localized data view table name.
	 */
	public String tableName(String namespace, /*String namespace,*/String standardTableName);

	/**
	 * Localizes and organizes entities by namespace
	 */
	public Map<String, List<String>> entitiesByNamespace(List<String> entities);

	/**
	 * Escapes the column name in a db specific manner. This method is 
	 * provided for convenience.
	 */
	public String escapeColumnName(String columnName);

	/**
	 * Creates a temp table name in a db specific manner. This method is 
	 * provided for convenience.
	 */
	public String createTempTable(String tableName, String spec);
	
	/**
	 * Returns a temp table name in a db specific manner. This method is 
	 * provided for convenience.
	 */
	public String tempTableName(String tableName);

	/**
	 * Sets a row limit in a db specific manner. This method is 
	 * provided for convenience.
	 * 
	 * @param selectBody
	 * 		the body of the select statement, minus the leading select keyword itself
	 * @returns
	 * 		a full row limited statement, including the select keyword.
	 */
	public String rowLimit(String selectBody, long limit);



}