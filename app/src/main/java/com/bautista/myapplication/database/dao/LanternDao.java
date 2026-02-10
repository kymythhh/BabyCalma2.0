package com.bautista.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.bautista.myapplication.database.entities.LanternEntity;

import java.util.List;

@Dao
public interface LanternDao {
    
    @Insert
    long insert(LanternEntity lantern);
    
    @Update
    void update(LanternEntity lantern);
    
    @Delete
    void delete(LanternEntity lantern);
    
    @Query("SELECT * FROM lanterns WHERE username = :username AND created_date = :date AND is_released = 0 ORDER BY created_timestamp ASC")
    List<LanternEntity> getActiveLanternsForDate(String username, String date);
    
    @Query("SELECT * FROM lanterns WHERE username = :username AND created_date = :date ORDER BY created_timestamp DESC")
    List<LanternEntity> getAllLanternsForDate(String username, String date);
    
    @Query("SELECT COUNT(*) FROM lanterns WHERE username = :username AND created_date = :date")
    int getTotalCountForDate(String username, String date);
    
    @Query("SELECT COUNT(*) FROM lanterns WHERE username = :username AND created_date = :date AND is_released = 0")
    int getActiveCountForDate(String username, String date);
    
    @Query("SELECT COUNT(*) FROM lanterns WHERE username = :username AND created_date = :date AND is_released = 1")
    int getReleasedCountForDate(String username, String date);
    
    @Query("UPDATE lanterns SET is_released = 1, released_timestamp = :timestamp WHERE id = :lanternId")
    void markAsReleased(int lanternId, long timestamp);
    
    @Query("DELETE FROM lanterns WHERE username = :username AND created_date = :date")
    void deleteByDate(String username, String date);
    
    @Query("DELETE FROM lanterns WHERE username = :username AND created_date < :date")
    void deleteOlderThan(String username, String date);
    
    @Query("SELECT * FROM lanterns WHERE username = :username ORDER BY created_timestamp DESC")
    List<LanternEntity> getAllLanterns(String username);
}
