package com.bautista.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.bautista.myapplication.database.entities.DailyStatsEntity;

import java.util.List;

@Dao
public interface DailyStatsDao {
    
    @Insert
    void insert(DailyStatsEntity stats);
    
    @Update
    void update(DailyStatsEntity stats);
    
    @Delete
    void delete(DailyStatsEntity stats);
    
    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    DailyStatsEntity getStatsForDate(String date);
    
    @Query("SELECT * FROM daily_stats ORDER BY timestamp DESC LIMIT 7")
    List<DailyStatsEntity> getLastSevenDays();
    
    @Query("SELECT AVG(wellness_score) FROM daily_stats WHERE date >= :startDate")
    int getAverageWellnessScore(String startDate);
    
    @Query("DELETE FROM daily_stats WHERE date < :date")
    void deleteOlderThan(String date);
    
    @Query("SELECT * FROM daily_stats ORDER BY timestamp DESC")
    List<DailyStatsEntity> getAllStats();
}
