// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
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
 */
package org.apache.ftpserver.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Standard date related utility methods.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class DateUtils {
    
    private final static String[] MONTHS = {
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
            
    /**
     * Get unix style date string.
     */
    public static String getUnixDate(long millis) {
        if (millis < 0) {
            return "------------";
        }
        
        StringBuffer sb = new StringBuffer(16);
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);
        
        // month
        sb.append( MONTHS[cal.get(Calendar.MONTH)] );
        sb.append(' ');
        
        // day
        int day = cal.get(Calendar.DATE);
        if(day < 10) {
            sb.append(' ');
        }
        sb.append(day);
        sb.append(' ');
        
        long sixMonth = 15811200000L; //183L * 24L * 60L * 60L * 1000L;
        long nowTime = System.currentTimeMillis();
        if( Math.abs(nowTime - millis) > sixMonth) {
            
            // year
            int year = cal.get(Calendar.YEAR);
            sb.append(' ');
            sb.append(year);
        }
        else {
            
            // hour
            int hh = cal.get(Calendar.HOUR_OF_DAY);
            if(hh < 10) {
                sb.append('0');
            }
            sb.append(hh);
            sb.append(':');
            
            // minute
            int mm = cal.get(Calendar.MINUTE);
            if(mm < 10) {
                sb.append('0');
            }
            sb.append(mm);
        }
        return sb.toString();
    }
    
    /**
     * Get ISO 8601 timestamp.
     */
    public static String getISO8601Date(long millis) {
        StringBuffer sb = new StringBuffer(19);
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);
        
        // year
        sb.append(cal.get(Calendar.YEAR));
        
        // month
        sb.append('-');
        int month = cal.get(Calendar.MONTH) + 1;
        if(month < 10) {
            sb.append('0');
        }
        sb.append(month);
        
        // date
        sb.append('-');
        int date = cal.get(Calendar.DATE);
        if(date < 10) {
            sb.append('0');
        }
        sb.append(date);
        
        // hour
        sb.append('T');
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour < 10) {
            sb.append('0');
        }
        sb.append(hour);
        
        // minute
        sb.append(':');
        int min = cal.get(Calendar.MINUTE);
        if(min < 10) {
            sb.append('0');
        }
        sb.append(min);
        
        // second
        sb.append(':');
        int sec = cal.get(Calendar.SECOND);
        if(sec < 10) {
            sb.append('0');
        }
        sb.append(sec);
        
        return sb.toString();
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
