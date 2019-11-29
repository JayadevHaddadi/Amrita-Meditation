package edu.amrita.elearn.iamhelper.history;

import android.app.Application;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import edu.amrita.elearn.iamhelper.R;
import edu.amrita.elearn.iamhelper.database.IamEntry;
import edu.amrita.elearn.iamhelper.model.StateModel;
import edu.amrita.elearn.iamhelper.database.HistoryModel;
import edu.amrita.elearn.iamhelper.util.TimeFormatter;
import ru.cleverpumpkin.calendar.CalendarDate;
import ru.cleverpumpkin.calendar.CalendarView;
import timber.log.Timber;

public class HistoryActivity extends AppCompatActivity {
    public static final String START_RATING_DIALOG = "RATING_DIALOG";

    private static final String TAG = HistoryActivity.class.getSimpleName();
    private static DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("History");
        toolbar.setTitleTextColor(getResources().getColor(R.color.textColor));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        setSupportActionBar(toolbar);

        if (StateModel.getModel().getState() == StateModel.RATING) {
            Calendar cal = Calendar.getInstance();
            Date today = new Date(System.currentTimeMillis() - (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) * 60 * 1000);
            RatingDialog ratingDialog = new RatingDialog(this, today);
            ratingDialog.show();
        }

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Timber.d("ITEMz:  %s", id);
        Timber.d("R.id.action_add:  %s", R.id.action_add);

        if (id == R.id.action_add) {
            Calendar cal = Calendar.getInstance();
            Date today = new Date(System.currentTimeMillis() - (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) * 60 * 1000);
            RatingDialog ratingDialog = new RatingDialog(this, today);
            ratingDialog.show();
            return true;
        }
//        else if (id == R.id.action_sync_down) {
//            GoogleCalendarUtil.getUtil().syncDown();
//        }
        else
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    interface Intf {
    }

    public static class CalendarFragment extends Fragment {
        private CalendarView calendarView;

        @Override
        public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.history_calendar_fragment, container, false);
            calendarView = rootView.findViewById(R.id.calendar_view2);
            CalendarDate initialDate = new CalendarDate(System.currentTimeMillis());
            calendarView.setupCalendar(initialDate,
                    null, new CalendarDate(System.currentTimeMillis()),
                    CalendarView.SelectionMode.NON,
                    new ArrayList<>(),
                    Calendar.MONDAY,
                    false
            );
            //TODO backup that many days missing this month
            calendarView.moveToDate(new CalendarDate(System.currentTimeMillis() - 10 * 24 * 3600 * 1000));

            calendarView.setOnDateClickListener(calendarDate -> {
                MeditationsDialog dialog = new MeditationsDialog(getActivity(), calendarDate.getDate());
                dialog.show();
                return null;
            });

            HistoryModel.getModel().getLiveRatings().observe(this, newRatingList -> {
                Log.d(TAG, "Updating calendar");
                assert newRatingList != null;
                updateCalendar(newRatingList);
            });

