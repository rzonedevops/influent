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
public class MSSQLDataNamespaceHandler extends AbstractDataNamespaceHandler {

	/**
	 * Constructs a handler which uses standard table names.
	 */
	public MSSQLDataNamespaceHandler() {
	}
	
	/**
	 * Creates a new handler based on a configuration defined as a JSON object string
	 * mapping standard names to localized names.
	 * 
	 * @throws JSONException 
	 */
	public MSSQLDataNamespaceHandler(String tableNamesJson) throws JSONException {
		super(tableNamesJson);
	}
	
	/**
	 * Constructs a namespace handler from a
	 * map of standard names to localized names.
	 */
	public MSSQLDataNamespaceHandler(Map<String, String> tableNames) {
		super(tableNames);
	}
	
	
	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#escapeColumnName(java.lang.String)
	 */
	@Override
	public String escapeColumnName(String columnName) {
		return '[' + columnName + ']';
	}
	
	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#createTempTable(java.lang.String, java.lang.String)
	 */
	@Override
	public String createTempTable(String tableName, String spec) {
		return " CREATE TABLE "+ tempTableName(tableName) + " " + spec + "; ";
	}

	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#tempTableName(java.lang.String)
	 */
	@Override
	public String tempTableName(String tableName) {
		String name = tableName;
		if (!tableName.startsWith("##")) {
			name = "##" + tableName;
		}
		return name.replaceAll("-", "_");
	}
	
	/* (non-Javadoc)
	 * @see influent.server.dataaccess.DataNamespaceHandler#rowLimit(java.lang.String, long)
	 */
	@Override
	public String rowLimit(String selectBody, long limit) {
		if (limit > 0) {
			return "select top " + limit + " " + selectBody;
		}
		return "select " + selectBody;
	}

}
