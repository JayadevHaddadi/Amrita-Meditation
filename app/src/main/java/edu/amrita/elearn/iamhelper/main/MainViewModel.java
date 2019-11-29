package edu.amrita.elearn.iamhelper.main;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import edu.amrita.elearn.iamhelper.model.ControllerService;


public class MainViewModel extends AndroidViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();

    public MainViewModel(@NonNull final Application application) {
        super(application);
        Log.d(TAG, "onCreate MainViewModel");

        Intent startIntent = new Intent(application, ControllerService.class);
        startIntent.setAction(ControllerService.START_FOREGROUND_ACTION);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String ANDROID_CHANNEL_NAME = "IamHelper channel";

            NotificationChannel androidChannel = new NotificationChannel(ControllerService
                    .ANDROID_CHANNEL_ID, ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(Color.BLUE);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            androidChannel.setImportance(NotificationManager.IMPORTANCE_NONE);

            ((NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(androidChannel);

            application.startForegroundService(startIntent);
        } else {
            application.startService(startIntent);
        }
    }
}
