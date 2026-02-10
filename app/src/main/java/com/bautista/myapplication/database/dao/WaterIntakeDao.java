package com.bautista.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.bautista.myapplication.database.entities.WaterIntakeEntity;

import java.util.List;

@Dao
public interface WaterIntakeDao {
    
    @Insert
    void insert(WaterIntakeEntity waterIntake);
    
    @Update
    void update(WaterIntakeEntity waterIntake);
    
    @Delete
    void delete(WaterIntakeEntity waterIntake);
    
    @Query("SELECT * FROM water_intake WHERE username = :username AND date = :date LIMIT 1")
    WaterIntakeEntity getByDate(String username, String date);
    
    @Query("SELECT SUM(glass_count) FROM water_intake WHERE username = :username AND date = :date")
    int getTotalGlassesForDate(String username, String date);
    
    @Query("DELETE FROM water_intake WHERE username = :username AND date = :date")
    void deleteByDate(String username, String date);
    
    @Query("DELETE FROM water_intake WHERE username = :username AND date < :date")
    void deleteOlderThan(String username, String date);
    
    @Query("SELECT * FROM water_intake WHERE username = :username ORDER BY timestamp DESC")
    List<WaterIntakeEntity> getAllRecords(String username);
}
