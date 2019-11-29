package edu.amrita.elearn.iamhelper.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import com.google.api.services.calendar.model.Event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.amrita.elearn.iamhelper.util.TimeFormatter;

@Entity(tableName = "iamtable")
public class IamEntry {

    private static final String TAG = IamEntry.class.getSimpleName();
    private static SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static DateFormat DATE_FORMATTER = SimpleDateFormat.getDateTimeInstance();

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int rating;
    private String comment;
    private int duration;
    @ColumnInfo(name = "created_at")
    private Date date;
    @ColumnInfo(name = "google_id")
    private String googleId;
    @ColumnInfo(name = "updated_at_google")
    private Date updatedAtGoogle;
    @ColumnInfo(name = "updated_locally")
    private boolean updatedLocallySinceLastGoogleSync = false;

    public IamEntry(int id, int rating, String comment, Date date, int duration) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDurationInMin() {
        return TimeFormatter.intRoundToMinutesWithValue(duration);
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public Date getUpdatedAtGoogle() {
        return updatedAtGoogle;
    }

    public void setUpdatedAtGoogle(Date updatedAtGoogle) {
        this.updatedAtGoogle = updatedAtGoogle;
    }

    public boolean hasGoogleId() {
        return googleId != null;
    }

    public void print() {
        Log.d(TAG, "--IAMENTRY--");
        Log.d(TAG, "id: " + id);
        Log.d(TAG, "Comment: " + comment);
        Log.d(TAG, "date: " + date);
        Log.d(TAG, "googleId: " + googleId);
        Log.d(TAG, "updatedAtGoogle: " + updatedAtGoogle);
    }

    public boolean hasGoogleUpdated() {
        return updatedAtGoogle != null;
    }

    public boolean isUpdatedLocallySinceLastGoogleSync() {
        return updatedLocallySinceLastGoogleSync;
    }

    public void setUpdatedLocallySinceLastGoogleSync(boolean updatedLocallySinceLastGoogleSync) {
        this.updatedLocallySinceLastGoogleSync = updatedLocallySinceLastGoogleSync;
    }

    public void setGoogleEvent(Event googleEvent) {
        if(googleEvent == null)
            return;

        setGoogleId(googleEvent.getId());
        try {
            setUpdatedAtGoogle(FORMATTER.parse(googleEvent.getUpdated().toString()));
//                            iamEntry.setUpdatedAtGoogle(sdf.parse(event.getUpdated().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        setUpdatedAtGoogle(new Date(googleEvent.getUpdated().getValue()));
    }
}
