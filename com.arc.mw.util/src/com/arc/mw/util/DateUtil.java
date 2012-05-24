/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.mw.util;


import java.util.Calendar;


/**
 * This class returns Date and Time information by using Java's Calendar class. Each call to Init() creates a new
 * Calendar object which takes a snapshot of the current date/time. It is recommended that you call Init() once first
 * before using the DateUtil class to simplify things.
 * @author Hurai Rody
 * @currentOwner <a href="mailto:hurai.rody@arc.com">Hurai Rody</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class DateUtil {

    public static int FORMAT_MM_DD_YYYY = 0;

    private static Calendar mCalendar = null;

    private static String[] months = new String[] {
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December" };

    /**
     * This method creates a new Calendar object
     */
    public static void Init () {
        mCalendar = Calendar.getInstance();
    }

    /**
     * Returns the current Year as a string (e.g. 2003).
     * @return the current year as a string.
     */
    public static String getYear (boolean fourDigit) {
        if (mCalendar == null)
            Init();
        String year = mCalendar.get(Calendar.YEAR) + "";
        if (year.length() == 4 && fourDigit)
            return year;
        return year.substring(2);
    }

    /**
     * Returns either the name or the month number depending on the name parameter specified. N.B. If the month number
     * is returned, it will be a digit from 1-12.
     * @param name if true returns the digit, otherwise returns the name of the month.
     * @return either the name or the month number depending on the parameter specified.
     */
    public static String getMonth (boolean name) {
        if (mCalendar == null)
            Init();
        if (name)
            return months[mCalendar.get(Calendar.MONTH)];
        else {
            String retVal = null;
            int val = mCalendar.get(Calendar.MONTH) + 1;
            if (val < 10)
                retVal = "0" + val;
            else
                retVal = val + "";
            return retVal;
        }
    }

    /**
     * Returns the current day as a string, numbers will be in the range from 1-31.
     * @return the current day.
     */
    public static String getDay () {
        if (mCalendar == null)
            Init();
        String retVal = null;
        int val = mCalendar.get(Calendar.DAY_OF_MONTH);
        if (val < 10)
            retVal = "0" + val;
        else
            retVal = val + "";
        return retVal;
    }

    /**
     * Returns the current minute as a string (range is 0-59).
     * @return the current minute.
     */
    public static String getMinute () {
        if (mCalendar == null)
            Init();
        String retVal = null;
        int val = mCalendar.get(Calendar.MINUTE);
        if (val < 10)
            retVal = "0" + val;
        else
            retVal = val + "";
        return retVal;
    }

    /**
     * Returns the current Second as a string (range is 0-59).
     * @return the current second.
     */
    public static String getSecond () {
        if (mCalendar == null)
            Init();
        String retVal = null;
        int val = mCalendar.get(Calendar.SECOND);
        if (val < 10)
            retVal = "0" + val;
        else
            retVal = val + "";
        return retVal;
    }

    /**
     * Returns the current hour as a string. Option specifies whether to use military time. Range is from 1-24.
     * @return the current hour.
     */
    public static String getHour (boolean military) {
        if (mCalendar == null)
            Init();
        String retVal = null;
        int val = mCalendar.get(Calendar.HOUR_OF_DAY);
        if (military) {
            if (val < 10)
                retVal = "0" + val;
            else
                retVal = val + "";
        }
        else {
            if (val > 12)
                val -= 12;
            if (val < 10)
                retVal = "0" + val;
            else
                retVal = val + "";
        }
        return retVal;
    }

    /**
     * Returns "AM" or "PM" depending on the current time.
     * @return AM or PM designation.
     */
    public static String getAMPM () {
        if (mCalendar == null)
            Init();
        return (mCalendar.get(Calendar.AM_PM) == 0 ? "AM" : "PM");
    }

    /**
     * Returns the current date and time. Using the verbose option will return a user-friendly version (e.g. "May 16,
     * 2003 18:10:20"). Whereas not using the verbose option will return one long string (e.g. 20030516181020).
     * @param verbose if true, the date is returned in verbose format, otherwise in short format.
     * @return today's date and time.
     */
    public static String getTodaysDate (boolean verbose, boolean includeSeconds) {
        Init();
        String retVal = null;

        String year = getYear(true);
        String day = getDay();
        String minute = getMinute();
        String second = getSecond();
        String amPm = getAMPM();
        if (verbose) {
            String month = getMonth(true);
            String hour = getHour(false);
            retVal = month + " " + day + ", " + year + " " + hour + ":" + minute;
            if (includeSeconds)
                retVal += ":" + second;
            retVal += " " + amPm;
        }
        else {
            String month = getMonth(false);
            String hour = getHour(true);
            retVal = year + month + day + hour + minute;
            if (includeSeconds)
                retVal += second;
        }
        return retVal;
    }

    /**
     * Reads a particular date format then translates it to another format. The only format implemented so far is
     * "MM-DD-YYYY" which can have anything as a delimiter. It also parses based on the delimiter, so it can really be
     * M-D-YY even.
     * @param date the date string
     * @param format the date format
     * @return the translated string
     */
    public static String formatDate (String date, int format) {
        String retVal = null;

        if (date == null || date.length() == 0)
            return "unknown";

        String[] strs = date.split("-");
        if (strs.length != 3)
            return "unknown";
        int month = Integer.parseInt(strs[0]);
        int day = Integer.parseInt(strs[1]);
        int year = Integer.parseInt(strs[2]);
        retVal = months[month - 1] + " " + day + ", " + year;
        return retVal;
    }

    /**
     * Returns a verbose date string such as "December 22, 2003 01:23:54" based on the longDate paramater.
     * @param longDate a date string represented by yyyymmddhhss (e.g. 200403251003).
     * @return a formatted date String.
     */
    public static String longDateToVerbose (String longDate) {
        String retVal = null;

        int year = Integer.parseInt(longDate.substring(0, 4));
        int month = Integer.parseInt(longDate.substring(4, 6)) - 1;
        int day = Integer.parseInt(longDate.substring(6, 8));
        int hour = Integer.parseInt(longDate.substring(8, 10));
        int minute = Integer.parseInt(longDate.substring(10, 12));
        int second = Integer.parseInt(longDate.substring(12, 14));
        mCalendar = Calendar.getInstance();
        mCalendar.set(year, month, day, hour, minute, second);

        String yearStr = getYear(true);
        String dayStr = getDay();
        String minuteStr = getMinute();
        String secondStr = getSecond();
        String amPm = getAMPM();
        String monthStr = getMonth(true);
        String hourStr = getHour(false);
        retVal = monthStr + " " + dayStr + ", " + yearStr + " " + hourStr + ":" + minuteStr;
        retVal += ":" + secondStr;
        retVal += " " + amPm;

        return retVal;
    }

    /**
     * Returns a date String such as "Jan 23, 2004 13:04" based on the parameter longDate.
     * @param longDate a date string represented by yyyymmddhhss (e.g. 200403251003).
     * @param includeSeconds include the seconds parameter.
     * @return a formatted date String.
     */
    public static String longDateToVerbose (String longDate, boolean includeSeconds) {
        String retVal = null;
        int year = Integer.parseInt(longDate.substring(0, 4));
        int month = Integer.parseInt(longDate.substring(4, 6)) - 1;
        int day = Integer.parseInt(longDate.substring(6, 8));
        int hour = Integer.parseInt(longDate.substring(8, 10));
        int minute = Integer.parseInt(longDate.substring(10, 12));
        mCalendar = Calendar.getInstance();
        mCalendar.set(year, month, day, hour, minute);

        String yearStr = getYear(true);
        String dayStr = getDay();
        String minuteStr = getMinute();
        String monthStr = getMonth(true).substring(0, 3);
        String hourStr = getHour(true);
        retVal = monthStr + " " + dayStr + ", " + yearStr + " " + hourStr + ":" + minuteStr;

        return retVal;
    }

    /**
     * Test main method.
     * @param args
     */
    public static void main (String[] args) {
        String testStr1 = "9-29-2004";
        String testStr2 = "12-1-2002";
        System.out.println("converting date " + testStr1 + " to:" + formatDate(testStr1, DateUtil.FORMAT_MM_DD_YYYY));
        System.out.println("converting date " + testStr2 + " to:" + formatDate(testStr2, DateUtil.FORMAT_MM_DD_YYYY));
    }

    public static void mainOrg (String[] args) {
        Init();
        System.out.println("Today's date is:" + getTodaysDate(true, true));
        System.out.println("Today's date is:" + getTodaysDate(true, false));
        System.out.println("Today's date is:" + getTodaysDate(false, true));
        System.out.println("Today's date is:" + getTodaysDate(false, false));

        System.out.println("Custom date is::" + longDateToVerbose("20030612093513"));
        System.out.println("Custom date is::" + longDateToVerbose("19740612093513"));
        System.out.println("Custom date is::" + longDateToVerbose("20100101010101"));
        System.out.println("Custom date is::" + longDateToVerbose("20031201155501"));

    }

    public static void mainOld2 (String[] args) {
        System.out.println("Today's date is:" + getTodaysDate(true, true));
        System.out.println("Custom date is::" + longDateToVerbose("20031204180436"));
    }
}
