package com.bautista.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "affirmations")
public class AffirmationEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    @ColumnInfo(name = "affirmation_text")
    public String affirmationText;
    
    @ColumnInfo(name = "display_order")
    public int displayOrder;
    
    public AffirmationEntity(String affirmationText, int displayOrder) {
        this.affirmationText = affirmationText;
        this.displayOrder = displayOrder;
    }
}
