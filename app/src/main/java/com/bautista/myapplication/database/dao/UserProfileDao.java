package com.bautista.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.bautista.myapplication.database.entities.UserProfileEntity;

@Dao
public interface UserProfileDao {
    
    @Insert
    void insert(UserProfileEntity profile);
    
    @Update
    void update(UserProfileEntity profile);
    
    @Delete
    void delete(UserProfileEntity profile);
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    UserProfileEntity getUserProfile();
    
    @Query("UPDATE user_profile SET last_login = :timestamp WHERE id = 1")
    void updateLastLogin(long timestamp);
    
    @Query("SELECT COUNT(*) FROM user_profile")
    int getUserCount();
    
    @Query("DELETE FROM user_profile")
    void deleteAll();
}
