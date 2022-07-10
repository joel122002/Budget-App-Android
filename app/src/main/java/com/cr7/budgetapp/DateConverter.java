package com.cr7.budgetapp;

import java.util.Date;

public class DateConverter {
    public static final long MAGIC = 86400000L;
    public static final long HOUR = 3600 * 1000;
    public static final long MINUTE = 60 * 1000;

    public int DateToDays(Date date) {
        //  convert a date to an integer and back again
        long currentTime = date.getTime();
        currentTime = currentTime / MAGIC;
        return (int) currentTime;
    }

    public Date DaysToDate(int days) {
        //  convert integer back again to a date
        long currentTime = (long) days * MAGIC;
        return new Date(currentTime);
    }
}
