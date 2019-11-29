package edu.amrita.elearn.iamhelper.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.util.Log;

@android.arch.persistence.room.Database(entities = {IamEntry.class}, version = 4, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class Database extends RoomDatabase {

    private static final String LOG_TAG = Database.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "meditations";
    private static Database sInstance;


    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE iamtable "
                    + "ADD COLUMN google_id TEXT");
            database.execSQL("ALTER TABLE iamtable "
                    + "ADD COLUMN updated_at_google INTEGER");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE iamtable "
                    + "ADD COLUMN updated_locally INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static Database getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(),
                                Database.class, Database.DATABASE_NAME)
                        .addMigrations(MIGRATION_2_3, MIGRATION_3_4).build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract IamDao taskDao();

}
