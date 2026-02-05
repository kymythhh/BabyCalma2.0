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
    
    @Query("SELECT * FROM water_intake WHERE date = :date LIMIT 1")
    WaterIntakeEntity getByDate(String date);
    
    @Query("SELECT SUM(glass_count) FROM water_intake WHERE date = :date")
    int getTotalGlassesForDate(String date);
    
    @Query("DELETE FROM water_intake WHERE date = :date")
    void deleteByDate(String date);
    
    @Query("DELETE FROM water_intake WHERE date < :date")
    void deleteOlderThan(String date);
    
    @Query("SELECT * FROM water_intake ORDER BY timestamp DESC")
    List<WaterIntakeEntity> getAllRecords();
}
