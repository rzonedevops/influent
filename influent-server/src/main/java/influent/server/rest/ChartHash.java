package influent.server.rest;

import influent.idl.FL_Cluster;
import influent.server.clustering.utils.ClusterContextCache;
import influent.server.clustering.utils.ContextRead;
import influent.server.clustering.utils.ClusterContextCache.PermitSet;
import influent.server.utilities.DateTimeParser;
import influent.server.utilities.TypedId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

public class ChartHash {
	List<String> ids;
	DateTime startDate; 
	DateTime endDate; 
	List<String> focusIds;
	Double focusMaxDebitCredit;
	Integer numBuckets;
	Integer width;
	Integer height;
	String id;
	String contextId;
	String focusContextId;
	String sessionId;
	ClusterContextCache contextCache;

	private String _hash;
	private String _longHash;
	
	private static final String SEPARATOR 		= "|";
	private static final String ID_SECTION 		= SEPARATOR + ":I:";
	private static final String FOCUS_ID 		= ":F:";
	private static final String UUID_SEPARATOR	= ":U:";

	ChartHash(String id, 
			  List<String> ids, 
			  DateTime startDate, 
			  DateTime endDate, 
			  List<String> focusIds, 
			  Double focusMaxDebitCredit, 
			  Integer numBuckets, 
			  Integer width, 
			  Integer height, 
			  String contextId, 
			  String focusContextId, 
			  String sessionId, 
			  ClusterContextCache contextCache) {
		
		this.id = id;
		this.ids = ids;
		this.startDate = startDate;
		this.endDate = endDate;
		this.focusIds = focusIds;
		this.focusMaxDebitCredit = focusMaxDebitCredit;
		this.numBuckets = numBuckets;
		this.width = width;
		this.height = height;
		this.contextId = contextId;
		this.focusContextId = focusContextId;
		this.sessionId = sessionId;
		this.contextCache = contextCache;
		
		this._hash = makeHash();
		this._longHash = makeLongHash();
	}
	
	ChartHash(String hash, ClusterContextCache contextCache) {
		this.setHash(hash);
		this.contextCache = contextCache;
		parseHash();
	}

	public String getLongHash() {
		return _longHash;
	}

	public String getHash() {
		return _hash;
	}

	public void setHash(String hash) {
		this._hash = hash;
	}

	public void setLongHash(String longHash) {
		this._longHash = longHash;
	}	
	
	public String makeHash() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(sessionId+SEPARATOR);
		buffer.append(contextId+SEPARATOR);
		buffer.append(focusContextId+SEPARATOR);
		buffer.append(startDate.toString() + SEPARATOR);
		buffer.append(endDate.toString() + SEPARATOR);
		buffer.append(focusMaxDebitCredit + SEPARATOR);
		buffer.append(numBuckets.toString() + SEPARATOR);
		buffer.append(width.toString() + SEPARATOR);
		buffer.append(height.toString() + SEPARATOR);
		buffer.append(id.replaceAll(" ", "%20"));					// URL hash CANNOT have spaces!!
		buffer.append(ID_SECTION); 									// Define where ids start 
		
		for (String id : ids) {
			buffer.append(SEPARATOR + id.replaceAll(" ", "%20"));
			
			TypedId tId = TypedId.fromTypedId(id);
			
			if (tId.getType() == TypedId.FILE) {
				buffer.append(UUID_SEPARATOR + UUID.randomUUID());
			}	
		}
		
		for (String id : focusIds) {
			buffer.append(SEPARATOR + FOCUS_ID + id.replaceAll(" ", "%20"));
			
			TypedId tId = TypedId.fromTypedId(id);
			
			if (tId.getType() == TypedId.FILE) {
				buffer.append(UUID_SEPARATOR + UUID.randomUUID());
			}	
		}
		
