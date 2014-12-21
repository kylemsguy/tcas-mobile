package com.kylemsguy.tcasmobile;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class OhareanCalendar extends GregorianCalendar {
    private Calendar cal;

    public OhareanCalendar() {
        super();
    }

    public OhareanCalendar(int year, int month, int day) {
        super(year, month, day);
    }

    public OhareanCalendar(int year, int month, int day, int hour, int minute) {
        super(year, month, day, hour, minute);
    }

    public OhareanCalendar(int year, int month, int day, int hour, int minute, int second) {
        super(year, month, day, hour, minute, second);
    }

    public OhareanCalendar(Locale locale) {
        super(locale);
    }

    public OhareanCalendar(TimeZone timezone) {
        super(timezone);
    }

    public OhareanCalendar(TimeZone timezone, Locale locale) {
        super(timezone, locale);
    }

    public int getOhareanTimeZone() {
        return 0;
    }

    public int[] getOhareanDate() {
        Date d = getTime();
        long s = d.getTime();
        s = s - 946684800; // shift epoch to year 2000

        // calculate length of Abeo
        long m = s / 100;
        s = s % 100;

        long h = m / 50;
        m = m % 50;

        long day = h / 20;
        h = h % 20;

        //TODO figure out how to get current year

        return null;
    }

    @Override
    public String toString() {
        //return super.toString();
        int[] oDate = getOhareanDate();
        return "";
    }

    public static OhareanCalendar dateGregToOharean(int time_t, int offset) {
        return null;
    }

    public static OhareanCalendar dateGregToOharean(int month, int day, int year) {
        return null;
    }

    public static int dateOhareanToUNIX(int[] Oharean) {
        return 0;
    }
}
