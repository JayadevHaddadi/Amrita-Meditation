package edu.amrita.elearn.iamhelper.iamparts;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import edu.amrita.elearn.iamhelper.R;
import edu.amrita.elearn.iamhelper.model.StateModel;
import edu.amrita.elearn.iamhelper.model.IamPart;
import edu.amrita.elearn.iamhelper.util.TimeFormatter;

public class IamPartsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = IamPartsAdapter.class.getSimpleName();
    private IamPartsActivity iamPartsActivity;
    private int currentSelected = 0;

    IamPartsAdapter(IamPartsActivity iamPartsActivity) {
        this.iamPartsActivity = iamPartsActivity;
        currentSelected = StateModel.getModel().getCurrentItemPosition();
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public class IamViewHolder extends RecyclerView.ViewHolder {
        private ImageView playButton;
        private FrameLayout background;
        private TextView tvTotalTime;
        private TextView tvTitle;
        private TextView tvDescription;

        public ConstraintLayout layout;

        IamViewHolder(ConstraintLayout layout) {
            super(layout);
            this.layout = layout;
            tvTitle = layout.findViewById(R.id.firstLine);
            tvDescription = layout.findViewById(R.id.secondLine);
            tvTotalTime = layout.findViewById(R.id.tv_total_time);
            background = layout.findViewById(R.id.background_layout);
            playButton = layout.findViewById(R.id.play_button);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.iam_parts_activity, parent, false);
        return new IamViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder mainHolder, final int position) {
        final IamPart iamItem = StateModel.getModel().getIamParts().get(position);

        IamViewHolder holder = (IamViewHolder) mainHolder;
        holder.tvTitle.setText(iamItem.getName());
        holder.tvDescription.setText(iamItem.getDesciption());
        holder.tvTotalTime.setText(TimeFormatter.formatIntToSecondsOrMinutesWithValue(iamItem.getTime()));

        View.OnClickListener onClickListener = v -> {
            currentSelected = position;
            notifyDataSetChanged();
        };
        holder.layout.setOnClickListener(onClickListener);
        holder.tvTitle.setOnClickListener(onClickListener);
        holder.tvDescription.setOnClickListener(onClickListener);

        if (position == currentSelected) {
            holder.playButton.setVisibility(View.VISIBLE);
            holder.playButton.setOnClickListener(v -> {
                StateModel.getModel().setCurrentItemPosition(position);
                StateModel.getModel().setCurrentItemTime(0);
                if(StateModel.getModel().getState() == StateModel.RUNNING)
                    StateModel.getModel().soundCurrentItem(false);
                iamPartsActivity.finish();
            });
            holder.background.setBackgroundResource(R.drawable.highlighted_shape);
            holder.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
            holder.tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            holder.tvTotalTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        } else {
            holder.playButton.setVisibility(View.GONE);
            holder.background.setBackgroundResource(R.drawable.unhighlated_shape);
            holder.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            holder.tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            holder.tvTotalTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        }
    }

    @Override
    public int getItemCount() {
        return StateModel.getModel().getIamParts().size();
    }
}