/* ====================================================================
 * Copyright 2002 - 2004
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * $Id$
 */
package org.apache.ftpserver.util;

import java.util.*;
import java.text.*;

/**
 * This is a timezone conversion utility class.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */

public class DateUtils {

    private static final String[] MONTHS = {
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    };

    // as SimpleDateFormat is not thread-safe - we have to use ThreadLocal
    private final static ThreadLocal AFTER_SIX = new ThreadLocal() {
        protected Object initialValue() {
            return new SimpleDateFormat(" yyyy");
        }
    };
    private final static ThreadLocal BEFORE_SIX = new ThreadLocal() {
        protected Object initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };


    /**
     * Get unix style date string.
     */
    public static String getUnixDate(Date date) {
        long dateTime = date.getTime();
        if (dateTime < 0) {
            return "------------";
        }

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        String firstPart = MONTHS[cal.get(Calendar.MONTH)] + ' ';

        String dateStr = String.valueOf(cal.get(Calendar.DATE));
        if (dateStr.length() == 1) {
            dateStr = ' ' + dateStr;
        }
        firstPart += dateStr + ' ';

        long nowTime = System.currentTimeMillis();
        if ( Math.abs(nowTime - dateTime) > 183L * 24L * 60L * 60L * 1000L) {
            DateFormat fmt = (DateFormat)AFTER_SIX.get();
            return firstPart + fmt.format(date);
        }
        else {
            DateFormat fmt = (DateFormat)BEFORE_SIX.get();
            return firstPart + fmt.format(date);
        }
    }

    /**
     * Get the timezone specific string.
     */
    public static String getString(Date dt, DateFormat df, TimeZone to) {
        df.setTimeZone(to);
        return df.format(dt);
    }

    /**
     * Get the timezone specific calendar.
     */
    public static Calendar getCalendar(Date dt, TimeZone to) {
        Calendar cal = Calendar.getInstance(to);
        cal.setTime(dt);
        return cal;
    }

    /**
     * Get date object.
     */
    public static Date getDate(String str, DateFormat df, TimeZone from)
    throws java.text.ParseException {
        df.setTimeZone(from);
        return df.parse(str);
    }

    /**
     * Get date difference => d1 - d2.
     */
    public static String getDifference(Date d1, Date d2) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(d2);
        int year2 = calendar.get(Calendar.YEAR);
        int day2 = calendar.get(Calendar.DAY_OF_YEAR);
        int hour2 = calendar.get(Calendar.HOUR_OF_DAY);
        int min2  = calendar.get(Calendar.MINUTE);

        calendar.setTime(d1);
        int year1 = calendar.get(Calendar.YEAR);
        int day1 = calendar.get(Calendar.DAY_OF_YEAR);
        int hour1 = calendar.get(Calendar.HOUR_OF_DAY);
        int min1  = calendar.get(Calendar.MINUTE);

        int leftDays = (day1-day2)+(year1-year2)*365;
        int leftHours = hour1-hour2;
        int leftMins  = min1 - min2;

        if(leftMins < 0) {
            leftMins += 60;
            --leftHours;
        }
        if(leftHours < 0) {
            leftHours += 24;
            --leftDays;
        }

        String interval = "";
        if(leftDays > 0) {
            interval = leftDays + " Days";
        }
        else if((leftHours > 0) && (leftDays == 0)) {
            interval = leftHours + " Hours";
        }
        else if((leftMins > 0) && (leftHours == 0) && (leftDays == 0)) {
            interval = leftMins + " Minutes";
        }
        else {
            interval = "";
        }
        return interval;
    }


}
