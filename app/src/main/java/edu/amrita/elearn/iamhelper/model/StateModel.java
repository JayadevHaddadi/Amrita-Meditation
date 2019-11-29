package edu.amrita.elearn.iamhelper.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.amrita.elearn.iamhelper.R;
import edu.amrita.elearn.iamhelper.main.MainReceiver;
import edu.amrita.elearn.iamhelper.util.GlobalPreferences;

/**
 * All application-wide data is accessible through this singleton.
 */
public final class StateModel {

    private static final long VIBRATION_MILLISECONDS = 500;
    private boolean TTS_available = false;
    public static final int PAUSED = 0;
    public static final int RUNNING = 1;
    public static final int FINISHED = 2;
    public static final int RATING = 3;

    private int state;
    private int currentItemTime;
    private int currentItemPosition;
    private int totalTimePassed;
    private static final StateModel sDataModel = new StateModel();
    private static final String TAG = "IamDataModel";
    private Context mContext;
    private ArrayList<IamPart> iamParts;
    private TextToSpeech myTTS;
    private SharedPreferences prefs;
    private Vibrator vibrator;
    private int totalTime;
    private MediaPlayer mp;

    public static StateModel getModel() {
        return sDataModel;
    }

    /**
     * Initializes the data model with the context and shared preferences to be used.
     */
    public void init(Context context, SharedPreferences prefs) {
        Log.d(TAG, "Calling init");
        this.prefs = prefs;
        if (mContext != context) {
            Log.d(TAG, "New instantiation");
            mContext = context.getApplicationContext();

            iamParts = new ArrayList<>();
            iamParts.add(new IamPart(context.getString(R.string.sit_comfortably), "Spine straight", 10, R.raw.sit_comfortably));
            iamParts.add(new IamPart(context.getString(R.string.gaze_down), "Watch your mind", 10, R.raw.gaze_down));
            iamParts.add(new IamPart(context.getString(R.string.breating_practice), "Slowly close your eyes. Breathing practice", 15, R.raw.breathing_practice));
            iamParts.add(new IamPart(context.getString(R.string.deep_peace), "Be aware of the vibrations created by this practice", 10, R.raw.deep_peace));
            iamParts.add(new IamPart(context.getString(R.string.close_ears), "Immersed in the sound of the " +
                    "reverberations", 10, R.raw.close_ears_reverberation));
            iamParts.add(new IamPart(context.getString(R.string.makara), "Intone the sound \"Mmm….\" 5 times", 60, R.raw.makara_1));

            //Part II
            iamParts.add(new IamPart(context.getString(R.string.palm_on_head), "Showering of the grace of God", 15, R.raw.palm_on_head_gods_grace));
            iamParts.add(new IamPart(context.getString(R.string.ice_cold_water), "From the sahasrara to the muladhara", 15, R.raw.ice_cold_water));
            iamParts.add(new IamPart(context.getString(R.string.press_with), "Feel the vibration of that touch straight down", 15, R.raw.press_with_middle_finger));
            iamParts.add(new IamPart(context.getString(R.string.feel_vibration), "Vibration as passing from the sahasrara to the muladhara",
                    15, R.raw.feel_vibration));

            iamParts.add(new IamPart(context.getString(R.string.visualize), "Visualize a shining golden star there.", 15, R.raw.visualize_golden));
            iamParts.add(new IamPart(context.getString(R.string.vishuddhi), "Visualize a shining golden star there.", 15, R.raw.vishuddhi_chakra));
            iamParts.add(new IamPart(context.getString(R.string.anahata), "Visualize a shining golden star there.", 15, R.raw.anahata_chakra));
            iamParts.add(new IamPart(context.getString(R.string.manipuraka), "Visualize a shining golden star there.", 15, R.raw.manipuraka_chakra));
            iamParts.add(new IamPart(context.getString(R.string.swadhisthana), "Visualize a shining golden star there.", 15, R.raw.swadhistana_1));
            iamParts.add(new IamPart(context.getString(R.string.muladhara), "Visualize a shining golden star there.", 15, R.raw.muladhara_chakra));
            iamParts.add(new IamPart(context.getString(R.string.swadhisthana), "Visualize a shining golden star there.", 15, R.raw.swadhistana_1));
            iamParts.add(new IamPart(context.getString(R.string.manipuraka), "Visualize a shining golden star there.", 15, R.raw.manipuraka_chakra));
            iamParts.add(new IamPart(context.getString(R.string.anahata), "Visualize a shining golden star there.", 15, R.raw.anahata_chakra));
            iamParts.add(new IamPart(context.getString(R.string.vishuddhi), "Visualize a shining golden star there.", 15, R.raw.vishuddhi_chakra));
            iamParts.add(new IamPart(context.getString(R.string.ajna), "Visualize a shining golden star there.", 15, R.raw.ajna_chakra));
            iamParts.add(new IamPart(context.getString(R.string.sahasrara), "Visualize a shining golden star there.", 15, R.raw.sadhaswara_chakra));
            iamParts.add(new IamPart(context.getString(R.string.rising_sun), "Sahasrara chakra expanding", 30, R.raw.rising_sun));
            iamParts.add(new IamPart(context.getString(R.string.golden), "Sahasrara chakra spreading. Slowly spreading all over body",
                    120, R.raw.golden_effulgence));

            //Part III
            iamParts.add(new IamPart(context.getString(R.string.slowly_draw), "Rising along spine, descending front body", 120, R.raw.slowly_draw_breath));
            iamParts.add(new IamPart(context.getString(R.string.feel_vibration_muladhara), "Focus your mind on the vibrations created", 10, R.raw.feel_vibration_muladhara));
            iamParts.add(new IamPart(context.getString(R.string.swadhisthana), "Focus your mind on the vibrations created", 10, R.raw.swadhistana_1));
            iamParts.add(new IamPart(context.getString(R.string.manipuraka), "Focus your mind on the vibrations created", 10, R.raw.manipuraka_chakra));
            iamParts.add(new IamPart(context.getString(R.string.anahata), "Focus your mind on the vibrations created", 10, R.raw.anahata_chakra));
            iamParts.add(new IamPart(context.getString(R.string.vishuddhi), "Focus your mind on the vibrations created", 10, R.raw.vishuddhi_chakra));
            iamParts.add(new IamPart(context.getString(R.string.ajna), "Focus your mind on the vibrations created", 10, R.raw.ajna_chakra));
            iamParts.add(new IamPart(context.getString(R.string.sahasrara), "Focus your mind on the vibrations created", 10, R.raw.sadhaswara_chakra));

            iamParts.add(new IamPart(context.getString(R.string.ice_cold_water), "From the sahasrara to the muladhara", 30, R.raw.ice_cold_water));
            iamParts.add(new IamPart(context.getString(R.string.gaze_down), "Watch your mind", 120, R.raw.gaze_down));
            iamParts.add(new IamPart(context.getString(R.string.shavasana), "Lie down dont sleep", 0, R.raw.shavasana_3));
            iamParts.add(new IamPart(context.getString(R.string.end_meditation), "Meditate", 0, R.raw.end_meditation));
            Log.d(TAG, "Created all recycle items");
            updateTimes();

            setupTTS();

            resetState();

            vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        } else {
            Log.d(TAG, "No new inistantiation needed");
        }
    }

