package influent.server.data;

import influent.idl.FL_Constraint;
import influent.idl.FL_PropertyMatchDescriptor;
import influent.idl.FL_PropertyType;
import influent.idlhelper.SingletonRangeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntitySearchTerms {

	private String extraTerms = "";
	private List<FL_PropertyMatchDescriptor> terms = new ArrayList<FL_PropertyMatchDescriptor>();
	private Boolean doCluster = null;
	
	
	
	public EntitySearchTerms(String term) {
		Pattern extraTermRegEx = Pattern.compile("\\A([^:]*)(\\s*$| [^:\\s]+:.*)");
		Pattern tagsRegEx = Pattern.compile("([^:\\s]+):([^:]*)( |$)");
		
		Matcher extraTermMatcher = extraTermRegEx.matcher(term);
		StringBuilder extraTermsBuilder = new StringBuilder();
		while (extraTermMatcher.find()) {
			extraTermsBuilder.append(extraTermMatcher.group(1).toString().trim());
		}
		
		extraTerms = extraTermsBuilder.toString().trim();
		
		Matcher tagsMatcher = tagsRegEx.matcher(term.trim());
		while (tagsMatcher.find()) {
			
			String tagName = tagsMatcher.group(1).toString().trim();
			
			if (tagName.equalsIgnoreCase("cluster")) {
				String clusterBoolString = tagsMatcher.group(2).toString().trim();
				if (clusterBoolString != null) {
					doCluster = (clusterBoolString.equalsIgnoreCase("true") || clusterBoolString.equalsIgnoreCase("yes"));
				}
				
				continue;
			}
			
			String[] values = tagsMatcher.group(2).toString().trim().split(",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))");
			for (String val : values) {
				val = val.trim();
				
				FL_PropertyMatchDescriptor.Builder termBuilder = FL_PropertyMatchDescriptor.newBuilder();
				termBuilder.setKey(tagName);
				
				if (val.startsWith("-")) {
					val = val.substring(1);
					termBuilder.setConstraint(FL_Constraint.NOT);
				} else if (val.startsWith("\"") && val.endsWith("\"")) {
					val = val.substring(1, val.length() - 1);
					termBuilder.setConstraint(FL_Constraint.REQUIRED_EQUALS);
				} else {
					termBuilder.setConstraint(FL_Constraint.FUZZY_PARTIAL_OPTIONAL);
				}
				
				termBuilder.setRange(new SingletonRangeHelper(val, FL_PropertyType.STRING));
				
				terms.add(termBuilder.build());	
			}
		}
	}
	
	
	
	
	public String getExtraTerms() {
		return extraTerms;
	}
	
	
	
	
	public List<FL_PropertyMatchDescriptor> getTerms() {
		return terms;
	}
	
	
	
	
	public Boolean doCluster() {
		return doCluster;
	}
}
