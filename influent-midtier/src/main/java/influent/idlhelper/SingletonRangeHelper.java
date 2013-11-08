package influent.idlhelper;

import influent.idl.FL_GeoData;
import influent.idl.FL_PropertyType;
import influent.idl.FL_SingletonRange;

import java.util.Date;

public class SingletonRangeHelper extends FL_SingletonRange {

	public SingletonRangeHelper(Object value) {
		FL_PropertyType type = FL_PropertyType.STRING;
		
		if (value != null && !(value instanceof String)) {
			type = FL_PropertyType.STRING;
			
			if (value instanceof Number) {
				Number number = (Number)value;
				
				if (number instanceof Integer) {
					type = FL_PropertyType.LONG;
					value = Long.valueOf(number.longValue());
					
				} else if (number instanceof Long) {
					type = FL_PropertyType.LONG;
					
				} else {
					type = FL_PropertyType.DOUBLE;
					value = Double.valueOf(number.doubleValue());
				}
				
			} else if (value instanceof Boolean) {
				type = FL_PropertyType.BOOLEAN;
				
			} else if (value instanceof Date) {
				type = FL_PropertyType.DATE;
				value = Long.valueOf(((Date)value).getTime());
				
			} else if (value instanceof FL_GeoData) {
				type = FL_PropertyType.GEO;
				
			} else {
				value = value.toString();
			}
		}
		
		setValue(value);
		setType(type);
	}
	public SingletonRangeHelper(Object value, FL_PropertyType type) {
		setValue(value);
		setType(type);
	}

}
