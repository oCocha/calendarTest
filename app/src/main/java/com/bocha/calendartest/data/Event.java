package com.bocha.calendartest.data;

import java.util.ArrayList;

/**
 * Created by bob on 26.12.16.
 */

public class Event {
    private int[] eventStartDate;
    private int[] eventEndDate;
    private String eventName;
    private String eventDescription;

    public Event(int[] startDate, int[] endDate, String name, String description){
        eventStartDate = startDate;
        eventEndDate = endDate;
        eventName = name;
        eventDescription = description;
    }

    public int[] getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(int[] eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public int[] getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(int[] eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
}