            return rootView;
        }

        private void updateCalendar(List<IamEntry> ratingList) {
            ArrayList<CalendarView.DateIndicator> dateIndicators = new ArrayList<>();
            for (IamEntry entry : ratingList) {
                final Date itemDate = entry.getDate();
                dateIndicators.add(new CalendarView.DateIndicator() {
                    @NotNull
                    @Override
                    public CalendarDate getDate() {
                        return new CalendarDate(itemDate.getTime());
                    }

                    @Override
                    public int getColor() {
                        return Objects.requireNonNull(getContext()).getResources().getColor(R.color.colorPrimaryDark);
                    }
                });
            }
            calendarView.setDatesIndicators(dateIndicators);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return new CalendarFragment();
            else
                return new GraphFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class GraphFragment extends Fragment {

//        private LineGraphSeries<DataPointInterface> averageSeries;
        private LineGraphSeries<DataPointInterface> runningAverageSeries;
        private LineGraphSeries<DataPointInterface> meditationSeries;
        private TextView meditationsTV;
        private TextView averageTV;
        private static List<IamEntry> ratingList;
        private GraphView graphView;
        private TextView dayCountTV;

        enum Duration {
            MONTH,
            YEAR,
            ALL_TIME
        }
        Duration duration = Duration.MONTH;

        @Override
        public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.history_graph_fragment, container, false);

            RadioGroup radioSexGroup = rootView.findViewById(R.id.graphDurationRadioGroup);
            radioSexGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.monthRadioButton) {
                    duration = Duration.MONTH;
                } else if (checkedId == R.id.yearRadioButton) {
                    duration = Duration.YEAR;
                } else if (checkedId == R.id.allTimeRadioButton) {
                    duration = Duration.ALL_TIME;
                }
                updateGraph();
            });

            averageTV = rootView.findViewById(R.id.graph_average_TV);
            meditationsTV = rootView.findViewById(R.id.graph_meditations_TV);
            graphView = rootView.findViewById(R.id.graph);
            dayCountTV = rootView.findViewById(R.id.graphDayCountTV);
            assert graphView != null;

            // setting 'now's time to half hour in the future so we can se current entry properly
            Date today = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
            long monthInMilli = 86400000L * 30;
            Date monthAgo = new Date(today.getTime() - monthInMilli);

            graphView.getLegendRenderer().setVisible(true);
            graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
            graphView.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);

            // set getDate label formatter
            graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        Date date = new Date((long) value);
                        return "" + dateFormat.format(date);
                    } else {
                        return "";
                    }
                }
            });
            graphView.getGridLabelRenderer().setNumHorizontalLabels(7); // every second day
            graphView.getGridLabelRenderer().setNumVerticalLabels(6);
            graphView.getGridLabelRenderer().setHorizontalLabelsAngle(45);
            graphView.getGridLabelRenderer().setVerticalLabelsColor(R.color.textColor);
            graphView.getGridLabelRenderer().setHorizontalLabelsColor(R.color.textColor);
            graphView.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(R.color.textColor);
            graphView.getGridLabelRenderer().setHumanRounding(false);

            // set manual x bounds to have nice steps
            graphView.getViewport().setMinX(monthAgo.getTime());
            graphView.getViewport().setMaxX(today.getTime());
            graphView.getViewport().setXAxisBoundsManual(true);

//            Application application = Objects.requireNonNull(getActivity()).getApplication();
//            averageSeries = new LineGraphSeries<>();
//            averageSeries.setTitle(getString(R.string.average_graph_lebel));
//            averageSeries.setThickness(5);
//            averageSeries.setColor(ResourcesCompat.getColor(application.getResources(), R.color
//                    .graph_average_color, null));

            Application application = Objects.requireNonNull(getActivity()).getApplication();
            runningAverageSeries = new LineGraphSeries<>();
            runningAverageSeries.setTitle("Running average");
            runningAverageSeries.setThickness(5);
            runningAverageSeries.setColor(ResourcesCompat.getColor(application.getResources(), R.color
                    .graph_average_color, null));


            Paint paint = new Paint();
            int meditationColor = ResourcesCompat.getColor(application.getResources(), R.color
                    .graph_color, null);
            paint.setColor(meditationColor);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(0);

            meditationSeries = new LineGraphSeries<>();
            meditationSeries.setDrawDataPoints(true);
            meditationSeries.setCustomPaint(paint);
            meditationSeries.setDrawAsPath(false); //set true to be false
            meditationSeries.setDataPointsRadius(8);
            meditationSeries.setColor(meditationColor);
            meditationSeries.setTitle(getString(R.string.meditations_graph_label));

