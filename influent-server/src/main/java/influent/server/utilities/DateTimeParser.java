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
package influent.server.utilities;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeUtils;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Temporarily added to address a version issue with Joda DateTime.
 * 
 * @author djonker
 */
public class DateTimeParser {
	
	/**
	 * @see http://joda-time.sourceforge.net/apidocs/org/joda/time/DateTime.html#parse(java.lang.String)
	 */
	public static DateTime parse(String str) {
		if (str == null || str.isEmpty())
			return null;
		
		return ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(str);
	}

	/**
	 * @see http://joda-time.sourceforge.net/apidocs/org/joda/time/Period.html#normalizedStandard()
	 */
	public static Period normalize(Period period) {
		long millis = period.getMillis();
		millis+= period.getSeconds() * DateTimeConstants.MILLIS_PER_SECOND;
		millis+= period.getMinutes() * DateTimeConstants.MILLIS_PER_MINUTE;
		millis+= period.getHours() * DateTimeConstants.MILLIS_PER_HOUR;
		millis+= period.getDays() * DateTimeConstants.MILLIS_PER_DAY;
		millis+= period.getWeeks() * DateTimeConstants.MILLIS_PER_WEEK;

		Period result = new Period(millis, DateTimeUtils.getPeriodType(PeriodType.standard()), ISOChronology.getInstanceUTC());
		int years = period.getYears();
		int months = period.getMonths();
		
		if (years != 0 || months != 0) {
			years = FieldUtils.safeAdd(years, months / 12);
			months = months % 12;
			if (years != 0) {
				result = result.withYears(years);
			}
			if (months != 0) {
				result = result.withMonths(months);
			}
		}
		
		return result;
	}
}
