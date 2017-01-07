package com.bocha.calendartest.utility;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bob on 07.01.17.
 */

public class MiscUtility {

    /**Get the combined duration of a given hour and minute count in milliseconds
     *
     * @param durationHour
     * @param durationMinute
     * @return duration in milliseconds
     */
    public static Long getDurationMillis(int durationHour, int durationMinute) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.HOUR, durationHour);
        calendar.set(Calendar.MINUTE, durationMinute);
        Long startingDate = calendar.getTime().getTime();

        return startingDate;
    }

    /** Combine a given date, hour and minute to get a combined date from these values
     *
     * @param date
     * @param hour
     * @param minute
     * @return
     */
    public static Date getStartingDate(Date date, int hour, int minute) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(date);
        String yearString = dateString.substring(0, 4);
        String monthString = dateString.substring(5, 7);
        String dayString = dateString.substring(8);

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.MONTH, Integer.parseInt(monthString)-1);
        calendar.set(Calendar.YEAR, Integer.parseInt(yearString));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayString));
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        Date startingDate = calendar.getTime();

        return startingDate;
    }

}
