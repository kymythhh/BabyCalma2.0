package com.bautista.myapplication.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.bautista.myapplication.database.dao.BreathingSessionDao;
import com.bautista.myapplication.database.dao.DailyStatsDao;
import com.bautista.myapplication.database.dao.FocusSessionDao;
import com.bautista.myapplication.database.dao.LanternDao;
import com.bautista.myapplication.database.dao.UserProfileDao;
import com.bautista.myapplication.database.dao.WaterIntakeDao;
import com.bautista.myapplication.database.entities.BreathingSessionEntity;
import com.bautista.myapplication.database.entities.DailyStatsEntity;
import com.bautista.myapplication.database.entities.FocusSessionEntity;
import com.bautista.myapplication.database.entities.LanternEntity;
import com.bautista.myapplication.database.entities.UserProfileEntity;
import com.bautista.myapplication.database.entities.WaterIntakeEntity;

@Database(
    entities = {
        WaterIntakeEntity.class,
        BreathingSessionEntity.class,
        FocusSessionEntity.class,
        LanternEntity.class,
        DailyStatsEntity.class,
        UserProfileEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class BabyCalmaDatabase extends RoomDatabase {
    
    private static BabyCalmaDatabase instance;
    
    // Abstract methods to get DAOs
    public abstract WaterIntakeDao waterIntakeDao();
    public abstract BreathingSessionDao breathingSessionDao();
    public abstract FocusSessionDao focusSessionDao();
    public abstract LanternDao lanternDao();
    public abstract DailyStatsDao dailyStatsDao();
    public abstract UserProfileDao userProfileDao();
    
    // Singleton pattern
    public static synchronized BabyCalmaDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                BabyCalmaDatabase.class,
                "baby_calma_database"
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
    
    // Close database
    public static synchronized void closeDatabase() {
        if (instance != null && instance.isOpen()) {
            instance.close();
            instance = null;
        }
    }
}
