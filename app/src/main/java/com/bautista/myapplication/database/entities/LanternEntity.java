package com.bautista.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lanterns")
public class LanternEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    @ColumnInfo(name = "username")
    public String username;
    
    @ColumnInfo(name = "stressor_text")
    public String stressorText;
    
    @ColumnInfo(name = "created_date")
    public String createdDate; // Format: YYYY-MM-DD
    
    @ColumnInfo(name = "created_timestamp")
    public long createdTimestamp;
    
    @ColumnInfo(name = "released_timestamp")
    public Long releasedTimestamp; // Nullable
    
    @ColumnInfo(name = "is_released")
    public boolean isReleased;
    
    public LanternEntity(String username, String stressorText, String createdDate, long createdTimestamp, boolean isReleased) {
        this.username = username;
        this.stressorText = stressorText;
        this.createdDate = createdDate;
        this.createdTimestamp = createdTimestamp;
        this.isReleased = isReleased;
        this.releasedTimestamp = null;
    }
}
