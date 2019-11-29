package edu.amrita.elearn.iamhelper.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Dao
public interface IamDao {

    @Query("SELECT * FROM iamtable ORDER BY created_at")
    LiveData<List<IamEntry>> loadAllItems();

    @Query("SELECT * FROM iamtable ORDER BY created_at")
    List<IamEntry> loadAllItemsStatic();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTask(IamEntry taskEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertTask(ArrayList<IamEntry> taskEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTask(IamEntry taskEntry);

    @Delete
    void deleteTask(IamEntry taskEntry);

    @Query("SELECT * FROM iamtable WHERE id = :id")
    LiveData<IamEntry> loadTaskById(int id);

    @Query("SELECT * FROM iamtable WHERE created_at = :exactDate")
    IamEntry getEntryFromDate(Date exactDate);

    @Query("SELECT * FROM iamtable WHERE created_at >= :dateStart AND created_at < :dateEnd ORDER BY created_at ASC")
    List<IamEntry> loadItemsOnDate(Date dateStart, Date dateEnd);
}
