package com.bautista.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "breathing_sessions")
public class BreathingSessionEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    @ColumnInfo(name = "cycles")
    public int cycles;
    
    @ColumnInfo(name = "duration_seconds")
    public int durationSeconds;
    
    @ColumnInfo(name = "date")
    public String date; // Format: YYYY-MM-DD
    
    @ColumnInfo(name = "timestamp")
    public long timestamp;
    
    public BreathingSessionEntity(int cycles, int durationSeconds, String date, long timestamp) {
        this.cycles = cycles;
        this.durationSeconds = durationSeconds;
        this.date = date;
        this.timestamp = timestamp;
    }
}
