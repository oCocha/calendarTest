package com.bocha.calendartest.utility;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by oCocha on 22.12.2016.
 */

public class EventUtility {
    public static ArrayList<ArrayList> eventList = new ArrayList<ArrayList>();
    public static ArrayList<String> nameOfEvent = new ArrayList<String>();
    public static ArrayList<String> startDates = new ArrayList<String>();
    public static ArrayList<String> endDates = new ArrayList<String>();
    public static ArrayList<String> descriptions = new ArrayList<String>();

    public static ArrayList<ArrayList> readCalendarEvent(Context context) {
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/events"),
                        new String[]{"calendar_id", "title", "description",
                                "dtstart", "dtend", "eventLocation"}, null,
                        null, null);
        cursor.moveToFirst();
        // fetching calendars name
        String CNames[] = new String[cursor.getCount()];

        // fetching calendars id
        nameOfEvent.clear();
        startDates.clear();
        endDates.clear();
        descriptions.clear();
        for (int i = 0; i < CNames.length; i++) {

            /**Save the event data in separate arraylists*/
            nameOfEvent.add(cursor.getString(1));
            startDates.add(getDate(Long.parseLong(cursor.getString(3))));
            endDates.add(getDate(Long.parseLong(cursor.getString(4))));
            descriptions.add(cursor.getString(2));

            /**Save the current event in a single Arraylist*/
            ArrayList<String> tempEvenList = new ArrayList<String>();
            tempEvenList.add(cursor.getString(1));
            tempEvenList.add(cursor.getString(3));
            tempEvenList.add(cursor.getString(4));
            tempEvenList.add(cursor.getString(2));

            /**Add the current event arraylist to the arraylist containing all events*/
            eventList.add(tempEvenList);
            CNames[i] = cursor.getString(1);
            cursor.moveToNext();

        }
        return eventList;
    }

    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
