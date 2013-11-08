/**
 * 
 */
package influent.server.dataaccess;


import java.util.Map;

import org.json.JSONException;


/**
 * @author djonker
 *
 */
public class OracleDataNamespaceHandler extends AbstractDataNamespaceHandler {

	/**
	 * Constructs a handler which uses standard table names.
	 */
	public OracleDataNamespaceHandler() {
	}
	
	/**
	 * Creates a new handler based on a configuration defined as a JSON object string
	 * mapping standard names to localized names.
	 * 
	 * @throws JSONException 
	 */
	public OracleDataNamespaceHandler(String tableNamesJson) throws JSONException {
		super(tableNamesJson);
	}
	
	/**
	 * Constructs a namespace handler from a
	 * map of standard names to localized names.
	 */
	public OracleDataNamespaceHandler(Map<String, String> tableNames) {
		super(tableNames);
	}
	
	
	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataViewNamespaceHandler#escapeColumnName(java.lang.String)
	 */
	@Override
	public String escapeColumnName(String columnName) {
		return '"' + columnName + '"';
	}

	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataViewNamespaceHandler#rowLimit(java.lang.String, long)
	 */
	@Override
	public String rowLimit(String selectBody, long limit) {
		if (limit > 0) {
			return "select * from (select " + selectBody + ") where ROWNUM <= " + limit;
		}
		return "select " + selectBody;
	}

	

	
	/**
	 * Only used for pre-clustering, which we are not using.
	 * @see influent.server.dataaccess.DataViewNamespaceHandler#createTempTable(java.lang.String, java.lang.String)
	 */
	@Override
	public String createTempTable(String tableName, String spec) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataViewNamespaceHandler#tempTableName(java.lang.String)
	 */
	@Override
	public String tempTableName(String tableName) {
		return tableName;
	}

}
