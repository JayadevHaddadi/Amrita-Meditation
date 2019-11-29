package edu.amrita.elearn.iamhelper.util;

import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class DateUtil {
    private static final long MILLISECONDS_PER_DAY = 24 * 3600 * 1000;
    static Calendar calendar = Calendar.getInstance();

    public static Date getDayOnly(Date date) {
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
//        return new Date(date.getTime() - (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) * 60 * 1000);
    }

    public static void setHourMinuteForDate(Date date, int hour, int min) {
        calendar.setTime(date);
        Timber.d("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
        Timber.d("MINUTE: " + calendar.get(Calendar.MINUTE));
        Timber.d("HOUR_OF_DAY: " + hour);
        Timber.d("MINUTE: " + min);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        date.setTime(calendar.getTimeInMillis());
    }

    public void whenGettingDateWithoutTimeUsingCalendar_thenReturnDateWithoutTime() {
        Date dateWithoutTime = DateUtil.getDateWithoutTimeUsingCalendar();

        calendar.setTime(dateWithoutTime);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTimeInMillis(dateWithoutTime.getTime() + MILLISECONDS_PER_DAY - 1);

        calendar.setTimeInMillis(dateWithoutTime.getTime() + MILLISECONDS_PER_DAY);
    }

    public static Date getDateWithoutTimeUsingCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
