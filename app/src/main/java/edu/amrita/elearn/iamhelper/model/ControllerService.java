package edu.amrita.elearn.iamhelper.model;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import edu.amrita.elearn.iamhelper.R;
import edu.amrita.elearn.iamhelper.main.MainActivity;
import edu.amrita.elearn.iamhelper.util.TimeFormatter;

public class ControllerService extends Service {

    public static final String ANDROID_CHANNEL_ID = "edu.amrita.elearn.iamhelper.NOTIFICATION_CHANNEL";

    private static final String ACTION_PREFIX = "edu.amrita.elearn.iamhelper.action.";
    public static final String MAIN_ACTION = ACTION_PREFIX + "main";
    public static final String PLAY_ACTION = ACTION_PREFIX + "play";
    public static final String PAUSE_ACTION = ACTION_PREFIX + "pause";
    public static final String RESET_ACTION = ACTION_PREFIX + "reset";
    public static final String UPDATE_ACTION = ACTION_PREFIX + "update";
    public static final String NEXT_ACTION = ACTION_PREFIX + "next";
    public static final String LAST_ACTION = ACTION_PREFIX + "last";
    public static final String START_FOREGROUND_ACTION = ACTION_PREFIX + "start_foreground";
    public static final String STOP_FOREGROUND_ACTION = ACTION_PREFIX + "stop_foreground";
    public static final int FOREGROUND_SERVICE_ID = 101;

    private static final String TAG = ControllerService.class.getSimpleName();
    private Timer timer;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = ((PowerManager) getSystemService(Context.POWER_SERVICE));
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        Log.i(TAG, "In onCreate");
    }

    class CounterTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Counter is TICKING");
            boolean running = StateModel.getModel().increaseTimer();
            Log.d(TAG, "RUNNING? " + running);
            if (!running) {
                Log.d(TAG, "Counter FINISHED");
                releaseWakeLockAndTimer();
            }
            updateNotification();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        assert action != null;
        switch (action) {
            case START_FOREGROUND_ACTION: {
                Log.i(TAG, "Received Start Foreground Intent ");
                startNotification();
                timer = new Timer();
                return START_NOT_STICKY;
            }
            case UPDATE_ACTION: {
                Log.i(TAG, "Clicked update");
                updateNotification();
                return START_STICKY;
            }
            case PLAY_ACTION: {
                Log.i(TAG, "Clicked PLAY");
                StateModel.getModel().play();
                updateNotification();
                timer = new Timer();
                wakeLock.acquire(60 * 60 * 1000L /*60 minutes*/);
                timer.schedule(new CounterTask(), 1000, 1000);
                return START_NOT_STICKY;
            }
            case PAUSE_ACTION: {
                Log.i(TAG, "Clicked paused");
                StateModel.getModel().pause();
                updateNotification();
                releaseWakeLockAndTimer();
                return START_STICKY;
            }
            case RESET_ACTION: {
                Log.i(TAG, "Clicked reset");
                StateModel.getModel().resetFromEnd();
                updateNotification();
                return START_STICKY;
            }
            case NEXT_ACTION: {
                Log.i(TAG, "Clicked next");
                if (StateModel.getModel().getState() == StateModel.FINISHED ||
                        StateModel.getModel().getState() == StateModel.RATING)
                    return START_STICKY;
                boolean running = StateModel.getModel().goToNext();
                if (!running) {
                    Log.d(TAG, "Counter FINISHED");
                    releaseWakeLockAndTimer();
                }
                updateNotification();
                return START_STICKY;
            }
            case LAST_ACTION: {
                Log.i(TAG, "Clicked last");
                StateModel.getModel().goToLast();
                updateNotification();
                return START_STICKY;
            }
            case STOP_FOREGROUND_ACTION: {
                Log.i(TAG, "Received Stop Foreground Intent");
//                myTTS.shutdown();
                releaseWakeLockAndTimer();
                StateModel.getModel().resetState();
                StateModel.getModel().stopSilentMode();
                stopForeground(true);
                stopSelf();
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(FOREGROUND_SERVICE_ID);
                return START_NOT_STICKY;
            }
        }
        return START_NOT_STICKY;
    }

    private void releaseWakeLockAndTimer() {
        if (wakeLock.isHeld())
            wakeLock.release();
        timer.cancel();
        timer.purge();
    }

    void startNotification() {
        Notification note = getNotification();
        startForeground(FOREGROUND_SERVICE_ID, note);
    }

    private void updateNotification() {
        Notification note = getNotification();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(FOREGROUND_SERVICE_ID, note);
    }

    private Notification getNotification() {
        Bitmap myLogo = BitmapFactory.decodeResource(getApplication().
                getResources(), R.drawable.iam_small_square);

        Intent cancelIntent = new Intent(this, ControllerService.class);
        cancelIntent.setAction(STOP_FOREGROUND_ACTION);
        final PendingIntent cancelPI = PendingIntent.getService(this, 0,
                cancelIntent, 0);

        PendingIntent mainPendingIntent = createMainPendingIntent();
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this,
                ANDROID_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_meditation_guru)
                .setContentTitle(StateModel.getModel().getCurrentItemName())
                .setLargeIcon(myLogo)
                .setDeleteIntent(cancelPI)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setAutoCancel(false)
                .setShowWhen(false);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            nb.setPriority(NotificationManager.IMPORTANCE_NONE);
        }

        if (StateModel.getModel().getState() == StateModel.RATING) {
            nb.setContentText("Finished")
                    .setContentIntent(createMainPendingIntent());
        } else {
            nb.setContentIntent(mainPendingIntent).setContentText(
                    TimeFormatter.intToMinutesAndSeconds(StateModel.getModel().getCurrentItemTime()) + "/" +
                            TimeFormatter.intToMinutesAndSeconds(StateModel.getModel()
                                    .getCurrentItemEndTime()));
            if (StateModel.getModel().getState() == StateModel.PAUSED) {
                PendingIntent playPendingIntent = createPlayPendingIntent();
                nb.setOngoing(false)
                        .setAutoCancel(true)
//                        .addAction(android.R.drawable.ic_media_play, "Play",
//                                playPendingIntent);
                        .addAction(new NotificationCompat.Action(R.drawable.ic_play_button, "Play",
                                MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(),
                                        PlaybackStateCompat.ACTION_PLAY_PAUSE)));
            } else if (StateModel.getModel().getState() == StateModel.RUNNING) {
                PendingIntent pausePendingIntent = createPausePendingIntent();
                nb.addAction(android.R.drawable.ic_media_pause, "Pause",
                        pausePendingIntent);
            }
        }
        return nb.build();
    }

    private PendingIntent createPlayPendingIntent() {
        Intent playIntent = new Intent(this, ControllerService.class);
        playIntent.setAction(PLAY_ACTION);
        return PendingIntent.getService(this, 0,
                playIntent, 0);
    }

    private PendingIntent createMainPendingIntent() {
        Intent mainIntent = new Intent(this, MainActivity.class).setAction(MAIN_ACTION);
        return PendingIntent.getActivity(this, 0,
                mainIntent, 0);
    }

    private PendingIntent createPausePendingIntent() {
        Intent pauseIntent = new Intent(this, ControllerService.class);
        pauseIntent.setAction(PAUSE_ACTION);
        return PendingIntent.getService(this, 0,
                pauseIntent, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}