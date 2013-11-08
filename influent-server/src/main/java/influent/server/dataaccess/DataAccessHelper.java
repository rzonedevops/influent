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
package influent.server.dataaccess;

import influent.idl.FL_DateInterval;
import influent.idl.FL_DateRange;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.RandomAccess;

public class DataAccessHelper {

	// Serves no purpose other than for us to identify popup requests down the line when we are looking up entities.
	public interface DetailsSubject {}
	
	public static List<String> detailsSubject(String id) {
		return new DetailsSubjectList(id);
	}
    private static class DetailsSubjectList extends AbstractList<String> implements DetailsSubject, RandomAccess, Serializable {
        static final long serialVersionUID = 3093736618740652951L;
        private final String _id;

        DetailsSubjectList(String id) {
        	_id = id;
        }

        public int size() {
        	return 1;
        }

        public boolean contains(Object obj) {
        	return _id.equals(obj);
        }

        public String get(int index) {
            if (index != 0) throw new IndexOutOfBoundsException("Index: "+index+", Size: 1");
            
            return _id;
        }
    }
    
	public static final String ENTITY_TABLE = "FinEntity";
	public static final String FLOW_TABLE = "FinFlow";
	public static final String GLOBAL_CLUSTER_TABLE = "global_cluster_dataview";
	public static final String DYNAMIC_CLUSTER_TABLE = "dynamic_cluster_dataview";
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public static Date getStartDate(FL_DateRange date) {
		return new Date(date.getStartDate());
	}
	
	public static Date getEndDate(FL_DateRange date) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date.getStartDate());
		switch (date.getDurationPerBin().getInterval()) {
		case SECONDS:
			c.add(Calendar.SECOND, date.getNumBins().intValue());
			break;
		case HOURS:
			c.add(Calendar.HOUR_OF_DAY, date.getNumBins().intValue());
			break;
		case DAYS:
			c.add(Calendar.DATE, date.getNumBins().intValue());
			break;
		case WEEKS:
			c.add(Calendar.DATE, (date.getNumBins().intValue() * 7));
			break;
		case MONTHS:
			c.add(Calendar.MONTH, date.getNumBins().intValue());
			break;
		case QUARTERS:
			c.add(Calendar.MONTH, (date.getNumBins().intValue() *3));
			break;
		case YEARS:
			c.add(Calendar.YEAR, date.getNumBins().intValue());
			break;
		}
		
		return c.getTime();
	}
	
	public static boolean isDateInRange(long date, FL_DateRange range) {
		return getStartDate(range).getTime() <= date && date <= getEndDate(range).getTime();
	}
	
	public static String standardTableName(String tableSeriesName, FL_DateInterval interval) {
		if (interval != null) {
			return tableSeriesName + getIntervalLevel(interval);
		}
		
		return tableSeriesName;
	}

	public static String getIntervalLevel(FL_DateInterval interval) {
		switch (interval) {
		case SECONDS:
			return "By The Second";
		case HOURS:
			return "Hourly";
		case DAYS:
			return "Daily";
		case WEEKS:
			return "Weekly";
		case MONTHS:
			return "Monthly";
		case QUARTERS:
			return "Quarterly";
		case YEARS:
			return "Yearly";
		}
		return "Yearly";
	}
	
	public static String createNodeIdListFromCollection(List<String> nodeIDs, boolean usePrefix, boolean trimDash) {
		if (nodeIDs == null || nodeIDs.isEmpty()) return null;
	
		StringBuilder resultString = new StringBuilder();
		for (String id : nodeIDs) {
			resultString.append("'" + id + "', ");
		}
		resultString.replace(
			resultString.lastIndexOf(","),
			resultString.length() - 1,
			""
		);
		return resultString.toString();
	}

	
	public static String createInClause(Collection<String> inItems) {
		StringBuilder resultString = new StringBuilder();
		resultString.append("(");
		
		for (String item : inItems) {
			resultString.append("'" + item + "',");
		}
		if (!inItems.isEmpty()) {
			resultString.replace(
				resultString.lastIndexOf(","),
				resultString.length(),
				")"
			);
		}
		else {
			resultString.append(")");
		}
		return resultString.toString();
	}
}
