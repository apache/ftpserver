/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1997-2003 The Apache Software Foundation. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software
 *    itself, if and wherever such third-party acknowledgments
 *    normally appear.
 *
 * 4. The names "Incubator", "FtpServer", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
