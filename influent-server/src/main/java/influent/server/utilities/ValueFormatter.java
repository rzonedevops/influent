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

import influent.idl.FL_BoundedRange;
import influent.idl.FL_DistributionRange;
import influent.idl.FL_Frequency;
import influent.idl.FL_GeoData;
import influent.idl.FL_ListRange;
import influent.idl.FL_Property;
import influent.idl.FL_PropertyTag;
import influent.idl.FL_SingletonRange;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ValueFormatter {
	
	private static String NULLSTR = "Unspecified";
	private static int DIST_SAMPLE_SIZE = 3;
	
	private static interface Formatter {
		public String format(Object val);
	}

	private static Formatter getFormatter(FL_Property property) {
		if (property.getTags().contains(FL_PropertyTag.GEO)) {
			return new Formatter() {
				@Override
				public String format(Object val) {
					if (val instanceof FL_GeoData) {			
						return ((FL_GeoData)val).getCc();
					}
					if (val == null || val.toString().isEmpty()) {
						return NULLSTR;
					}
					return val.toString();
				}
			};
		} else if (property.getTags().contains(FL_PropertyTag.DATE)) {
			return new Formatter() {
				final DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMMMMMM dd, yyyy").withZoneUTC();
				
				@Override
				public String format(Object val) {
					if (val instanceof Long) {
						return fmt.print((Long)val);
					}
					if (val == null || val.toString().isEmpty()) {
						return NULLSTR;
					}
					return val.toString();
				}
			};
		} else {
			return new Formatter() {
				@Override
				public String format(Object val) {
					if (val == null || val.toString().isEmpty()) {
						return NULLSTR;
					}
					return val.toString();
				}
			};
		}

	}
	
	public static String format(FL_Property property) {
		if (property != null) {
			Formatter fmt= getFormatter(property);
			
			Object range = property.getRange();
			
			if (range != null) {
				if (range instanceof FL_SingletonRange) {
					return fmt.format(((FL_SingletonRange)range).getValue());
				}
				
				StringBuilder sb = new StringBuilder();
				
				if (range instanceof FL_BoundedRange) {
					FL_BoundedRange bounded = (FL_BoundedRange) range;
					
					sb.append(fmt.format(bounded.getStart()));
					sb.append(" to ");
					sb.append(fmt.format(bounded.getEnd()));
					
				} else if (range instanceof FL_ListRange) {
					FL_ListRange list = (FL_ListRange) range;
					
					if (list.getValues() != null) {
						for (Object val : list.getValues()) {
							if (sb.length() != 0) {
								sb.append(", ");
							}
							sb.append(fmt.format(val));
						}
					}
					
				} else if (range instanceof FL_DistributionRange) {
					FL_DistributionRange dist = (FL_DistributionRange) range;
					
					int samplesize = 0;
					
					for (FL_Frequency freq: dist.getDistribution()) {
						if (sb.length() != 0) {
							if (samplesize == DIST_SAMPLE_SIZE) {
								sb.append("...");
								break;
							}
							sb.append(", ");
						}
						samplesize++;
						
						// TO DO: this doesn't handle distributions of bounded or list
						sb.append(fmt.format(freq.getRange()));
					}
				}
				
				return sb.toString();
			}
		}
		
		return NULLSTR;
	}
	
}