//            graphView.getSecondScale().addSeries(averageSeries);
            graphView.getSecondScale().addSeries(runningAverageSeries);
            graphView.getSecondScale().addSeries(meditationSeries);
            graphView.getSecondScale().setLabelFormatter(new DefaultLabelFormatter());
            graphView.getSecondScale().setMinY(0);
            graphView.getSecondScale().setMaxY(10);
            Log.d(TAG, "Finished graph");

            meditationSeries.setOnDataPointTapListener((series, dataPoint) -> {
                Log.d(TAG, "getX: " + dataPoint.getX());
                Log.d(TAG, "getTitle: " + series.getTitle());

                new GetEntry().execute(new Date((long) dataPoint.getX()), null);
            });

            HistoryModel.getModel().getLiveRatings().observe(this, newRatingList -> {
                ratingList = newRatingList;
                Log.d(TAG, "Updating Graph");
                assert newRatingList != null;
                updateGraph();
            });

            return rootView;
        }

        class GetEntry extends AsyncTask<Date, Void, IamEntry> {
            @Override
            protected IamEntry doInBackground(Date[] date) {
                return HistoryModel.getModel().getEntryForExactTime(date[0]);
            }

            @Override
            protected void onPostExecute(IamEntry iamEntry) {
                RatingDialog ratingDialog = new RatingDialog(getActivity(), iamEntry);
                ratingDialog.show();
            }
        }

        private static final long DAY_IN_MILLI = 24 * 3600 * 1000L;
        private static final long MONTH_IN_MILLI = 30 * DAY_IN_MILLI;
        private static final long YEAR_IN_MILLI = 365 * DAY_IN_MILLI;

        private void updateGraph() {
            Timber.i("Recreating graph");
            float sumRating = 0;
            ArrayList<DataPoint> points = new ArrayList<>();
            int meditationThisDuration = 0;
            Date today = new Date();
            Date durationStartDate = new Date();

            if(duration == Duration.MONTH){
                durationStartDate.setTime(today.getTime() - MONTH_IN_MILLI);
                dayCountTV.setText("30");
            } else if(duration == Duration.YEAR){
                durationStartDate.setTime(today.getTime() - YEAR_IN_MILLI);
                dayCountTV.setText("365");
            } else if(duration == Duration.ALL_TIME){
                if(ratingList.size()>0) {
                    Timber.d("OLDEST INSERT: %s", ratingList.get(0).getDate());
                    durationStartDate = new Date(ratingList.get(0).getDate().getTime() - 1000);
                    long durationInDays = (today.getTime() - ratingList.get(0).getDate().getTime()) / DAY_IN_MILLI;
                    dayCountTV.setText(String.valueOf(durationInDays));
                }
                else dayCountTV.setText("0");
            }

            ArrayList<DataPoint> runningAveragePoints = new ArrayList<>();
//            ArrayList<DataPoint> EMApoints = new ArrayList<>();
            double alpha = 0.95;
            int count = 0;
//            if(ratingList.size()>0)
//                runningAverage = ratingList.get(0).getRating();

            int tenth = ratingList.size()/10;
            double fiveDayEverage=0;
            for (IamEntry entry : ratingList) {
                if (count >= tenth)
                    break;
                fiveDayEverage += entry.getRating();
                count++;
            }

            fiveDayEverage = fiveDayEverage/count;
            double runningAverage = fiveDayEverage;

            count = 0;
            double EMA = 0;
            for (IamEntry entry : ratingList) {

                count++;
//                double wightedCount = (1-Math.pow((1-alpha),count))/(1-(1-alpha));
//                runningAverage = entry.getRating() + (1-alpha)* runningAverage;
//                double EMA = runningAverage/wightedCount;

                runningAverage = runningAverage * alpha + (1-alpha) * entry.getRating();
                runningAveragePoints.add(new DataPoint(entry
                        .getDate(),runningAverage));

//                double k = 2.0/(count+1);
//                EMA = entry.getRating() * k + EMA * (1-k);


//                EMApoints.add(new DataPoint(entry
//                        .getDate(),EMA));


                final Date itemDate = entry.getDate();
                if (itemDate.after(durationStartDate)) {
                    meditationThisDuration++;
                    points.add(new DataPoint(entry
                            .getDate(), entry.getRating()));
                    sumRating += entry.getRating();
//                    Log.d(TAG, "getDate: " + entry.getDate());
//                    Log.d(TAG, "getGoogleId: " + entry.getGoogleId());
//                    Log.d(TAG, "hasGoogleId: " + entry.hasGoogleId());
//                    Log.d(TAG, "getUpdatedAtGoogle: " + entry.getUpdatedAtGoogle());
                }

            }

            float average = 0;
            if (meditationThisDuration != 0)
                average = sumRating / meditationThisDuration;

            if (runningAverageSeries != null) {
//                averageSeries.resetData(new DataPoint[]{
//                        new DataPoint(durationStartDate, average),
//                        new DataPoint(today, average),
//                });
//                averageSeries.resetData(EMApoints.toArray(new
//                        DataPoint[0]));
                meditationSeries.resetData(points.toArray(new
                        DataPoint[0]));

                runningAverageSeries.resetData(runningAveragePoints.toArray(new
                        DataPoint[0]));
            }
            graphView.getViewport().setMinX(durationStartDate.getTime());
            graphView.getViewport().setXAxisBoundsManual(true);
            graphView.getGridLabelRenderer().setNumHorizontalLabels(14);
            graphView.refreshDrawableState();

            averageTV.setText(TimeFormatter.floatToString(average));
            meditationsTV.setText(String.valueOf(meditationThisDuration));
        }
    }
}
