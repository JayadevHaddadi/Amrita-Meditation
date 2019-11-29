package edu.amrita.elearn.iamhelper.util;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import edu.amrita.elearn.iamhelper.R;
import edu.amrita.elearn.iamhelper.database.HistoryModel;
import edu.amrita.elearn.iamhelper.database.IamEntry;
import edu.amrita.elearn.iamhelper.main.MainActivity;
import timber.log.Timber;

public class GoogleCalendarUtil {

    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static String SADHANA_STRING;

    private static GoogleCalendarUtil singelton;
    private GoogleAccountCredential mCredential;
    private final String TAG = GoogleCalendarUtil.class.getSimpleName();
    private Calendar googleCalendarService;
    private GoogleSignInAccount account;

    public static GoogleCalendarUtil getUtil() {
        if (singelton == null)
            singelton = new GoogleCalendarUtil();
        return singelton;
    }

    public void signOut() {
        mCredential.setSelectedAccount(null);
    }

    public void setupSync(final Context context) {
        SADHANA_STRING = context.getString(R.string.sadhana_google_calendar);

        if (!isGooglePlayServicesAvailable(context)) {
            Timber.d("Google Play not available!");
            acquireGooglePlayServices(context);
        } else if (account == null) {
            Timber.d("Not logged in!");
            ((MainActivity) context).chooseAccount();
        } else if (!isDeviceOnline(context)) {
            Timber.d("Device not online!");
            ((MainActivity) context).makeToast("You are offline!\nCant post calender updates!");
        } else {
            mCredential = GoogleAccountCredential.usingOAuth2(
                    context, Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff())
                    .setSelectedAccountName(Objects.requireNonNull(account.getAccount()).name);
            Timber.d("Logged in to: %s", mCredential.getSelectedAccountName());

            completeSync();
            GlobalPreferences.setGoogleSync(true);
        }
    }

