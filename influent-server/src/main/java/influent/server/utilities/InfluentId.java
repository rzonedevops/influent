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
public final class InfluentId {

	/**
	 * The account id type.
	 */
	public final static char ACCOUNT = 'a';
	
	/**
	 * The account owner id type.
	 */
	public final static char ACCOUNT_OWNER = 'o';
	
	/**
	 * The cluster id type.
	 */
	public final static char CLUSTER = 'c';
	
	/**
	 * The cluster summary id type.
	 */
	public final static char CLUSTER_SUMMARY = 's';

	/**
	 * The file id type.
	 */
	public final static char FILE = 'f';

	/**
	 * The file id type.
	 */
	public final static char LINK = 'l';
	
	
	private final char idClass;
	private final String idType;
	private final String nativeId;
	private final String influentId;
	private final String typedId;
	
	
	
	/**
	 * Constructs from a native id, class, and type.
	 */
	public static InfluentId fromNativeId(final char idClass, final String idType, final String nativeId) {
		return new InfluentId(idClass, idType, nativeId);
	}




	/**
	 * Constructs from a typed id and class.
	 */
	public static InfluentId fromTypedId(final char idClass, final String typedId) {
		String[] splitTypedId = typedId.split("\\.", 2);
		if (splitTypedId.length < 2) {
			return new InfluentId(idClass, null, typedId);
		}

		return new InfluentId(idClass, splitTypedId[0], splitTypedId[1]);
	}
	
	
	
	
	/**
	 * Constructs from an influent id.
	 */
	public static InfluentId fromInfluentId(final String influentId) {
		return new InfluentId(influentId);
	}
	
	
	
	
	/**
	 * Converts a list of influent ids to native ids, returning a new list.
	 */
	public static List<String> nativeFromInfluentIds(final Collection<String> influentIds) {
		final List<String> list = new ArrayList<String>(influentIds.size());
		
		for (String s : influentIds) {
			list.add(fromInfluentId(s).nativeId);
		}
		
		return list;
	}
	
	
	
	
	/**
	 * Converts a list of influent ids to native ids, filtering for the specified class and returning a new list.
	 */
	public static List<String> nativeFromInfluentIds(final Collection<String> influentIds, final char filterClass) {
		final List<String> list = new ArrayList<String>(influentIds.size());
		
		for (String s : influentIds) {
			final InfluentId influentId = fromInfluentId(s);
			
			if (influentId.idClass == filterClass) {
				list.add(influentId.nativeId);
			}
		}
		
		return list;
	}




	/**
	 * Converts a list of influent ids to typed ids, returning a new list.
	 */
	public static List<String> typedFromInfluentIds(final Collection<String> influentIds) {
		final List<String> list = new ArrayList<String>(influentIds.size());

		for (String s : influentIds) {
			list.add(fromInfluentId(s).typedId);
		}

		return list;
	}




	/**
	 * Converts a list of influent ids to typed ids, filtering for the specified class and returning a new list.
	 */
	public static List<String> typedFromInfluentIds(final Collection<String> influentIds, final char filterClass) {
		final List<String> list = new ArrayList<String>(influentIds.size());

		for (String s : influentIds) {
			final InfluentId influentId = fromInfluentId(s);

			if (influentId.idClass == filterClass) {
				list.add(influentId.typedId);
			}
		}

		return list;
	}
	
	
	
	
	/**
	 * Filters a list of influent ids for the specified class and returning a new list.
	 */
	public static List<String> filterInfluentIds(final Collection<String> influentIds, final char filterClass) {
		final List<String> list = new ArrayList<String>(influentIds.size());
		
		for (String s : influentIds) {
			final InfluentId influentId = fromInfluentId(s);
			
			if (influentId.idClass == filterClass) {
				list.add(influentId.influentId);
			}
		}
		
		return list;
	}




	/**
	 * Filters a list of typed ids for the specified class and returning a new list.
	 */
	public static List<String> filterTypedIds(final Collection<String> influentIds, final char filterClass) {
		final List<String> list = new ArrayList<String>(influentIds.size());

		for (String s : influentIds) {
			final InfluentId influentId = fromInfluentId(s);

			if (influentId.idClass == filterClass) {
				list.add(influentId.typedId);
			}
		}

		return list;
	}
	
	
	
	
	/**
	 * Converts a list of native ids to influent ids, returning a new list.
	 */
	public static List<String> influentFromNativeIds(char idClass, String idType, final Collection<String> nativeIds) {
		final List<String> list = new ArrayList<String>(nativeIds.size());
		
		for (String s : nativeIds) {
			list.add(fromNativeId(idClass, idType, s).influentId);
		}
		
		return list;
	}




	/**
	 * Converts a list of native ids to typed ids, returning a new list.
	 */
	public static List<String> typedFromNativeIds(String idType, final Collection<String> nativeIds) {
		final List<String> list = new ArrayList<String>(nativeIds.size());

		for (String s : nativeIds) {
			list.add(fromNativeId(InfluentId.ACCOUNT, idType, s).typedId);
		}

		return list;
	}


	
	
	/**
	 * Return whether the typed id has the specified type
	 */
	public static boolean hasIdClass(String influentId, char type) {
		return (fromInfluentId(influentId).getIdClass() == type);
	}
	
	
	
	/**
	 * @return the class of id
	 */
	public char getIdClass() {
		return idClass;
	}
	
	
	
	
	/**
	 * @return the native id
	 */
	public String getNativeId() {
		return nativeId;
	}





	/**
	 * @return the native id
	 */
	public String getTypedId() {
		return typedId;
	}




	
	/**
	 * @return the influent id
	 */
	public String getInfluentId() {
		return influentId;
	}
	
	
	
	
	/**
	 * @return the id type
	 */
	public String getIdType() {
		return idType;
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return influentId;
	}
	
	
	
	
	/**
	 * For clarity we wrap this constructor for access by a static method above.
	 */
	private InfluentId(final String id) {

		if (id != null && id.length() > 2) {
			String[] splits = id.split("\\.", 3);
			
			if (splits.length == 1) {
				this.nativeId = splits[0];
				this.idType = null;
				this.idClass = ACCOUNT;
				this.typedId = null;
				this.influentId = null;
				return;
			}
			
			if (splits.length > 0) {
				this.idClass = splits[0].charAt(0);
			} else {
				this.idClass = ACCOUNT;
			}
			
			if (splits.length > 1 && !splits[1].equalsIgnoreCase("null")) {
				this.idType = splits[1];
			} else {
				this.idType = null;
			}
			
			if (splits.length > 2 && !splits[2].equalsIgnoreCase("null")) {
				this.nativeId = splits[2];
			} else {
				this.nativeId = null;
			}

			if (this.idType != null && this.nativeId != null) {
				this.typedId = this.idType + "." + this.nativeId;
				this.influentId = this.idClass + "." + this.idType + "." + this.nativeId;
			} else {
				this.typedId = null;
				this.influentId = null;
			}

		} else {
			this.nativeId = null;
			this.idType = null;
			this.influentId  = null;
			this.typedId = null;
			this.idClass = ACCOUNT;
		}
	}
	
	
	
	
	/**
	 * For clarity we wrap this constructor for access by a static method above.
	 */
	private InfluentId(final char idClass, final String idType, final String nativeId) {
		this.idClass = idClass;
		this.idType = idType;
		this.nativeId  = nativeId;
		this.influentId = "" + idClass + "." + idType + "." + nativeId;
		this.typedId = "" + idType + "." + nativeId;
	}
}
