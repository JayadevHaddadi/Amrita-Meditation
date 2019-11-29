package edu.amrita.elearn.iamhelper.history;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.amrita.elearn.iamhelper.R;
import edu.amrita.elearn.iamhelper.database.IamEntry;
import edu.amrita.elearn.iamhelper.model.StateModel;
import edu.amrita.elearn.iamhelper.database.HistoryModel;
import edu.amrita.elearn.iamhelper.util.DateUtil;
import edu.amrita.elearn.iamhelper.util.GlobalPreferences;
import edu.amrita.elearn.iamhelper.util.GoogleCalendarUtil;

public class RatingDialog extends Dialog {
    private Date date;
    private MeditationsDialog.CreateEntryCallback callback;
    private String TAG = RatingDialog.class.getSimpleName();

    private IamEntry iamEntry;

    // USED FOR ADDING NEW ENTRY FROM MAIN HISTORY PAGE
    RatingDialog(Activity activity, Date date) {
        super(activity);
        this.date = date;
    }

    // USED FOR ALREADY EXISTING IAM
    RatingDialog(Activity activity, IamEntry iamEntry) {
        super(activity);
        this.iamEntry = iamEntry;
    }

    @Override
    public Bundle onSaveInstanceState() {
//        Bundle bundle = new Bundle();
//        bundle.putString(KEY_COMMENT,);
        return super.onSaveInstanceState();
    }

    public void setCallback(MeditationsDialog.CreateEntryCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        StateModel.getModel().ratingDone();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.history_rating_dialog);

        final RatingBar ratingBar = findViewById(R.id.ratingBar);
        ImageButton buttonSaveRating = findViewById(R.id.buttonSaveRating);
        final EditText etComment = findViewById(R.id.textViewRatingComment);
        final TextView textViewRating = findViewById(R.id.textViewRating);
        final TimePicker timePicker = findViewById(R.id.endTimePicker);
        final Spinner durationSpinner = findViewById(R.id.durationSpinner);
        final ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> dismiss());

        timePicker.setIs24HourView(true);

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 120; i++) {
            list.add(i);
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                list
        );
        durationSpinner.setAdapter(adapter);

        if (iamEntry != null) {
            ratingBar.setRating(iamEntry.getRating());
            etComment.setText(iamEntry.getComment());
            textViewRating.setText(String.valueOf(iamEntry.getRating()));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(iamEntry.getDate());
            timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(calendar.get(Calendar.MINUTE));
            durationSpinner.setSelection(iamEntry.getDuration() / 60);
        } else {
            int duration = StateModel.getModel().getTotalTimePassed();
            durationSpinner.setSelection(duration / 60 + 1);
        }

        ratingBar.setOnRatingBarChangeListener((RatingBar ratingBar1, float value, boolean b) -> {
            textViewRating.setText(String.valueOf((int) value));
        });

        buttonSaveRating.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String comment = etComment.getText().toString();

            if (iamEntry != null) {
                iamEntry.setRating(rating);
                iamEntry.setComment(comment);
                iamEntry.setDuration(durationSpinner.getSelectedItemPosition() * 60);
                DateUtil.setHourMinuteForDate(iamEntry.getDate(), timePicker.getHour(), timePicker.getMinute());
            } else if (date != null) {
                DateUtil.setHourMinuteForDate(date, timePicker.getHour(), timePicker.getMinute());
                iamEntry = new IamEntry(0, rating, comment, date, durationSpinner.getSelectedItemPosition() * 60);
            }

            new Thread(() -> {
                if (GlobalPreferences.getGoogleSync()) {
                    if (iamEntry.hasGoogleId())
                        GoogleCalendarUtil.getUtil().postUpdate(iamEntry);
                    else {
                        Event event = GoogleCalendarUtil.getUtil().postImmediateEntry(iamEntry);
                        iamEntry.setGoogleEvent(event);
                    }
                }
                HistoryModel.getModel().insertEntry(iamEntry);
            }).start();

            if (callback != null) {
                callback.op(iamEntry);
            } else {
                StateModel.getModel().ratingDone();
            }
            dismiss();
        });
    }
}