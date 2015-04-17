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

import influent.idl.FL_Entity;
import influent.idl.FL_Link;
import influent.idl.FL_Property;
import influent.idl.FL_PropertyTag;
import influent.idl.FL_SearchResult;
import influent.idl.FL_SearchResults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SearchResultsHelper extends FL_SearchResults {
	
	public String toJson() throws IOException {
		return SerializationHelper.toJson(this);
	}
	
	public static String toJson(FL_SearchResults descriptor) throws IOException {
		return SerializationHelper.toJson(descriptor);
	}

	public static String toJson(List<FL_SearchResults> descriptors) throws IOException {
		return SerializationHelper.toJson(descriptors, FL_SearchResults.getClassSchema());
	}

	public static String toJson(Map<String, List<FL_SearchResults>> map) throws IOException {
		return SerializationHelper.toJson(map, FL_SearchResults.getClassSchema());
	}
	
	public static FL_SearchResults fromJson(String json) throws IOException {
		return SerializationHelper.fromJson(json, FL_SearchResults.getClassSchema());
	}
	
	public static List<FL_SearchResults> listFromJson(String json) throws IOException {
		return SerializationHelper.listFromJson(json, FL_SearchResults.getClassSchema());
	}
	
	public static Map<String, List<FL_SearchResults>> mapFromJson(String json) throws IOException {
		return SerializationHelper.mapFromJson(json, FL_SearchResults.getClassSchema());
	}
	
	public static List<String> getKeysForTag(FL_SearchResults searchResults, FL_PropertyTag tag) {
		HashSet<String> keys = new HashSet<String>();
		
		for (FL_SearchResult searchResult : searchResults.getResults()) {
			List<FL_Property> propList = null;
			Object objWithProperties = searchResult.getResult();

			if (objWithProperties instanceof FL_Entity) {
				propList = ((FL_Entity)objWithProperties).getProperties();
			} else if (objWithProperties instanceof FL_Link) {
				propList = ((FL_Link)objWithProperties).getProperties();
			}

			if (propList != null) {
				for (FL_Property prop : propList) {
					for (FL_PropertyTag t : prop.getTags()) {
						if (tag.equals(t)) {
							keys.add(prop.getKey());
						}
					}
				}
			}
		}
		
		return new ArrayList<String>(keys);
	}

}