		_hash = buffer.toString();
		return getHash();
	}
	
	public String makeLongHash() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(sessionId+SEPARATOR);
		buffer.append(contextId+SEPARATOR);
		buffer.append(focusContextId+SEPARATOR);
		buffer.append(startDate.toString() + SEPARATOR);
		buffer.append(endDate.toString() + SEPARATOR);
		buffer.append(focusMaxDebitCredit + SEPARATOR);
		buffer.append(numBuckets.toString() + SEPARATOR);
		buffer.append(width.toString() + SEPARATOR);
		buffer.append(height.toString() + SEPARATOR);
		buffer.append(id.replaceAll(" ", "%20"));					// URL hash CANNOT have spaces!!
		buffer.append(ID_SECTION); 									// Define where ids start 

		final PermitSet permits = new PermitSet();
		
		for (String id : ids) {
			buffer.append(SEPARATOR + id.replaceAll(" ", "%20"));
			
			TypedId tId = TypedId.fromTypedId(id);
			if (tId.getType() == TypedId.FILE) {
				try {
					final ContextRead entityContext = contextCache.getReadOnly(contextId, permits);

					if (entityContext != null) {
						FL_Cluster flcluster = entityContext.getFile(id);
						
						if (flcluster != null) {
							for (String c : flcluster.getSubclusters()) {
								buffer.append(c);
							}
							for (String m : flcluster.getMembers()) {
								buffer.append(m);
							}
						}
					}
				} finally {
					permits.revoke();
				}
			}			
		}
		
		for (String id : focusIds) {
			buffer.append(SEPARATOR + FOCUS_ID + id.replaceAll(" ", "%20"));
			
			TypedId tId = TypedId.fromTypedId(id);
			if (tId.getType() == TypedId.FILE) {
				try {
					final ContextRead entityContext = contextCache.getReadOnly(contextId, permits);

					if (entityContext != null) {
						FL_Cluster flcluster = entityContext.getFile(id);
						
						if (flcluster != null) {
							for (String c : flcluster.getSubclusters()) {
								buffer.append(c);
							}
							for (String m : flcluster.getMembers()) {
								buffer.append(m);
							}
						}
					}
				} finally {
					permits.revoke();
				}
			}	
		}
		
		_longHash = buffer.toString();
		return getLongHash();
	}
	
	public void parseHash() {
		final int numParts = 10;
		String[] hashParts = _hash.split("\\" + SEPARATOR, numParts);
		
		if (hashParts.length < numParts) {
			throw new AssertionError("Chart data hash is not of the right format");
		}
		sessionId = hashParts[0];
		contextId = hashParts[1];
		focusContextId = hashParts[2];
		startDate = DateTimeParser.parse(hashParts[3]);
		endDate = DateTimeParser.parse(hashParts[4]);
		focusMaxDebitCredit = (hashParts[5].compareToIgnoreCase("null") == 0) ? null : Double.parseDouble(hashParts[5]);
		numBuckets = Integer.parseInt(hashParts[6]);
		width = Integer.parseInt(hashParts[7]);
		height = Integer.parseInt(hashParts[8]);
		
		// Find where the id section starts
		final int idSectionIdx = hashParts[9].indexOf(ID_SECTION);						
		
		// the id is everything before the id section
		// Restore any encoded spaces
		id = hashParts[9].substring(0, idSectionIdx).replaceAll("%20", " "); 			
		
		// Split everything after the id section
		String[] idParts = hashParts[9].substring(idSectionIdx + ID_SECTION.length() + SEPARATOR.length()).split("\\" + SEPARATOR);
		
		final PermitSet permits = new PermitSet();
		
		ids = new ArrayList<String>();
		focusIds = new ArrayList<String>();
		for (int i = 0; i < idParts.length; i++) {
			
			String id = idParts[i].replaceAll("%20", " ");

			// If there's a UUID, discard it.
			final int uuidIdx = id.indexOf(UUID_SEPARATOR);
			if (uuidIdx != -1)
				id = id.substring(0, uuidIdx);
			
			boolean isFocus = id.startsWith(FOCUS_ID);
			
			// Check to see if this id belongs to a mutable cluster and if so, expand it, but don't add the file itself.
			TypedId tId = TypedId.fromTypedId(id);
			if (tId.getType() == TypedId.FILE) {
				try {
					final ContextRead entityContext = contextCache.getReadOnly(isFocus ? focusContextId : contextId, permits);

					if (entityContext != null) {
						FL_Cluster flcluster = entityContext.getFile(id);
						
						if (flcluster != null) {
							(isFocus ? focusIds : ids).addAll(flcluster.getSubclusters());
							(isFocus ? focusIds : ids).addAll(flcluster.getMembers());
						}
					}
				} finally {
					permits.revoke();
				}
			} else {
				if (isFocus) {
					id = id.substring(FOCUS_ID.length());
					focusIds.add(id);
				} else {
					ids.add(id);
				}
			}
		}
	}
}
