package com.bautista.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.bautista.myapplication.database.entities.AffirmationEntity;

import java.util.List;

@Dao
public interface AffirmationDao {
    
    @Insert
    void insertAll(List<AffirmationEntity> affirmations);
    
    @Query("SELECT * FROM affirmations ORDER BY display_order ASC")
    List<AffirmationEntity> getAllAffirmations();
    
    @Query("SELECT COUNT(*) FROM affirmations")
    int getAffirmationCount();
    
    @Query("SELECT * FROM affirmations WHERE id = :id LIMIT 1")
    AffirmationEntity getAffirmationById(int id);
    
    @Query("DELETE FROM affirmations")
    void deleteAll();
}
