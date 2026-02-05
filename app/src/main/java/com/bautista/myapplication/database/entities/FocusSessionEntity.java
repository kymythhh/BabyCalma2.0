package com.bautista.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_sessions")
public class FocusSessionEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    @ColumnInfo(name = "duration_minutes")
    public int durationMinutes;
    
    @ColumnInfo(name = "date")
    public String date; // Format: YYYY-MM-DD
    
    @ColumnInfo(name = "timestamp")
    public long timestamp;
    
    @ColumnInfo(name = "completed")
    public boolean completed;
    
    public FocusSessionEntity(int durationMinutes, String date, long timestamp, boolean completed) {
        this.durationMinutes = durationMinutes;
        this.date = date;
        this.timestamp = timestamp;
        this.completed = completed;
    }
}
