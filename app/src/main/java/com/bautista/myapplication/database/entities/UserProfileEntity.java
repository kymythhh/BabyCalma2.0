package com.bautista.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfileEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    @ColumnInfo(name = "username")
    public String username;
    
    @ColumnInfo(name = "first_letter")
    public String firstLetter;
    
    @ColumnInfo(name = "created_date")
    public String createdDate;
    
    @ColumnInfo(name = "last_login")
    public long lastLogin;
    
    public UserProfileEntity(String username, String firstLetter, String createdDate, long lastLogin) {
        this.username = username;
        this.firstLetter = firstLetter;
        this.createdDate = createdDate;
        this.lastLogin = lastLogin;
    }
}
