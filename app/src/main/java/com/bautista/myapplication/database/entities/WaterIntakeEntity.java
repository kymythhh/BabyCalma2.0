package com.bautista.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_intake")
public class WaterIntakeEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    @ColumnInfo(name = "glass_count")
    public int glassCount;
    
    @ColumnInfo(name = "date")
    public String date; // Format: YYYY-MM-DD
    
    @ColumnInfo(name = "timestamp")
    public long timestamp;
    
    public WaterIntakeEntity(int glassCount, String date, long timestamp) {
        this.glassCount = glassCount;
        this.date = date;
        this.timestamp = timestamp;
    }
}
