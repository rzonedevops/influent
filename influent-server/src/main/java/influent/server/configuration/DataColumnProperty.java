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
package influent.server.configuration;

import java.util.HashMap;
import java.util.Map;

public class DataColumnProperty {
	String _key;
	Map<String, String> _memberKeyMap = new HashMap<String, String>();
	Map<String, ApplicationConfiguration.SystemColumnType> _memberTypeMap = new HashMap<String, ApplicationConfiguration.SystemColumnType>();



	String getKey() {
		return _key;
	}




	void setKey(String key) {
		_key = key;
	}




	void setMemberKey(String memberKey) {
		setMemberKey(null, memberKey);
	}




	void setMemberKey(String type, String memberKey) {
		_memberKeyMap.put(type, memberKey);
	}




	String getMemberKey() {
		return getMemberKey(null);
	}




	String getMemberKey(String type) {

		if (_memberKeyMap.keySet().size() == 1 && _memberKeyMap.containsKey(null)) {
			return _memberKeyMap.get(null);
		}

		return _memberKeyMap.get(type);
	}




	void setMemberType(String memberType) {
		setMemberType(null, memberType);
	}




	void setMemberType(String type, String memberType) {
		if (memberType.equalsIgnoreCase(ApplicationConfiguration.SystemColumnType.STRING.name())) {
			_memberTypeMap.put(type, ApplicationConfiguration.SystemColumnType.STRING);
		} else if (memberType.equalsIgnoreCase(ApplicationConfiguration.SystemColumnType.DOUBLE.name())) {
			_memberTypeMap.put(type, ApplicationConfiguration.SystemColumnType.DOUBLE);
		} else if (memberType.equalsIgnoreCase(ApplicationConfiguration.SystemColumnType.FLOAT.name())) {
			_memberTypeMap.put(type, ApplicationConfiguration.SystemColumnType.FLOAT);
		} else if (memberType.equalsIgnoreCase(ApplicationConfiguration.SystemColumnType.INTEGER.name())) {
			_memberTypeMap.put(type, ApplicationConfiguration.SystemColumnType.INTEGER);
		} else if (memberType.equalsIgnoreCase(ApplicationConfiguration.SystemColumnType.DATE.name())) {
			_memberTypeMap.put(type, ApplicationConfiguration.SystemColumnType.DATE);
		} else if (memberType.equalsIgnoreCase(ApplicationConfiguration.SystemColumnType.BOOLEAN.name())) {
			_memberTypeMap.put(type, ApplicationConfiguration.SystemColumnType.BOOLEAN);
		} else if (memberType.equalsIgnoreCase(ApplicationConfiguration.SystemColumnType.HEX.name())) {
			_memberTypeMap.put(type, ApplicationConfiguration.SystemColumnType.HEX);
		}
	}




	void setMemberType(ApplicationConfiguration.SystemColumnType memberType) {
		setMemberType(null, memberType);
	}




	void setMemberType(String type, ApplicationConfiguration.SystemColumnType memberType) {
		_memberTypeMap.put(type, memberType);
	}




	ApplicationConfiguration.SystemColumnType getMemberType() {
		return getMemberType(null);
	}




	ApplicationConfiguration.SystemColumnType getMemberType(String type) {

		if (_memberTypeMap.keySet().size() == 1 && _memberTypeMap.containsKey(null)) {
			return _memberTypeMap.get(null);
		}

		return _memberTypeMap.get(type);
	}
}