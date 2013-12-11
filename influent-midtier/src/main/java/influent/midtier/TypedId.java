/**
 * Copyright (c) 2013 Oculus Info Inc.
 * http://www.oculusinfo.com/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package influent.midtier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Native ids are typed for internal reference to make it easy to distinguish
 * between entity types, like accounts or clusters.
 * 
 * @author djonker
 * 
 */
public final class TypedId {

	/**
	 * The account id type.
	 */
	public final static char ACCOUNT = 'a';
	
	/**
	 * The cluster id type.
	 */
	public final static char CLUSTER = 'c';
	
	private final char idType;
	private final String nativeId;
	private final String typedId;

	
	/**
	 * Constructs from a native id and type.
	 */
	public static TypedId fromNativeId(final char type, final String id) {
		return new TypedId(type, id);
	}

	/**
	 * Constructs from a typed id.
	 */
	public static TypedId fromTypedId(final String typedId) {
		return new TypedId(typedId);
	}

//	/**
//	 * Constructs a list from a collection of typed ids, filtering for the specified type.
//	 */
//	public static List<TypedId> fromTypedIds(final Collection<String> typedIds, final char filterType) {
//		final List<TypedId> list = new ArrayList<TypedId>(typedIds.size());
//		
//		for (String s : typedIds) {
//			final TypedId typedId = fromTypedId(s);
//			
//			if (typedId.idType == filterType) {
//				list.add(typedId);
//			}
//		}
//		
//		return list;
//	}
//
//	/**
//	 * Constructs a list from a collection of typed ids.
//	 */
//	public static List<TypedId> fromTypedIds(final Collection<String> typedIds) {
//		final List<TypedId> list = new ArrayList<TypedId>(typedIds.size());
//		
//		for (String s : typedIds) {
//			list.add(fromTypedId(s));
//		}
//		
//		return list;
//	}

	/**
	 * Converts a list of typed ids to native ids, returning a new list.
	 */
	public static List<String> nativeFromTypedIds(final Collection<String> typedIds) {
		final List<String> list = new ArrayList<String>(typedIds.size());
		
		for (String s : typedIds) {
			list.add(fromTypedId(s).nativeId);
		}
		
		return list;
	}
	
	/**
	 * Converts a list of typed ids to native ids, filtering for the specified type and returning a new list.
	 */
	public static List<String> nativeFromTypedIds(final Collection<String> typedIds, final char filterType) {
		final List<String> list = new ArrayList<String>(typedIds.size());
		
		for (String s : typedIds) {
			final TypedId typedId = fromTypedId(s);
			
			if (typedId.idType == filterType) {
				list.add(typedId.nativeId);
			}
		}
		
		return list;
	}
	
	/**
	 * Converts a list of typed ids to native ids, filtering for the specified type and returning a new list.
	 */
	public static List<String> filterTypedIds(final Collection<String> typedIds, final char filterType) {
		final List<String> list = new ArrayList<String>(typedIds.size());
		
		for (String s : typedIds) {
			final TypedId typedId = fromTypedId(s);
			
			if (typedId.idType == filterType) {
				list.add(typedId.typedId);
			}
		}
		
		return list;
	}
	
	/**
	 * Converts a list of native ids to typed ids, returning a new list.
	 */
	public static List<String> typedFromNativeIds(char type, final Collection<String> nativeIds) {
		final List<String> list = new ArrayList<String>(nativeIds.size());
		
		for (String s : nativeIds) {
			list.add(fromNativeId(type, s).typedId);
		}
		
		return list;
	}
	
	/**
	 * @return the type of id
	 */
	public char getType() {
		return idType;
	}

	/**
	 * @return the native id
	 */
	public String getNativeId() {
		return nativeId;
	}

	/**
	 * @return the typed id
	 */
	public String getTypedId() {
		return typedId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return typedId;
	}


	/**
	 * For clarity we wrap this constructor for access by a static method above.
	 */
	private TypedId(final String id) {
		typedId  = id;
		
		if (id != null && id.length() > 2) {
			nativeId = id.substring(2);
			idType = id.charAt(0);
		} else {
			nativeId = null;
			idType = ACCOUNT;
		}
	}
	
	/**
	 * For clarity we wrap this constructor for access by a static method above.
	 */
	private TypedId(final char type, final String id) {
		idType    = type;
		nativeId  = id;
		typedId   = "" + type + '.' + id;
	}
	
}
