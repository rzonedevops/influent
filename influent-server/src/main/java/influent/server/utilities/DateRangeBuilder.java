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

import influent.idl.FL_DateInterval;
import influent.idl.FL_DateRange;
import influent.idlhelper.DateRangeHelper;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Months;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.Years;

public class DateRangeBuilder {
	
	public static FL_DateRange getDateRange(DateTime startDate, DateTime endDate) {
		Days days = Days.daysBetween(startDate, endDate);
		if (days.getDays() == 14) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.DAYS, 14L); }
		if (days.getDays() == 30) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.DAYS, 15L, 2); }
		if (days.getDays() == 60) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.DAYS, 15L, 4); }
		Weeks weeks = Weeks.weeksBetween(startDate, endDate);
		if (weeks.getWeeks() == 16) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.WEEKS, 16L); }
		if (weeks.getWeeks() == 32) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.WEEKS, 16L, 2); }
		Months months = Months.monthsBetween(startDate, endDate);
		if (months.getMonths() == 12) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.MONTHS, 12L); }
		if (months.getMonths() == 16) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.MONTHS, 16L); }
		if (months.getMonths() == 24) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.MONTHS, 16L, 2); }
		if (months.getMonths() == 32) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.MONTHS, 12L); }
		Years years = Years.yearsBetween(startDate, endDate);
		if (years.getYears() == 4) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.QUARTERS, 16L); }
		if (years.getYears() == 8) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.QUARTERS, 16L, 2); }
		if (years.getYears() == 16) { return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.YEARS, 16L); }

		return new DateRangeHelper(startDate.getMillis(), FL_DateInterval.DAYS, 15L, 1);
	}

	public static int determineInterval(DateTime date, DateTime startDate, FL_DateInterval interval, int numIntervalsPerBin) {
		switch (interval) {
		case SECONDS:
			Seconds seconds = Seconds.secondsBetween(startDate, date);
			return seconds.getSeconds()/numIntervalsPerBin;
		case HOURS:
			Hours hours = Hours.hoursBetween(startDate, date);
			return hours.getHours()/numIntervalsPerBin;
		case DAYS:
			Days days = Days.daysBetween(startDate, date);
			return days.getDays()/numIntervalsPerBin;
		case WEEKS:
			Weeks weeks = Weeks.weeksBetween(startDate, date);
			return weeks.getWeeks()/numIntervalsPerBin;
		case MONTHS:
			Months months = Months.monthsBetween(startDate, date);
			return months.getMonths()/numIntervalsPerBin;
		case QUARTERS:
			months = Months.monthsBetween(startDate, date);
			return months.getMonths()/3/numIntervalsPerBin;
		case YEARS:
			Years years = Years.yearsBetween(startDate, date);
			return years.getYears()/numIntervalsPerBin;
		}
		return 0;
	}
}
