package edu.amrita.elearn.iamhelper.main;

import android.Manifest;
import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.calendar.CalendarScopes;
import com.squareup.picasso.Picasso;

import java.util.List;

import edu.amrita.elearn.iamhelper.R;
import edu.amrita.elearn.iamhelper.util.GlobalPreferences;
import edu.amrita.elearn.iamhelper.util.GoogleCalendarUtil;
import edu.amrita.elearn.iamhelper.history.HistoryActivity;
import edu.amrita.elearn.iamhelper.iamparts.IamPartsActivity;
import edu.amrita.elearn.iamhelper.model.StateModel;
import edu.amrita.elearn.iamhelper.model.ControllerService;
import edu.amrita.elearn.iamhelper.util.TimeFormatter;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    //Constants
    private static final int ON_DO_NOT_DISTURB_CALLBACK_CODE = 1;
    private String TAG = MainActivity.class.getSimpleName();

    // Views
    private ImageButton playButton;
    private TextView mTvTotal;
    private TextView timePassedTV;
    private TextView currentItemMaxTime;
    private TextView currentItemTv;
    private TextView currentItemTimeTv;
    private CheckBox silentCheckBox;

    //Others
    private NavigationView mainDrawer;
    private NavigationView iamDrawer;
    public DrawerLayout drawer;

    private AudioManager audioManager;
    private int originalRingerMode;
    private int originalSystemVolume;
    private int originalMusicVolume;

    private NotificationManager notificationManager;
    private SharedPreferences prefs;
    private Toast mToast;
    private boolean CREATED_BOOLEAN = false;
    private MainReceiver receiver;
    private boolean signedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_navigation);
        Timber.d("OnCreate MainActivity + CREATED_BOOLEAN: %s", CREATED_BOOLEAN);
        CREATED_BOOLEAN = true;
        receiver = new MainReceiver(this);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.textColor));
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        iamDrawer = findViewById(R.id.iam_settings_navigation_view);
        mainDrawer = findViewById(R.id.main_navigation_view);
        mainDrawer.setNavigationItemSelectedListener(menuItem -> {
            // Handle navigation view item clicks here.
            int id = menuItem.getItemId();

            if (id == R.id.history_item) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            } else if (id == R.id.voice_item) {
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (id == R.id.iam_parts) {
                Intent intent = new Intent(MainActivity.this, IamPartsActivity.class);
                startActivity(intent);
            } else {
                Timber.d("Something else was pressed");
            }

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        timePassedTV = findViewById(R.id.time_passed_tv);
        currentItemMaxTime = findViewById(R.id.current_item_max_time);
        currentItemTv = findViewById(R.id.current_item_tv);
        currentItemTimeTv = findViewById(R.id.current_item_time);
        mTvTotal = findViewById(R.id.tv_total_time);
        playButton = findViewById(R.id.play_button);
        ImageButton nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            Intent nextIntent = new Intent(this, ControllerService.class);
            nextIntent.setAction(ControllerService.NEXT_ACTION);
            startService(nextIntent);
        });
        ImageButton lastButton = findViewById(R.id.lastButton);
        lastButton.setOnClickListener(v -> {
            Intent lastIntent = new Intent(this, ControllerService.class);
            lastIntent.setAction(ControllerService.LAST_ACTION);
            startService(lastIntent);
        });

        ViewModelProviders.of(this).get(MainViewModel.class);

        prefs = StateModel.getModel().getPrefs();

        setupSpinner();

        setupVibration();

        setupStayAwake();

        setupSilent();

        setupGoogleCalander();

        updateTickingTimer();

        updateState();

        setupHumanVoice();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.calendar_item) {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openSettingsDrawer(View view) {
        NavigationView navigationView2 = findViewById(R.id.iam_settings_navigation_view);
        drawer.openDrawer(navigationView2);
        view.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.settings_rotate));
    }

    private void setupGoogleCalander() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account == null)
            Timber.d("No last login!");
        else {
            signInGoogleAccount(account);
        }
    }

    @AfterPermissionGranted(GoogleCalendarUtil.REQUEST_PERMISSION_GET_ACCOUNTS)
    public void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(CalendarScopes.CALENDAR))
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GoogleCalendarUtil.REQUEST_ACCOUNT_PICKER);
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    GoogleCalendarUtil.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("Activity results with code %s", resultCode);
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ON_DO_NOT_DISTURB_CALLBACK_CODE:
                if (Build.VERSION.SDK_INT >= Build
                        .VERSION_CODES.M && notificationManager.isNotificationPolicyAccessGranted()) {
                    silentOn();
                    prefs.edit().putBoolean(GlobalPreferences.KEY_SILENT, true).apply();
                }
                break;
            case GoogleCalendarUtil.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    GoogleCalendarUtil.getUtil().setupSync(this);
                } else {
                    makeToast("This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.");
                }
                break;
            case GoogleCalendarUtil.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {

                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    GoogleSignInAccount acct = result.getSignInAccount();

                    assert acct != null;
                    signInGoogleAccount(acct);
                }
                break;
            case GoogleCalendarUtil.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    GoogleCalendarUtil.getUtil().setupSync(this);
                } else {
                    makeToast("Authorization not given");
                }
                break;
        }
    }

    private void signInGoogleAccount(GoogleSignInAccount account) {
        signedIn = true;
        String personName = account.getDisplayName();
        String personEmail = account.getEmail();
        Uri personPhoto = account.getPhotoUrl();

        Timber.d("personName %s", personName);
        Timber.d("personEmail %s", personEmail);
        Timber.d("personPhoto %s", personPhoto);

        View headerLayout = mainDrawer.getHeaderView(0);
        TextView nameTV = headerLayout.findViewById(R.id.googleAccountNameTV);
        TextView emailTV = headerLayout.findViewById(R.id.googleAccountEmailTV);
        ImageView profileIV = headerLayout.findViewById(R.id.googleAccountImage);
        Button loginOut = headerLayout.findViewById(R.id.logInOutButton);

        nameTV.setText(personName);
        nameTV.setVisibility(View.VISIBLE);
        emailTV.setText(personEmail);
        emailTV.setVisibility(View.VISIBLE);
        loginOut.setText(getString(R.string.signOut));
        Uri uri = account.getPhotoUrl();
        Picasso.get().load(uri)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.sym_def_app_icon)
                .into(profileIV);

        GoogleCalendarUtil.getUtil().setAccount(account);
        GoogleCalendarUtil.getUtil().setupSync(this);
    }

    private void signOutGoogleAccount() {
        signedIn = false;
        Timber.i("Logging out account");

        View headerLayout = mainDrawer.getHeaderView(0);
        TextView nameTV = headerLayout.findViewById(R.id.googleAccountNameTV);
        TextView emailTV = headerLayout.findViewById(R.id.googleAccountEmailTV);
        ImageView profileIV = headerLayout.findViewById(R.id.googleAccountImage);
        Button loginOut = headerLayout.findViewById(R.id.logInOutButton);

        nameTV.setVisibility(View.GONE);
        emailTV.setVisibility(View.GONE);
        loginOut.setText(getString(R.string.signIn));

        profileIV.setImageResource(R.drawable.iam_small_square);

        GoogleCalendarUtil.getUtil().signOut();
    }

    public void signInOut(View view) {
        if (signedIn) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(CalendarScopes.CALENDAR))
                    .build();
