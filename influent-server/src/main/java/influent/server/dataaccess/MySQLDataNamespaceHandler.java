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
public class MySQLDataNamespaceHandler extends AbstractDataNamespaceHandler {

	/**
	 * Constructs a handler which uses standard table names.
	 */
	public MySQLDataNamespaceHandler() {
	}
	
	/**
	 * Creates a new handler based on a configuration defined as a JSON object string
	 * mapping standard names to localized names.
	 * 
	 * @throws JSONException 
	 */
	public MySQLDataNamespaceHandler(String tableNamesJson) throws JSONException {
		super(tableNamesJson);
	}
	
	/**
	 * Constructs a namespace handler from a
	 * map of standard names to localized names.
	 */
	public MySQLDataNamespaceHandler(Map<String, String> tableNames) {
		super(tableNames);
	}
	
	
	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataViewNamespaceHandler#escapeColumnName(java.lang.String)
	 */
	@Override
	public String escapeColumnName(String columnName) {
		return '`' + columnName + '`';
	}

	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataViewNamespaceHandler#createTempTable(java.lang.String, java.lang.String)
	 */
	@Override
	public String createTempTable(String tableName, String spec) {
		return "CREATE TEMPORARY TABLE "+ tempTableName(tableName) + " " + spec + "; ";
	}

	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataViewNamespaceHandler#tempTableName(java.lang.String)
	 */
	@Override
	public String tempTableName(String tableName) {
		return tableName;
	}

	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataViewNamespaceHandler#rowLimit(java.lang.String, long)
	 */
	@Override
	public String rowLimit(String selectBody, long limit) {
		if (limit > 0) {
			return "select " + selectBody + " limit " + limit;
		}
		
		return "select " + selectBody;
	}
}
