package edu.amrita.elearn.iamhelper.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryModel {
    private static final String TAG = HistoryModel.class.getSimpleName();
    private static HistoryModel singelton;

    private ExecutorService executor;
    private Database mDatabase;
    private LiveData<List<IamEntry>> liveEntries;

    public static HistoryModel getModel() {
        if (singelton == null)
            singelton = new HistoryModel();
        return singelton;
    }

    public void init(Application application) {
        Log.d(TAG, "Init HistoryModel " + System.currentTimeMillis());
        mDatabase = Database.getInstance(application);
        executor = Executors.newSingleThreadExecutor();
        liveEntries = mDatabase.taskDao().loadAllItems();
        Log.d(TAG, "Init HistoryModel " + System.currentTimeMillis());
    }

    public void insertEntry(final IamEntry iamEntry) {
        executor.execute(() -> {
            long id = mDatabase.taskDao().insertTask(iamEntry);
            iamEntry.setId((int) id);
            Log.d(TAG, "Entered new IamEntry with id: " + id);
        });
    }

    public LiveData<List<IamEntry>> getLiveRatings() {
        return liveEntries;
    }

    public List<IamEntry> getStaticEntries() {
        return mDatabase.taskDao().loadAllItemsStatic();
    }

    public List<IamEntry> getEntriesForDate(Date dateStart) {
        Log.d(TAG,dateStart.toString());
        Date dateEnd = new Date(dateStart.getTime() + 1000 * 60 * 60 * 24);
        Log.d(TAG,dateEnd.toString());
        return mDatabase.taskDao().loadItemsOnDate(dateStart, dateEnd);
    }

    public void deleteEntry(IamEntry entry) {
        mDatabase.taskDao().deleteTask(entry);
    }

    public IamEntry getEntryForExactTime(Date exactDate) {
        return mDatabase.taskDao().getEntryFromDate(exactDate);
    }

//    private void insertFakeEntries() {
//        for (long i = 35; i >= 0; i--) {
//            final int rating = (int) Math.round(Math.random() * 4) + 4;
//            final Date getDate = new Date();
//            final long diffDate = (long) (((Math.random()) - 0.5f) * 86400000);
//            //+86400000*diffDate)
//            getDate.setTime((getDate.getTime() - 86400000L * i + diffDate));
//            final IamEntry iamEntry = new IamEntry(0, rating, "", getDate, 1000);
//            insertEntry(iamEntry);
//            Log.d(TAG, String.valueOf("diff: " + 86400000 * diffDate));
//            Log.d(TAG, String.valueOf("inPast: " + 86400000 * i));
//            Log.d(TAG, String.valueOf(getDate.getTime()));
//            Log.d(TAG, "Entered new IamEntry with Rating: " + rating + ", getDate: " + getDate);
//        }
//    }
}