    public void updateTimes() {
        String string = prefs.getString(GlobalPreferences.KEY_SPINNER_EXTRA_TIME, mContext.getString
                (R.string.default_extra_time));
        assert string != null;
        int intExtraTime = Integer.valueOf(string.split(" ")[0]);

        string = prefs.getString(GlobalPreferences.KEY_SPINNER_SHAVASANA, mContext.getString(R.string
                .default_shavasana));
        assert string != null;
        int intShavasanaTime = Integer.valueOf(string.split(" ")[0]);

        string = prefs.getString(GlobalPreferences.KEY_SPINNER_END_MEDITATION, mContext.getString(R
                .string.default_shavasana));
        assert string != null;
        int intEndMeditation = Integer.valueOf(string.split(" ")[0]);

        totalTime = 0;

        for (IamPart part : iamParts) {
            if (!part.getName().equals(mContext.getString(R.string.shavasana)) &&
                    !part.getName().equals(mContext.getString(R.string.end_meditation)))
                part.addTime(intExtraTime);
            else if (part.getName().equals(mContext.getString(R.string.shavasana)))
                part.addTime(intShavasanaTime * 60);
            else if (part.getName().equals(mContext.getString(R.string.end_meditation)))
                part.addTime(intEndMeditation * 60); // TODO: 10
            totalTime += part.getTime();
        }

        Intent intent = new Intent();
        intent.setAction(MainReceiver.RECEIVER_ACTION);
        intent.putExtra(MainReceiver.EXTRA_NAME, MainReceiver.IAM_TIME);
        mContext.sendBroadcast(intent);
    }

    void resetState() {
        state = PAUSED;
        currentItemTime = 0;
        currentItemPosition = 0; // TODO: iamParts.size()-1
        totalTimePassed = 0;
//        for(IamPart part: iamParts){
//            if(part.getName().equals())
//        }
    }

    private void setupTTS() {
//        makeToast("Initating TTS!");
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        List<ResolveInfo> activities = mContext.getPackageManager().queryIntentActivities
                (checkTTSIntent, 0);
        if (activities.size() > 0) {
            myTTS = new TextToSpeech(mContext, initStatus -> {
                if (initStatus == TextToSpeech.SUCCESS) {
                    if (myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE) {
                        myTTS.setLanguage(Locale.US);
                    } else {
                        Log.d(TAG, "English Text-To-Speech missing!");
//                            makeToast("English Text-To-Speech missing!");
//                            Intent installTTSIntent = new Intent();
//                            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                            currentActivity.startActivity(installTTSIntent);
                    }
                } else if (initStatus == TextToSpeech.ERROR) {
                    makeToast("Sorry! Text To Speech failed...");
                    Log.e(TAG, "TTS failed");
                }

                AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                int amStreamMusicMaxVol = audioManager.getStreamMaxVolume
                        (AudioManager.STREAM_MUSIC);
                Log.d(TAG, "max volume: " + amStreamMusicMaxVol);

                int vol = prefs.getInt(GlobalPreferences.KEY_VOLUME, amStreamMusicMaxVol * 3 / 4);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

                TTS_available = true;
            });
        } else {
            Log.e(TAG, "Sorry Text-To-Speach function not found!");
            makeToast("Sorry Text-To-Speach function not found!");
            TTS_available = false;
        }
    }

