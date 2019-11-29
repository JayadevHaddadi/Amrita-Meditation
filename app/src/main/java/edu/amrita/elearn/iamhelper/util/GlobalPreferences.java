package edu.amrita.elearn.iamhelper.util;

import android.content.SharedPreferences;

public class GlobalPreferences {
    public static final String KEY_SPINNER_EXTRA_TIME = "extra key";
    public static final String KEY_STAY_AWAKE = "stay awake";
    public static final String KEY_SPINNER_SHAVASANA = "shavasana time";
    public static final String KEY_SILENT = "mode";
    public static final String KEY_VOLUME = "volume";
    public static final String KEY_VIBRATE = "vibrate";
    public static final String KEY_SPINNER_END_MEDITATION = "end meditation";
    public static final String KEY_GOOGLE_SYNC = "google sync";
    public static final String KEY_ACCOUNT_NAME = "account name";
    public static final String KEY_HUMAN_VOICE = "human voice";
    private static final String KEY_CALENDAR_ID = "calendar id";

    private static SharedPreferences prefs;

    public static void init(SharedPreferences prefs) {
        GlobalPreferences.prefs = prefs;
    }

    public static boolean getGoogleSync() {
        return prefs.getBoolean(KEY_GOOGLE_SYNC, false);
    }

    public static void setSadhanaCalendarID(String sadhanaCalendarID) {
        prefs.edit().putString(KEY_CALENDAR_ID, sadhanaCalendarID).apply();
    }

    public static String getSadhanaCalendarID() {
        return prefs.getString(KEY_CALENDAR_ID, "");
    }

    public static String getAccountName() {
        return prefs.getString(GlobalPreferences.KEY_ACCOUNT_NAME, null);
    }

    public static void setGoogleSync(boolean value) {
        prefs.edit().putBoolean(GlobalPreferences.KEY_GOOGLE_SYNC, value).apply();
    }

    public static void setAccountName(String accountName) {
        prefs.edit().putString(GlobalPreferences.KEY_ACCOUNT_NAME, accountName).apply();
    }
}
