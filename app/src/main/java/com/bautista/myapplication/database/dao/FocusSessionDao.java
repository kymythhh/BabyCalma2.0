package com.bautista.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.bautista.myapplication.database.entities.FocusSessionEntity;

import java.util.List;

@Dao
public interface FocusSessionDao {
    
    @Insert
    void insert(FocusSessionEntity session);
    
    @Update
    void update(FocusSessionEntity session);
    
    @Delete
    void delete(FocusSessionEntity session);
    
    @Query("SELECT * FROM focus_sessions WHERE date = :date ORDER BY timestamp DESC")
    List<FocusSessionEntity> getSessionsForDate(String date);
    
    @Query("SELECT SUM(duration_minutes) FROM focus_sessions WHERE date = :date AND completed = 1")
    int getTotalMinutesForDate(String date);
    
    @Query("SELECT COUNT(*) FROM focus_sessions WHERE date = :date AND completed = 1")
    int getCompletedSessionsForDate(String date);
    
    @Query("DELETE FROM focus_sessions WHERE date = :date")
    void deleteByDate(String date);
    
    @Query("DELETE FROM focus_sessions WHERE date < :date")
    void deleteOlderThan(String date);
    
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    List<FocusSessionEntity> getAllSessions();
}
