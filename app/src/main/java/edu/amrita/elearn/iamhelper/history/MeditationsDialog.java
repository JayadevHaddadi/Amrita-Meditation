package edu.amrita.elearn.iamhelper.history;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.amrita.elearn.iamhelper.R;
import edu.amrita.elearn.iamhelper.database.IamEntry;
import edu.amrita.elearn.iamhelper.database.HistoryModel;
import timber.log.Timber;

class MeditationsDialog extends Dialog {
    private MeditationsAdapter mAdapter;
    private List<IamEntry> entriesForDate;
    private String TAG = MeditationsDialog.class.getSimpleName();
    private Activity activity;
    private Date date;

    MeditationsDialog(Activity activity, Date date) {
        super(activity);
        this.activity = activity;
        this.date = date;
    }

    interface CreateEntryCallback {
        void op(IamEntry iamEntry);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.history_meditations_dialog);
        DateFormat format = SimpleDateFormat.getDateInstance();

        TextView dateTV = findViewById(R.id.date_TV);
        dateTV.setText(format.format(date));

        ImageView addIV = findViewById(R.id.add_IV);
        addIV.setOnClickListener(v -> {
            Timber.d("showing add!");
            RatingDialog ratingDialog = new RatingDialog(activity, date);
            ratingDialog.setCallback(iamEntry -> {
                entriesForDate.add(iamEntry);
                mAdapter.notifyDataSetChanged();
                Timber.d("notifyDataSetChanged");
            });
            ratingDialog.show();
            Timber.d("showing add!2");
        });

        ImageView dismissIV = findViewById(R.id.dismiss_IV);
        dismissIV.setOnClickListener(v -> {
            dismiss();
        });

        final RecyclerView recyclerView = findViewById(R.id.meditations_rv);

        final LinearLayoutManager rvLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(rvLayoutManager);

        this.entriesForDate = new ArrayList<>();
        mAdapter = new MeditationsAdapter(entriesForDate, activity);

        new Thread(() -> {
            Timber.d("Getting data for getDate: %s", date);
            List<IamEntry> entriesForDate = HistoryModel.getModel().getEntriesForDate(date);
            MeditationsDialog.this.entriesForDate.addAll(entriesForDate);
            Timber.d("Got data; SIZE: %s", MeditationsDialog.this.entriesForDate.size());
            for (IamEntry entry : MeditationsDialog.this.entriesForDate) {
                entry.print();
            }
            recyclerView.post(() -> mAdapter.notifyDataSetChanged());
        }).start();

        recyclerView.setAdapter(mAdapter);
    }
}
