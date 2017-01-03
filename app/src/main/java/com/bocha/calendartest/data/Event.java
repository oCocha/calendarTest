package com.bocha.calendartest.data;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by bob on 26.12.16.
 */

public class Event {
    private Date eventStartDate;
    private Date eventEndDate;
    private String eventName;
    private String eventDescription;

    public Event(Date startDate, Date endDate, String name, String description){
        eventStartDate = startDate;
        eventEndDate = endDate;
        eventName = name;
        eventDescription = description;
    }

    public Event(Long startDateLong, Long endDateLong, String name, String description){
        Date startDate = new Date();
        startDate.setTime(startDateLong);
        Date endDate = new Date();
        endDate.setTime(endDateLong);
        eventStartDate = startDate;
        eventEndDate = endDate;
        eventName = name;
        eventDescription = description;
    }

    public Date getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(Date eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public Date getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(Date eventStartDate) {
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
