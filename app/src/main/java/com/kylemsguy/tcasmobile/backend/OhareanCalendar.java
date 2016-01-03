package com.kylemsguy.tcasmobile.backend;

import java.util.Calendar;

public class OhareanCalendar {

    public static final String[] SEASON_NAMES = {"Ineo", "Cresco", "Vigeo", "Cado", "Abeo"};
    public static final int MINUTE_LENGTH_SEC = 100;
    public static final int HOUR_LENGTH_SEC = MINUTE_LENGTH_SEC * 50;
    public static final int DAY_LENGTH_SEC = HOUR_LENGTH_SEC * 20;
    public static final int WEEK_LENGTH_SEC = DAY_LENGTH_SEC * 6;
    public static final int SEASON_LENGTH_SEC = WEEK_LENGTH_SEC * 15;
    public static final int YEAR_LENGTH_SEC = SEASON_LENGTH_SEC * 4 + 5 * DAY_LENGTH_SEC;
    public static final int LYEAR_LENGTH_SEC = YEAR_LENGTH_SEC + DAY_LENGTH_SEC;

    // internal constants for int array format
    private static final int DATE_ARY_YEAR = 0;
    private static final int DATE_ARY_SEASON = 1;
    private static final int DATE_ARY_WEEK = 2;


    private long unix; // unix timestamp

    public OhareanCalendar() {
        // get current time
        unix = System.currentTimeMillis() / 1000;
    }

    public OhareanCalendar(long unix) {
        this.unix = unix;
    }

    public OhareanCalendar(Calendar date) {
        // get unix timestamp and store
        unix = date.getTimeInMillis() / 1000;
    }

    public OhareanCalendar(String ohareanDate) {
        // parse date string and give date
    }

    @Override
    public String toString() {
        int[] dateAry = unixToOharean(unix);
        StringBuilder sb = new StringBuilder();
        sb.append(dateAry[0]);
        sb.append(" ");
        sb.append(SEASON_NAMES[dateAry[1]]);
        sb.append(" ");
        sb.append(dateAry[2]);
        sb.append(" ");
        sb.append("THIS CLASS IS NOT FUNCTIONAL YET");
        return sb.toString();
    }

    private static int[] unixToOharean(long unix) {
        long sec = unix - 946684800; // shift epoch to year 2000

        int year = (int) (sec / YEAR_LENGTH_SEC);
        sec %= YEAR_LENGTH_SEC;
        int month = (int) (sec / SEASON_LENGTH_SEC);
        sec %= SEASON_LENGTH_SEC;
        int week = (int) (sec / DAY_LENGTH_SEC);
        sec %= DAY_LENGTH_SEC;
        int hour = (int) (sec / HOUR_LENGTH_SEC);
        sec %= HOUR_LENGTH_SEC;
        int minute = (int) (sec / MINUTE_LENGTH_SEC);
        sec %= MINUTE_LENGTH_SEC;

        return new int[]{month, week, hour, minute, (int) sec};
    }

    public static int getMonthIndex(String month) throws IllegalArgumentException {
        for (int i = 0; i < SEASON_NAMES.length; i++) {
            if (SEASON_NAMES[i].equals(month))
                return i;
        }
        throw new IllegalArgumentException("Invalid month name, \"" + month + "\".");
    }

    public static long daysOffsetToUnix(double offset) {
        long currentTime = System.currentTimeMillis() / 1000;
        int secsInDay = DAY_LENGTH_SEC;
        return (long) (currentTime - offset * secsInDay);
    }

    public static double unixToDaysOffset(long unix) {
        long currentTime = System.currentTimeMillis() / 1000;
        return (double) (currentTime - unix) / DAY_LENGTH_SEC;
    }

    /**
     * Returns a tuple (days, hours, minutes, seconds) showing the time from unix1 to unix2
     * <p/>
     * The unix timestamps are assumed to be given in seconds, and not millis.
     * <p/>
     * Note: this is for the Gregorian calendar.
     *
     * @param unix1
     * @param unix2
     * @return
     */
    public static int[] diffTime(long unix1, long unix2) {
        long diff = Math.abs(unix1 - unix2);

        int days = (int) (diff / 86400);
        diff = diff % 86400;
        int hours = (int) (diff / 3600);
        diff = diff % 3600;
        int minutes = (int) (diff / 60);
        diff = diff % 60;

        return new int[]{days, hours, minutes, (int) diff};
    }

}