    private void completeSync() {
        Timber.d("http");
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Timber.d("calander");
        googleCalendarService = new Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Iam Helper")
                .build();

        new Thread(() -> {
            String sadhanaCalendarID = GlobalPreferences.getSadhanaCalendarID();

            List<IamEntry> iamList = HistoryModel.getModel().getStaticEntries();
            List<Event> eventList = null;
            try {
                Log.d(TAG, "sending");
                String pageToken = null;
                boolean foundSadhana = false;
                calenderLoop:
                do {
                    CalendarList calendarList = googleCalendarService.calendarList().list().setPageToken(pageToken).execute();
                    List<CalendarListEntry> items = calendarList.getItems();

                    for (CalendarListEntry calendarListEntry : items) {
//                            Log.d(TAG, "\nSAD: " + SADHANA_STRING);
                        Log.d(TAG, "NAME: " + calendarListEntry.getSummary());
                        Log.d(TAG, "EQUAL: " + calendarListEntry.getSummary().equals(SADHANA_STRING));
                        Log.d(TAG, "ID: " + calendarListEntry.getId());
                        if (calendarListEntry.getSummary().equals(SADHANA_STRING)) {
                            Log.d(TAG, "FOUND!");
                            foundSadhana = true;
                            sadhanaCalendarID = calendarListEntry.getId();
                            break calenderLoop;
                        }
                    }
                    pageToken = calendarList.getNextPageToken();
                } while (pageToken != null);
                if (!foundSadhana) {
                    com.google.api.services.calendar.model.Calendar calendar =
                            new com.google.api.services.calendar.model.Calendar();
                    calendar.setSummary(SADHANA_STRING);
                    calendar.setDescription("Used for \"Iam Helper\" app");
                    com.google.api.services.calendar.model.Calendar sadhanaCalendar = googleCalendarService
                            .calendars().insert(calendar).execute();
                    sadhanaCalendarID = sadhanaCalendar.getId();
                    Log.d(TAG, "Created new Sadhana calendar with ID: " + sadhanaCalendarID);
                    CalendarListEntry sadhanaListEntry = googleCalendarService.calendarList().get(sadhanaCalendarID)
                            .execute();
                    // relese: 3: RED? 23 purple for testing
                    Log.d(TAG, "COLOR");
                    Log.d(TAG, "" + R.integer.google_calendar_color);
                    Log.d(TAG, String.valueOf(R.integer.google_calendar_color));
                    sadhanaListEntry.setColorId(String.valueOf(R.integer.google_calendar_color));
                    googleCalendarService.calendarList().update(sadhanaListEntry.getId(), sadhanaListEntry).execute();
                }
                GlobalPreferences.setSadhanaCalendarID(sadhanaCalendarID);
                Log.d(TAG, "Setting up " + SADHANA_STRING + " calendar successful!");

                // NOW CHECKING ALL EVENTS
                DateTime now = new DateTime(System.currentTimeMillis());
                Events events = googleCalendarService.events().list(sadhanaCalendarID)
                        .setTimeMax(now)
                        .setSingleEvents(true)
                        .execute();
//                            .setOrderBy("startTime")
                eventList = events.getItems();

            } catch (UserRecoverableAuthIOException e) {
                Log.d(TAG, "UserRecoverableAuthIOException");
//                ((MainActivity) context).startActivityForResult(e.getIntent(),
//                        REQUEST_AUTHORIZATION);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "error: " + e.getMessage());
            }

            if (eventList == null) {
                Log.d(TAG, "EventList null!");
                return;
            }

            // UPLOADING LOCAL ITEMS THAT WERE NOT POSTED BEFORE
            Log.d(TAG, "Size: " + iamList.size());
            for (IamEntry iamEntry : iamList) {
                if (!iamEntry.hasGoogleId()) {
                    Event event = postImmediateEntry(iamEntry);
                    iamEntry.setGoogleEvent(event);
                    HistoryModel.getModel().insertEntry(iamEntry);
                }
                iamEntry.print();
            }

            // UPDATING LOCAL DB WITH ALL ONLINE ITEMS
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            for (Event event : eventList) {
                try {
                    System.out.println("SUMMERY: " + event.getSummary());
                    if (!event.getSummary().equals("Iam Meditation"))
                        continue;
                    System.out.println("ID: " + event.getId());
                    System.out.println("Time: " + event.getStart().toString());
                    System.out.println("Time: " + new Date(event.getStart().getDateTime().getValue()).toString());
                    System.out.println(event.getDescription());
                    System.out.println(event.getUpdated().toString());
                    System.out.println(sdf.parse(event.getUpdated().toString()));
                    boolean found = false;
                    for (IamEntry entry : iamList) {
                        if (entry.getGoogleId().equals(event.getId())) {
                            Log.d(TAG, "ID EXISTS LOCALLY");
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Log.d(TAG, "NOT FOUND!");
                        String description = event.getDescription();
                        String[] split = description.split("\n");
                        Integer rating = Integer.valueOf(split[0].split(" ")[1]);
                        String[] duration = split[1].split(" ");
                        String[] minSec = duration[1].split(":");
                        int seconds = Integer.valueOf(minSec[0]) * 60 + Integer.valueOf(minSec[1]);
                        StringBuilder comment = new StringBuilder();
                        for (int lines = 3; lines < split.length; lines++)
                            comment.append(split[lines]).append("\n");
                        Date date = new Date(event.getStart().getDateTime().getValue());
                        IamEntry iamEntry = new IamEntry(0, rating, comment.toString(), date, seconds);
                        iamEntry.setGoogleEvent(event);
                        HistoryModel.getModel().insertEntry(iamEntry);
                        Log.d(TAG, "ENTRY DONE!");
                    }
                } catch (Exception e) {
                    Log.d(TAG, "SOMETHING WENT WRONG DUDUUDEEE!");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Event postImmediateEntry(IamEntry iamEntry) {
        final Event localEvent = eventFromEntry(iamEntry);
        String sadhanaCalendarID = GlobalPreferences.getSadhanaCalendarID();

        Event googleEvent = null;
        try {
            Log.d(TAG, "sending");
            googleEvent = googleCalendarService.events().insert(sadhanaCalendarID, localEvent).execute();
            String id = googleEvent.getId();
            Log.d(TAG, "Successful entry with Id: " + id);
        } catch (UserRecoverableAuthIOException e) {
            Log.d(TAG, "UserRecoverableAuthIOException");
//                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "error: " + e.getMessage());
        }
        return googleEvent;
    }

    private Event eventFromEntry(IamEntry iamEntry) {
        String description = ("Rating: " + iamEntry.getRating() + "\n") +
                "Duration: " + iamEntry.getDurationInMin() + "\n" +
                "Comment:\n" + iamEntry.getComment();
        final Event localEvent = new Event()
                .setSummary("Iam Meditation")
                .setDescription(description);

        Date endDate = iamEntry.getDate();
        Date startDate = new Date(endDate.getTime() - iamEntry.getDuration() * 1000);
        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(startDate));
        localEvent.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(endDate));
        localEvent.setEnd(end);

        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false);
        localEvent.setReminders(reminders);
        return localEvent;
    }

    public void postUpdate(IamEntry iamEntry) {
        final Event localEvent = eventFromEntry(iamEntry);
        String sadhanaCalendarID = GlobalPreferences.getSadhanaCalendarID();

        try {
            Log.d(TAG, "posting update");
            googleCalendarService.events().update(sadhanaCalendarID, iamEntry.getGoogleId(), localEvent).execute();
            Log.d(TAG, "Successful entry with Id: " + iamEntry.getGoogleId());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "error: " + e.getMessage());
        }
    }

    public void setAccountName(String accountName) {
        GlobalPreferences.setAccountName(accountName);
        mCredential.setSelectedAccountName(accountName);
    }

    private boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Attempt to resolve a missing, out-of-getDate, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices(Context context) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode, context);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of getDate.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    private void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode, Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                ((MainActivity) context),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void syncDown() {
        completeSync();
    }

    public void deleteEntry(String googleId) {
        String sadhanaCalendarID = GlobalPreferences.getSadhanaCalendarID();
        Log.d(TAG, "sadhanaID: " + sadhanaCalendarID);
        if (sadhanaCalendarID == null)
            return;
        try {
            googleCalendarService.events().delete(sadhanaCalendarID, googleId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAccount(GoogleSignInAccount acct) {
        this.account = acct;
    }
}
