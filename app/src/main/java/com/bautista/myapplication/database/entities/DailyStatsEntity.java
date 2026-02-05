package com.bautista.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_stats")
public class DailyStatsEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    @ColumnInfo(name = "date")
    public String date; // Format: YYYY-MM-DD
    
    @ColumnInfo(name = "wellness_score")
    public int wellnessScore;
    
    @ColumnInfo(name = "total_water")
    public int totalWater;
    
    @ColumnInfo(name = "total_breathing_cycles")
    public int totalBreathingCycles;
    
    @ColumnInfo(name = "total_focus_minutes")
    public int totalFocusMinutes;
    
    @ColumnInfo(name = "total_lanterns_released")
    public int totalLanternsReleased;
    
    @ColumnInfo(name = "timestamp")
    public long timestamp;
    
    public DailyStatsEntity(String date, int wellnessScore, int totalWater, 
                           int totalBreathingCycles, int totalFocusMinutes, 
                           int totalLanternsReleased, long timestamp) {
        this.date = date;
        this.wellnessScore = wellnessScore;
        this.totalWater = totalWater;
        this.totalBreathingCycles = totalBreathingCycles;
        this.totalFocusMinutes = totalFocusMinutes;
        this.totalLanternsReleased = totalLanternsReleased;
        this.timestamp = timestamp;
    }
}
