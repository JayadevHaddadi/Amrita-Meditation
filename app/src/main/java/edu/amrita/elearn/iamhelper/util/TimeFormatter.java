package edu.amrita.elearn.iamhelper.util;

import java.text.MessageFormat;
import java.util.Locale;

public class TimeFormatter {
    public static String intToMinutesAndSeconds(int totalTime) {
        int sec = totalTime % 60;
        String secString = String.valueOf(sec);
        if (sec < 10)
            secString = "0" + String.valueOf(sec);

        int min = totalTime / 60;
        return MessageFormat.format("{0}:{1}", min, secString);
    }

    public static String formatIntToMinutesWithValue(int totalTime) {
        return intToMinutesAndSeconds(totalTime) + " min";
    }

    public static String intRoundToMinutesWithValue(int totalTime) {
        return ((int)Math.ceil(totalTime/60.0)) + " min";
    }

    public static String formatIntToSecondsOrMinutes(int timeInSec) {
        if (timeInSec < 60)
            return "" + timeInSec;

        int sec = timeInSec % 60;
        String secString = String.valueOf(sec);
        if (sec < 10)
            secString = "0" + String.valueOf(sec);

        int min = timeInSec / 60;
        return MessageFormat.format("{0}:{1}", min, secString);
    }

    public static String formatIntToSecondsOrMinutesWithValue(int timeInSec) {
        return formatIntToSecondsOrMinutes(timeInSec) + " min";
    }

    public static String floatToString(float number) {
        return String.format(Locale.getDefault(),"%.1f", number);
    }
}
