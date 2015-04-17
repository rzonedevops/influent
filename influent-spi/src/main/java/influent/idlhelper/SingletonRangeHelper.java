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

package influent.idlhelper;

import influent.idl.FL_GeoData;
import influent.idl.FL_PropertyType;
import influent.idl.FL_SingletonRange;

import java.util.Date;

public class SingletonRangeHelper {

	/**
	 * Returns the value of a singleton range, handling nulls.
	 * @param range
	 * @return
	 */
	public static Object value(Object range) {
		return range instanceof FL_SingletonRange? ((FL_SingletonRange)range).getValue() : null;
	}
	
	/**
	 * Class construction should really be deprecated for use
	 * of these methods when the type is known at compile time.

	 * @param value
	 * @return new singleton range
	 */
	public static FL_SingletonRange from(String value) {
		return FL_SingletonRange.newBuilder()
			.setType(FL_PropertyType.STRING)
			.setValue(value)
			.build();
	}
	
	/**
	 * @param value
	 * @return new singleton range
	 */
	public static FL_SingletonRange from(Boolean value) {
		return FL_SingletonRange.newBuilder()
			.setType(FL_PropertyType.BOOLEAN)
			.setValue(value)
			.build();
	}
	
	/**
	 * @param value
	 * @return new singleton range
	 */
	public static FL_SingletonRange from(Date value) {
		return FL_SingletonRange.newBuilder()
			.setType(FL_PropertyType.DATE)
			.setValue(value.getTime())
			.build();
	}
	
	/**
	 * @param value
	 * @return new singleton range
	 */
	public static FL_SingletonRange from(Integer value) {
		return FL_SingletonRange.newBuilder()
			.setType(FL_PropertyType.INTEGER)
			.setValue(value)
			.build();
	}

	/**
	 * @param value
	 * @return new singleton range
	 */
	public static FL_SingletonRange from(Long value) {
		return FL_SingletonRange.newBuilder()
			.setType(FL_PropertyType.LONG)
			.setValue(value)
			.build();
	}

	/**
	 * @param value
	 * @return new singleton range
	 */
	public static FL_SingletonRange from(Float value) {
		return FL_SingletonRange.newBuilder()
			.setType(FL_PropertyType.DOUBLE)
			.setValue(Double.valueOf(value.doubleValue()))
			.build();
	}

	/**
	 * @param value
	 * @return new singleton range
	 */
	public static FL_SingletonRange from(Double value) {
		return FL_SingletonRange.newBuilder()
			.setType(FL_PropertyType.DOUBLE)
			.setValue(value)
			.build();
	}

	/**
	 * @param value
	 * @return new singleton range
	 */
	public static FL_SingletonRange from(FL_GeoData value) {
		return FL_SingletonRange.newBuilder()
			.setType(FL_PropertyType.GEO)
			.setValue(value)
			.build();
	}

	/**
	 * @param value
	 * @return new singleton range
	 */
	public static FL_SingletonRange from(Object value, FL_PropertyType type) {
		return FL_SingletonRange.newBuilder()
			.setType(type)
			.setValue(value)
			.build();
	}
	
	/**
	 * Do a best guess determination of type. Note that if the value is null
	 * the property type is indeterminant and will be marked as OTHER.
	 * 
	 * @param untyped value
	 * @return new typed singleton range
	 */
	public static FL_SingletonRange fromUnknown(Object value) {
		
		// can't type nulls, which is why this method is not preferred
		if (value == null) {
			return null;
		}
		
		if (value instanceof String) {
			return from((String)value);
			
		} else if (value instanceof Number) {
			Number number = (Number)value;
			
			if (number instanceof Integer) {
				return from((Integer)number);
				
			} else if (number instanceof Long) {
				return from((Long)number);
			}

			return from(Double.valueOf(number.doubleValue()));
			
		} else if (value instanceof Boolean) {
			return from((Boolean)value);
			
		} else if (value instanceof Date) {
			return from((Date)value);
			
		} else if (value instanceof FL_GeoData) {
			return from((FL_GeoData)value);				
		} 

		// debatable.
		return from(value.toString(), FL_PropertyType.STRING);
	}
}