    private void makeToast(String text) {
        Log.e(TAG, "SHOULD HAVE MAKE TOAST: " + text);
    }

    public ArrayList<IamPart> getIamParts() {
        return iamParts;
    }

    public int getState() {
        return state;
    }

    public int getCurrentItemPosition() {
        return currentItemPosition;
    }

    public void setCurrentItemPosition(int position) {
        currentItemPosition = position;
    }

    public int getCurrentItemTime() {
        return currentItemTime;
    }

    public void setCurrentItemTime(int time) {
        currentItemTime = time;
    }

    public int getTotalTimePassed() {
        return totalTimePassed;
    }

    boolean goToNext(){
        if (currentItemPosition < iamParts.size()) {
            currentItemTime = 0;
            currentItemPosition++;
        }
        if (currentItemPosition >= iamParts.size()) {
            playAudioOrTTS(R.raw.ohnamahshivaya_4, "Om Namah Shivaya");
            sendStartRateBroadcast();
            state = StateModel.RATING;
            return false;
        } else if(state == RUNNING) {
            soundCurrentItem(true);
        }
        timePassedBroadcast();
        return true;
    }

    void goToLast(){
        if(currentItemTime < 3 && currentItemPosition > 0)
            currentItemPosition--;
        currentItemTime = 0;
        if(state == RUNNING) {
            soundCurrentItem(true);
        }
        timePassedBroadcast();
    }

    boolean increaseTimer() {
        currentItemTime++;
        totalTimePassed++;

        IamPart iamPart = iamParts.get(currentItemPosition);

        // FINISHED THIS ITEM
        if (currentItemTime >= iamPart.getTime()) {
            return goToNext();
        }

        timePassedBroadcast();
        return true;
    }

    private void timePassedBroadcast() {
        Intent intent = new Intent();
        intent.setAction(MainReceiver.RECEIVER_ACTION);
        intent.putExtra(MainReceiver.EXTRA_NAME, MainReceiver.TIME_PASSED);
        mContext.sendBroadcast(intent);
    }

    private void sendStartRateBroadcast() {
        Intent intent = new Intent()
                .setAction(MainReceiver.RECEIVER_ACTION)
                .putExtra(MainReceiver.EXTRA_NAME, MainReceiver.RATE);
        mContext.sendBroadcast(intent);
    }

    void play() {
        state = RUNNING;
        sendStateBroadcast();
        soundCurrentItem(false);
    }

    public void soundCurrentItem(boolean vibrate) {
        IamPart nextIamPart = iamParts.get(currentItemPosition);
        if (nextIamPart.getTime() > 0) {
            playAudioOrTTS(nextIamPart.getAudioID(), nextIamPart.getName());

            if (vibrate && prefs.getBoolean(GlobalPreferences.KEY_VIBRATE, false)) {
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MILLISECONDS,
                            VibrationEffect
                                    .DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    vibrator.vibrate(VIBRATION_MILLISECONDS);
                }
            }
        }
    }

    private void playAudioOrTTS(int audioID, String audioString) {
        if (prefs.getBoolean(GlobalPreferences.KEY_HUMAN_VOICE, true)) {
            if (mp != null) {
                mp.stop();
                mp.release();
                mp = null;
            }
            mp = MediaPlayer.create(mContext, audioID);
            mp.start();
        } else if (TTS_available)
            myTTS.speak(audioString, TextToSpeech.QUEUE_FLUSH, null);
    }

    void pause() {
        state = PAUSED;
        sendStateBroadcast();
        playAudioOrTTS(R.raw.pause_1, mContext.getString(R.string.pause_voice));
    }

    private void sendStateBroadcast() {
        Intent intent = new Intent();
        intent.setAction(MainReceiver.RECEIVER_ACTION);
        intent.putExtra(MainReceiver.EXTRA_NAME, MainReceiver.STATE);
        mContext.sendBroadcast(intent);
    }

    public String getCurrentItemName() {
        if (currentItemPosition < iamParts.size())
            return iamParts.get(currentItemPosition).getName();
        else
            return "Om Namah Shivaya";
    }

    int getCurrentItemEndTime() {
        return iamParts.get(currentItemPosition).getTime();
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    void resetFromEnd() {
        resetState();
        Intent intent = new Intent();
        intent.setAction(MainReceiver.RECEIVER_ACTION);
        intent.putExtra(MainReceiver.EXTRA_NAME, MainReceiver.RESET);
        mContext.sendBroadcast(intent);
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void stopSilentMode() {

    }

    public void ratingDone() {
        state = StateModel.FINISHED;
    }

    public int getCurrentItemMaxTime() {
        if (currentItemPosition < iamParts.size())
            return iamParts.get(currentItemPosition).getMaxTime();
        else
            return 0;
    }
}