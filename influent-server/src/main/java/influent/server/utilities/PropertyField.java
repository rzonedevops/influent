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
package influent.server.utilities;

import influent.idl.FL_PropertyDescriptor;

import java.util.List;

/**
 * Defines source fields for a non-primitive property like FL_GeoData
 */
public class PropertyField {
	private final String name;
	private final boolean searchable;
	private final String key;

	private FL_PropertyDescriptor property;

	/**
	 * Initialize the fixed properties
	 */
	public PropertyField(String name, String key, boolean isSearchable) {
		this.name = name;
		this.key = key;
		this.searchable = isSearchable;
	}

	public void setProperty(FL_PropertyDescriptor property) {
		this.property = property;
	}

	/**
	 * @return the field name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return true if should be searched when this higher level property is searched.
	 */
	public boolean isSearchable() {
		return searchable;
	}

	/**
	 * @return the property key which the field maps to.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the property which the field is populated from, if key is found.
	 */
	public FL_PropertyDescriptor getProperty() {
		return property;
	}

	/**
	 * Interface which returns property fields
	 */
	public static interface Provider {

		/**
		 * Returns true if a property has field mappings
		 * @param key
		 *  property key.
		 * @return
		 *  boolean
		 */
		public boolean isCompositeProperty(String key);

		/**
		 * Returns a list of field mappings for a property key
		 * @param key
		 *  property key.
		 * @return
		 *  List of field definitions
		 */
		public List<PropertyField> getFields(String key);

		/**
		 * Returns a field definition for a property field name.
		 * @param key
		 *  property key
		 * @param name
		 *  name of field
		 * @return
		 *  A field definition
		 */
		public PropertyField getField(String key, String name);
	}


}
