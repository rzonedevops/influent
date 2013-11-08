package influent.entity.clustering;

import java.util.HashMap;
import java.util.Map;

public class Utils {

	public static Map<String, String> parseArguments(String[] args) {
		Map<String, String> argMap = new HashMap<String, String>();
		
		for (String arg : args) {
			String[] keyVal = arg.split("=", 2);
			
			if (keyVal.length == 2) {
				String key = keyVal[0];
				String val = keyVal[1];
				
				// strip off switch characters --
				if (key.startsWith("--")) {
					key = key.substring(2);
				}
				
				// strip off surrounding " " for arg value
				if (val.startsWith("\"")) {
					val = val.substring(1);
				}
				if (val.endsWith("\"")) {
					val = val.substring(0, val.length()-1);
				}
				
				argMap.put(key, val);
			}
		}
		return argMap;
	}
	
	public static String[][] parseClusterOrder(String arg) {
		String[] tokens = arg.split(",(?=([^\\(\\)]*\\([^\\(\\)]*\\))*[^\\(\\)]*$)");
		String[][] order = new String[tokens.length][];
		
		for (int i=0; i < tokens.length; i++) {
			String token = tokens[i].trim();
			if (token.startsWith("(")) {
				token = token.substring(1);
			}
			if (token.endsWith(")")) {
				token = token.substring(0, token.length()-1);
			}
			String[] subtokens = token.split(",");
			order[i] = new String[subtokens.length];
			for (int j=0; j < subtokens.length; j++) {
				order[i][j] = subtokens[j].trim();
			}
		}
		return order;
	}
	
	public static SchemaField[] parseSchema(String schemaStr) throws IllegalArgumentException {	
		String[] fields = schemaStr.split(",");
		SchemaField[] schema = new SchemaField[fields.length];
	
		for (int i=0; i < fields.length; i++) {
			String[] keyVal = fields[i].split(":");
			
			if (keyVal.length == 2) {
				schema[i] = new SchemaField(keyVal[0].trim(), SchemaField.FieldType.valueOf(keyVal[1].trim()));
			}
			else {
				throw new IllegalArgumentException("Invalid schema value!");
			}
		}
		return schema;
	}
	
	public static Map<String, SchemaField> parseSchemaMap(String schemaStr) throws IllegalArgumentException {
		Map<String, SchemaField> schemaMap = new HashMap<String, SchemaField>();
		SchemaField[] schema = parseSchema(schemaStr);
		
		for (SchemaField field : schema) {
			schemaMap.put(field.fieldName, field);
		}
		return schemaMap;
	}
}
