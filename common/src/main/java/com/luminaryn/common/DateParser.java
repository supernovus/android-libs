package com.luminaryn.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateParser {
    public static String fromMilliseconds (long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String fromSeconds (int seconds) {
        return fromMilliseconds(seconds * 1000);
    }
}
