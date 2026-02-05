package com.bautista.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.bautista.myapplication.database.entities.BreathingSessionEntity;

import java.util.List;

@Dao
public interface BreathingSessionDao {
    
    @Insert
    void insert(BreathingSessionEntity session);
    
    @Update
    void update(BreathingSessionEntity session);
    
    @Delete
    void delete(BreathingSessionEntity session);
    
    @Query("SELECT * FROM breathing_sessions WHERE date = :date ORDER BY timestamp DESC")
    List<BreathingSessionEntity> getSessionsForDate(String date);
    
    @Query("SELECT SUM(cycles) FROM breathing_sessions WHERE date = :date")
    int getTotalCyclesForDate(String date);
    
    @Query("SELECT SUM(duration_seconds) FROM breathing_sessions WHERE date = :date")
    int getTotalDurationForDate(String date);
    
    @Query("DELETE FROM breathing_sessions WHERE date = :date")
    void deleteByDate(String date);
    
    @Query("DELETE FROM breathing_sessions WHERE date < :date")
    void deleteOlderThan(String date);
    
    @Query("SELECT * FROM breathing_sessions ORDER BY timestamp DESC")
    List<BreathingSessionEntity> getAllSessions();
}
