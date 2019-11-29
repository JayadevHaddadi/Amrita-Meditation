package edu.amrita.elearn.iamhelper.main;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.os.BuildCompat;
import android.util.Log;

import edu.amrita.elearn.iamhelper.BuildConfig;
import edu.amrita.elearn.iamhelper.database.HistoryModel;
import edu.amrita.elearn.iamhelper.model.StateModel;
import edu.amrita.elearn.iamhelper.util.GlobalPreferences;
import timber.log.Timber;

public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        Timber.d("OnCreate!!!!");
        final Context applicationContext = getApplicationContext();
        final SharedPreferences prefs = getDefaultSharedPreferences(applicationContext);
        GlobalPreferences.init(prefs);
        StateModel.getModel().init(applicationContext, prefs);
        HistoryModel.getModel().init(this);

        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }

        super.onCreate();
    }

    /**
     * Returns the default {@link SharedPreferences} instance from the underlying storage context.
     */
    @TargetApi(Build.VERSION_CODES.N)
    private static SharedPreferences getDefaultSharedPreferences(Context context) {
        final Context storageContext;
        if (BuildCompat.isAtLeastN()) {
            // All N devices have split storage areas. Migrate the existing preferences into the new
            // device encrypted storage area if that has not yet occurred.
            final String name = PreferenceManager.getDefaultSharedPreferencesName(context);
            storageContext = context.createDeviceProtectedStorageContext();
            if (!storageContext.moveSharedPreferencesFrom(context, name)) {
                Log.d(TAG,"Failed to migrate shared preferences");
            }
        } else {
            storageContext = context;
        }
        return PreferenceManager.getDefaultSharedPreferences(storageContext);
    }
}
