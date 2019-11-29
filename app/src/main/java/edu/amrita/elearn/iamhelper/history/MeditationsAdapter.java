package edu.amrita.elearn.iamhelper.history;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import edu.amrita.elearn.iamhelper.R;
import edu.amrita.elearn.iamhelper.database.IamEntry;
import edu.amrita.elearn.iamhelper.database.HistoryModel;
import edu.amrita.elearn.iamhelper.util.GlobalPreferences;
import edu.amrita.elearn.iamhelper.util.GoogleCalendarUtil;
import timber.log.Timber;

public class MeditationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = MeditationsAdapter.class.getSimpleName();
    private List<IamEntry> entriesForDate;
    private Activity activity;
    private static DateFormat dateTimeInstance = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);

    public MeditationsAdapter(List<IamEntry> entriesForDate, Activity activity) {
        this.entriesForDate = entriesForDate;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public class MeditationViewHolder extends RecyclerView.ViewHolder {
        private ImageView deleteIV;
        private TextView commentTV;
        private TextView ratingTV;
        private TextView timeTV;
        private TextView lengthTV;

        public ConstraintLayout layout;

        MeditationViewHolder(ConstraintLayout layout) {
            super(layout);
            this.layout = layout;
            timeTV = layout.findViewById(R.id.time_TV);
            lengthTV = layout.findViewById(R.id.length_TV);
            ratingTV = layout.findViewById(R.id.rating_TV);
            commentTV = layout.findViewById(R.id.comment_TV);
            deleteIV = layout.findViewById(R.id.delete_IV);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.history_meditation_item, parent, false);
        return new MeditationViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder mainHolder, int position) {
        final IamEntry iamEntry = entriesForDate.get(position);

        MeditationViewHolder holder = (MeditationViewHolder) mainHolder;
        holder.layout.setOnClickListener(v -> {
            RatingDialog ratingDialog = new RatingDialog(activity,iamEntry);
            ratingDialog.setCallback(iamEntry1 -> {
                activity.runOnUiThread(() ->
                        notifyItemChanged(mainHolder.getAdapterPosition()));
            });
            ratingDialog.show();
        });

        holder.timeTV.setText(dateTimeInstance.format(iamEntry.getDate()));
        holder.lengthTV.setText(iamEntry.getDurationInMin());
        holder.ratingTV.setText(String.valueOf(iamEntry.getRating()));
        holder.commentTV.setText(iamEntry.getComment());

        holder.deleteIV.setOnClickListener(v -> new Thread(() -> {
            if (GlobalPreferences.getGoogleSync()) {
                GoogleCalendarUtil.getUtil().deleteEntry(iamEntry.getGoogleId());
            }
            HistoryModel.getModel().deleteEntry(iamEntry);
            entriesForDate.remove(mainHolder.getAdapterPosition());
            activity.runOnUiThread(() ->
                    notifyItemRemoved(mainHolder.getAdapterPosition())
            );
        }).start());


        Timber.d("BINDING ITEM in POS: %s", position);
    }

    @Override
    public int getItemCount() {
        return entriesForDate.size();
    }
}