//
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mGoogleSignInClient.signOut();
            signOutGoogleAccount();
            Timber.d("signed out!");
        } else {
            Timber.d("Signing in...");
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(CalendarScopes.CALENDAR))
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GoogleCalendarUtil.REQUEST_ACCOUNT_PICKER);
            Timber.d("Starting activity for signing in...");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    public void updateTickingTimer() {
        currentItemTv.setText(StateModel.getModel().getCurrentItemName());
        currentItemMaxTime.setText(TimeFormatter.intToMinutesAndSeconds(StateModel.getModel().getCurrentItemMaxTime()));
        currentItemTimeTv.setText(TimeFormatter.intToMinutesAndSeconds(StateModel.getModel().getCurrentItemTime()));
        timePassedTV.setText(TimeFormatter.intToMinutesAndSeconds(StateModel.getModel().getTotalTimePassed()));
    }

    public void updateTotalTime() {
        mTvTotal.setText(TimeFormatter.intToMinutesAndSeconds(StateModel.getModel().getTotalTime()));
    }

    public void updateState() {
        switch (StateModel.getModel().getState()) {
            case StateModel.PAUSED:
                playButton.setImageResource(android.R.drawable.ic_media_play);
                break;
            case StateModel.RUNNING:
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                break;
            case StateModel.FINISHED:
                playButton.setImageResource(android.R.drawable.ic_menu_revert);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainReceiver.RECEIVER_ACTION);
        registerReceiver(receiver, filter);
        Timber.d("OnStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateState();
        updateTickingTimer();
        updateTotalTime();
        if (StateModel.getModel().getState() == StateModel.RATING)
            startRating();
        Timber.d("OnResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("OnPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        Timber.d("OnStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("OnDestroy");
    }

    private void setupSilent() {
        ConstraintLayout layout = (ConstraintLayout) mainDrawer.getMenu().findItem(R.id.checkBox_silent)
                .getActionView();
        silentCheckBox = layout.findViewById(R.id.checkBox);

        boolean silent = prefs.getBoolean(GlobalPreferences.KEY_SILENT, false);

        audioManager = (AudioManager) getSystemService(Context
                .AUDIO_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);

        originalRingerMode = audioManager.getRingerMode();
        originalMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        originalSystemVolume = audioManager.getStreamVolume(AudioManager
                .STREAM_SYSTEM);
        Timber.d("originalRingerMode: %s", originalRingerMode);

        if (silent) {
            turnOnSilent();
        }

        silentCheckBox.setOnClickListener(v -> {
            boolean turningOn = silentCheckBox.isChecked();
            boolean changed;

            Timber.d("Status: %s", turningOn);

            if (turningOn) {
                changed = turnOnSilent();
            } else {
                turnOffSilent();
                silentCheckBox.setChecked(false);
                changed = true;
            }
            if (changed) {
                prefs.edit().putBoolean(GlobalPreferences.KEY_SILENT, turningOn).apply();
            }
        });
    }

    private boolean turnOnSilent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !notificationManager.isNotificationPolicyAccessGranted()) {

            makeToast("Please give access to silent mode");
            Intent intent = new Intent(android.provider.Settings
                    .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivityForResult(intent, ON_DO_NOT_DISTURB_CALLBACK_CODE);

            return false;
        } else {
            silentOn();
            return true;
        }
    }

    private void silentOn() {
//        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        silentCheckBox.setChecked(true);
        int amStreamMusicMaxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int vol = prefs.getInt(GlobalPreferences.KEY_VOLUME, amStreamMusicMaxVol * 3 / 4);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
    }

    private void turnOffSilent() {
        Timber.d("Turn off silent");
        audioManager.setRingerMode(originalRingerMode);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalSystemVolume, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMusicVolume, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int streamMusicMaxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                int newVol = vol - 1;
                if (newVol >= 0) {
                    prefs.edit().putInt(GlobalPreferences.KEY_VOLUME, newVol).apply();
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);
                }
            } else {
                int newVol = vol + 1;
                if (newVol <= streamMusicMaxVol) {
                    prefs.edit().putInt(GlobalPreferences.KEY_VOLUME, newVol).apply();
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);
                }
            }
            vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            makeToast("Volume set to: " + vol + "/" + streamMusicMaxVol);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Timber.d("onBackPressed");
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();

            turnOffSilent();

            Intent stopIntent = new Intent(this, ControllerService.class);
            stopIntent.setAction(ControllerService.STOP_FOREGROUND_ACTION);
            startService(stopIntent);
        }
    }

    private void setupStayAwake() {
        ConstraintLayout layout = (ConstraintLayout) mainDrawer.getMenu().findItem(R.id.checkBox_stay_awake)
                .getActionView();

        CheckBox checkBox = layout.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(GlobalPreferences.KEY_STAY_AWAKE, isChecked).apply();
            if (isChecked)
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        });

        boolean stayAwake = prefs.getBoolean(GlobalPreferences.KEY_STAY_AWAKE, false);

        if (stayAwake) {
            checkBox.setChecked(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else
            checkBox.setChecked(false);
    }

    private void setupVibration() {
        ConstraintLayout layout = (ConstraintLayout) mainDrawer.getMenu().findItem(R.id.checkBox_vibrate)
                .getActionView();
        CheckBox checkBox = layout.findViewById(R.id.checkBox);

        if (prefs.getBoolean(GlobalPreferences.KEY_VIBRATE, false)) {
            checkBox.setChecked(true);
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(GlobalPreferences.KEY_VIBRATE, isChecked).apply());
    }

    private void setupHumanVoice() {
        ConstraintLayout layout = (ConstraintLayout) mainDrawer.getMenu().findItem(R.id.checkBox_human_voice)
                .getActionView();
        CheckBox checkBox = layout.findViewById(R.id.checkBox);

        if (prefs.getBoolean(GlobalPreferences.KEY_HUMAN_VOICE, true)) {
            checkBox.setChecked(true);
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(GlobalPreferences.KEY_HUMAN_VOICE, isChecked).apply());
    }

    private void setupSpinner() {
        //EXTRA TIME SPINNER
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.string_array_extra_time, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        adapter.setDropDownViewTheme(R.style.BlackText);

        Spinner spinner = (Spinner) iamDrawer.getMenu().findItem(R.id
                .spinner_fixed_addition).getActionView();
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("EXTRA SPINNER");
                String newValue = (String) parent.getItemAtPosition(position);
                prefs.edit().putString(GlobalPreferences.KEY_SPINNER_EXTRA_TIME, newValue).apply();
                notifyUpdateTimes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String spinnerValue = prefs.getString(GlobalPreferences.KEY_SPINNER_EXTRA_TIME, getString(R
                .string.default_extra_time));
        assert spinnerValue != null;

        int spinnerPosition = adapter.getPosition(spinnerValue);
        spinner.setSelection(spinnerPosition);

        // SHAVASNA SPINNER
        adapter = ArrayAdapter.createFromResource(this,
                R.array.string_array_shavasana_time, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner = (Spinner) iamDrawer.getMenu().findItem(R.id.spinner_shavasana).getActionView();
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("SHAVASNA SPINNER");
                String newValue = (String) parent.getItemAtPosition(position);
                prefs.edit().putString(GlobalPreferences.KEY_SPINNER_SHAVASANA, newValue).apply();
                notifyUpdateTimes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerValue = prefs.getString(GlobalPreferences.KEY_SPINNER_SHAVASANA, getString(R.string
                .default_shavasana));
        assert spinnerValue != null;

        spinnerPosition = adapter.getPosition(spinnerValue);
        spinner.setSelection(spinnerPosition);

        // END MEDITATION SPINNER
        adapter = ArrayAdapter.createFromResource(this,
                R.array.string_array_end_meditation, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner = (Spinner) iamDrawer.getMenu().findItem(R.id.spinner_end_meditation).getActionView();
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("MEDITATION SPINNER");
                String newValue = (String) parent.getItemAtPosition(position);
                prefs.edit().putString(GlobalPreferences.KEY_SPINNER_END_MEDITATION, newValue).apply();
                notifyUpdateTimes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerValue = prefs.getString(GlobalPreferences.KEY_SPINNER_END_MEDITATION, getString(R.string
                .default_end_meditation));
        assert spinnerValue != null;

        spinnerPosition = adapter.getPosition(spinnerValue);
        spinner.setSelection(spinnerPosition);
    }

    private void notifyUpdateTimes() {
        StateModel.getModel().updateTimes();
        Intent updateIntent = new Intent(MainActivity.this, ControllerService.class);
        updateIntent.setAction(ControllerService.UPDATE_ACTION);
        startService(updateIntent);
    }

    public void startPauseClick(View view) {
        switch (StateModel.getModel().getState()) {
            case StateModel.PAUSED:
                Intent playIntent = new Intent(this, ControllerService.class);
                playIntent.setAction(ControllerService.PLAY_ACTION);
                startService(playIntent);
                int vol = prefs.getInt(GlobalPreferences.KEY_VOLUME, 10);
                if (vol <= 1)
                    makeToast("Observe volume is very low: " + vol);
                break;
            case StateModel.RUNNING:
                Intent pauseIntent = new Intent(this, ControllerService.class);
                pauseIntent.setAction(ControllerService.PAUSE_ACTION);
                startService(pauseIntent);
                break;
            case StateModel.FINISHED:
                Intent resetIntent = new Intent(this, ControllerService.class);
                resetIntent.setAction(ControllerService.RESET_ACTION);
                startService(resetIntent);
                break;
        }
    }

    public void makeToast(String text) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, text,
                Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    public void startRating() {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        intent.putExtra(HistoryActivity.START_RATING_DIALOG, true);
        startActivity(intent);
    }
}