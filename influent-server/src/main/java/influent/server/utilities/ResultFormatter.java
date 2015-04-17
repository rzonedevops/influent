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

import influent.idl.FL_Duration;

import java.text.DecimalFormat;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class ResultFormatter {
	
	public static DecimalFormat us_df = new DecimalFormat("$#,##0.00;$-#,##0.00");
	public static DecimalFormat world_df = new DecimalFormat("#,##0.00;-#,##0.00");
	public static DecimalFormat world_if = new DecimalFormat("#,##0;-#,##0");
	public static DateTimeFormatter date_formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	public static String formatCur(Number d, boolean isUSD) { return d == null ? "-" : isUSD? us_df.format(d) : world_df.format(d); }
	public static String formatCount(Number d) { return d == null ? "-" : world_if.format(d); }

	public static String formatDur(FL_Duration d) { 
		if (d == null) return "";
		
		int t = d.getNumIntervals().intValue();
		if (t == 0) return "-";
		
		ReadablePeriod period = null;
		switch (d.getInterval()) {
		case SECONDS:
			period = Seconds.seconds(t);
			break;
		case HOURS:
			period = Hours.hours(t);
			break;
		case DAYS:
			period = Days.days(t);
			break;
		case WEEKS:
			period = Weeks.weeks(t);
			break;
		case MONTHS:
			period = Months.months(t);
			break;
		case QUARTERS:
			period = Months.months(t*3);
			break;
		case YEARS:
			period = Years.years(t);
			break;
		}
		
		PeriodFormatter formatter = new PeriodFormatterBuilder()
	        .printZeroAlways()
	        .minimumPrintedDigits(2)
	        .appendHours()
	        .appendSeparator(":")
	        .printZeroAlways()
	        .minimumPrintedDigits(2)
	        .appendMinutes()
	        .appendSeparator(":")
	        .printZeroAlways()
	        .minimumPrintedDigits(2)
	        .appendSeconds()
	        .toFormatter();
		final String ftime = formatter.print(DateTimeParser.normalize(new Period(period)));

		return ftime;
	}

}
