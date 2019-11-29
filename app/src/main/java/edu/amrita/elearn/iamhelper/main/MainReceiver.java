package edu.amrita.elearn.iamhelper.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MainReceiver extends BroadcastReceiver {

    public static final String TIME_PASSED = "update";
    public static final String STATE = "state";
    public static final String EXTRA_NAME = "receiver_extra_name";
    public static final String IAM_TIME = "times";
    public static final String RECEIVER_ACTION = "edu.amrita.elearn.iamhelper.NOTIFICATION";
    public static final String RESET = "reset";
    public static final String RATE = "rate";
    private static final String TAG = MainReceiver.class.getSimpleName();
    private MainActivity mainActivity;

    public MainReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Recived broadcast!");
        String action = intent.getStringExtra(EXTRA_NAME);
        switch (action) {
            case TIME_PASSED:
                mainActivity.updateTickingTimer();
                break;
            case STATE:
                mainActivity.updateState();
                break;
            case IAM_TIME:
                mainActivity.updateTotalTime();
                break;
            case RESET:
//                mainActivity.scrollToTop();
                mainActivity.updateState();
                mainActivity.updateTickingTimer();
                break;
            case RATE:
                mainActivity.startRating();
                break;
        }
    }
}
