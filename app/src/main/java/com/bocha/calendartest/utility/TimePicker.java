package com.bocha.calendartest.utility;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

/**
 * Created by bob on 06.01.17.
 */

public class TimePicker extends android.widget.TimePicker {

    public TimePicker(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    public TimePicker(Context context) {
        super(context);
    }

    public void setCurrentHour(int hour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            super.setHour(hour);
        else
            super.setCurrentHour(hour);
    }

    public void setCurrentMinute(int minute) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            super.setMinute(minute);
        else
            super.setCurrentMinute(minute);
    }

    public Integer getCurrentHour() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return super.getHour();
        else
            return super.getCurrentHour();
    }

    public Integer getCurrentMinute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return super.getMinute();
        else
            return super.getCurrentMinute();
    }
}
