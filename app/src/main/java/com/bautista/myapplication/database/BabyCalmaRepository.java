package com.bautista.myapplication.database;

import android.content.Context;

import com.bautista.myapplication.database.entities.BreathingSessionEntity;
import com.bautista.myapplication.database.entities.FocusSessionEntity;
import com.bautista.myapplication.database.entities.LanternEntity;
import com.bautista.myapplication.database.entities.UserProfileEntity;
import com.bautista.myapplication.database.entities.WaterIntakeEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository class to handle all database operations.
 * Provides a clean API for Activities to interact with the database.
 */
public class BabyCalmaRepository {
    
    private final BabyCalmaDatabase database;
    private final Executor executor;
    
    public BabyCalmaRepository(Context context) {
        this.database = BabyCalmaDatabase.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    // ==================== WATER INTAKE ====================
    
    public void saveWaterIntake(final int glassCount, final String date, final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WaterIntakeEntity existing = database.waterIntakeDao().getByDate(date);
                    if (existing != null) {
                        existing.glassCount = glassCount;
                        existing.timestamp = System.currentTimeMillis();
                        database.waterIntakeDao().update(existing);
                    } else {
                        WaterIntakeEntity entity = new WaterIntakeEntity(
                            glassCount, 
                            date, 
                            System.currentTimeMillis()
                        );
                        database.waterIntakeDao().insert(entity);
                    }
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    public void getWaterIntakeForToday(final String date, final DataCallback<Integer> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int count = database.waterIntakeDao().getTotalGlassesForDate(date);
                    if (callback != null) callback.onDataLoaded(count);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    // ==================== BREATHING SESSIONS ====================
    
    public void saveBreathingSession(final int cycles, final int durationSeconds, 
                                     final String date, final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BreathingSessionEntity entity = new BreathingSessionEntity(
                        cycles,
                        durationSeconds,
                        date,
                        System.currentTimeMillis()
                    );
                    database.breathingSessionDao().insert(entity);
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    public void getTotalBreathingCyclesForToday(final String date, final DataCallback<Integer> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int total = database.breathingSessionDao().getTotalCyclesForDate(date);
                    if (callback != null) callback.onDataLoaded(total);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    // ==================== FOCUS SESSIONS ====================
    
    public void saveFocusSession(final int durationMinutes, final String date, 
                                final boolean completed, final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FocusSessionEntity entity = new FocusSessionEntity(
                        durationMinutes,
                        date,
                        System.currentTimeMillis(),
                        completed
                    );
                    database.focusSessionDao().insert(entity);
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    public void getTotalFocusMinutesForToday(final String date, final DataCallback<Integer> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int total = database.focusSessionDao().getTotalMinutesForDate(date);
                    if (callback != null) callback.onDataLoaded(total);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    // ==================== LANTERNS ====================
    
    public void saveLantern(final String stressorText, final String date, final DataCallback<Long> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LanternEntity entity = new LanternEntity(
                        stressorText,
                        date,
                        System.currentTimeMillis(),
                        false
                    );
                    long id = database.lanternDao().insert(entity);
                    if (callback != null) callback.onDataLoaded(id);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    public void releaseLantern(final int lanternId, final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    database.lanternDao().markAsReleased(lanternId, System.currentTimeMillis());
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    public void getActiveLanterns(final String date, final DataCallback<List<LanternEntity>> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<LanternEntity> lanterns = database.lanternDao().getActiveLanternsForDate(date);
                    if (callback != null) callback.onDataLoaded(lanterns);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    public void getLanternStats(final String date, final DataCallback<LanternStats> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int total = database.lanternDao().getTotalCountForDate(date);
                    int active = database.lanternDao().getActiveCountForDate(date);
                    int released = database.lanternDao().getReleasedCountForDate(date);
                    
                    LanternStats stats = new LanternStats(total, active, released);
                    if (callback != null) callback.onDataLoaded(stats);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    // ==================== USER PROFILE ====================
    
    public void saveUserProfile(final String username, final String firstLetter, 
                               final String createdDate, final DatabaseCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserProfileEntity entity = new UserProfileEntity(
                        username,
                        firstLetter,
                        createdDate,
                        System.currentTimeMillis()
                    );
                    database.userProfileDao().insert(entity);
                    if (callback != null) callback.onSuccess();
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    public void getUserProfile(final DataCallback<UserProfileEntity> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserProfileEntity profile = database.userProfileDao().getUserProfile();
                    if (callback != null) callback.onDataLoaded(profile);
                } catch (Exception e) {
                    if (callback != null) callback.onError(e);
                }
            }
        });
    }
    
    // ==================== CALLBACKS ====================
    
    public interface DatabaseCallback {
        void onSuccess();
        void onError(Exception e);
    }
    
    public interface DataCallback<T> {
        void onDataLoaded(T data);
        void onError(Exception e);
    }
    
    // ==================== HELPER CLASSES ====================
    
    public static class LanternStats {
        public final int total;
        public final int active;
        public final int released;
        
        public LanternStats(int total, int active, int released) {
            this.total = total;
            this.active = active;
            this.released = released;
        }
    }
}
